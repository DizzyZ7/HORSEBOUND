// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Live Homestead presentation and application orchestration over the validated ranch renderer.
 * Domain truth remains in GameSession and SaveGame v5.
 */
final class HomesteadRanchScreen implements RanchSessionScreen {
    private static final HomesteadStructureType[] BUILD_TYPES = HomesteadStructureType.values();
    private static final float PLACEMENT_DISTANCE = 4.4f;
    private static final float GRID_SIZE = 0.5f;
    private static final float INTERACTION_RADIUS = 3.8f;
    private static final float PLAYER_COLLISION_RADIUS = 0.45f;
    private static final float HORSE_COLLISION_RADIUS = 0.92f;

    private final HorseboundGame game;
    private final SaveService saveService;
    private final LivingRanchScreen delegate;
    private final RanchWorldAccess worldAccess;
    private final GameSession session;
    private final HomesteadRuntimeInput runtimeInput = new HomesteadRuntimeInput();
    private final HomesteadRenderer homesteadRenderer = new HomesteadRenderer();
    private final InventoryOverlay inventoryOverlay = new InventoryOverlay();
    private final HomesteadCollisionSystem collisionSystem = new HomesteadCollisionSystem();
    private final RanchUndoManager undoManager = new RanchUndoManager();
    private final RanchDismantleConfirmation dismantleConfirmation = new RanchDismantleConfirmation();
    private final HorseCareSystem careSystem = new HorseCareSystem();
    private final FixedStepClock careClock = new FixedStepClock();
    private final RanchAmbience ambience;
    private final Map<UUID, HorseNeeds> horseNeeds = new LinkedHashMap<>();
    private final Map<UUID, Position> previousHorsePositions = new HashMap<>();
    private final Set<UUID> hiddenLegacyStructureIds;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = GameFonts.create();

    private HomesteadRenderer.PlacementPreview placement;
    private UUID editedStructureId;
    private int buildTypeIndex;
    private float placementHeading;
    private boolean buildMode;
    private boolean editMode;
    private boolean disposed;
    private Language displayedLanguage = I18n.language();
    private String homesteadStatus = I18n.text("homestead.initial");
    private float statusTimer = 7f;
    private float careFeedbackCooldown;

    HomesteadRanchScreen(HorseboundGame game, SaveService saveService, SaveGame initialState) {
        this.game = game;
        this.saveService = saveService;

        LivingRanchScreen created;
        GameSession captured;
        GameSessionCapture.begin();
        try {
            created = new LivingRanchScreen(game, saveService, initialState);
            captured = GameSessionCapture.finish();
        } catch (RuntimeException | Error ex) {
            GameSessionCapture.cancel();
            throw ex;
        }
        delegate = created;
        worldAccess = created;
        session = captured;
        ambience = new RanchAmbience(initialState.worldSeed());

        for (SaveGame.HorseData horse : initialState.horses()) {
            horseNeeds.put(horse.id(), horse.needs());
            previousHorsePositions.put(horse.id(), new Position(horse.x(), horse.z()));
        }
        hiddenLegacyStructureIds = legacyStructureIds(initialState);
        saveService.setSaveTransformer(this, this::enrichSave);
    }

    @Override
    public void show() {
        HomesteadActionBus.reset();
        HomesteadInputContext.configure(true, false, false, true, false);
        delegate.show();
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(Math.max(0f, delta), FixedStepClock.DEFAULT_MAX_FRAME_SECONDS);

        if (inventoryOverlay.isOpen()) {
            renderFrozenWorldAndInventory(frameDelta);
            return;
        }

        dismantleConfirmation.tick(frameDelta);
        RanchWorldAccess.ActorPose beforePose = worldAccess.actorPose();
        PlacedStructure nearby = nearestInteractiveStructure(beforePose.x(), beforePose.z(), INTERACTION_RADIUS);
        boolean placementMode = buildMode || editMode;
        HomesteadInputContext.configure(
            true,
            nearby != null,
            editMode,
            true,
            placementMode
        );

        HomesteadRuntimeInput.InputResult input = runtimeInput.sample(placementMode);
        applySelectionInput(input);
        placement = placementMode ? calculatePlacement(beforePose) : null;
        worldAccess.setCameraObstacles(cameraObstacles());

        try {
            delegate.render(delta);
        } finally {
            HomesteadInputContext.reset();
        }
        if (game.getScreen() != this) return;

        resolveStructureCollisions(beforePose);
        RanchWorldAccess.ActorPose currentPose = worldAccess.actorPose();
        nearby = nearestInteractiveStructure(currentPose.x(), currentPose.z(), INTERACTION_RADIUS);
        handleSemanticActions(nearby, input.editPressed(), input.undoPressed());
        careClock.advance(frameDelta, this::updateHorseCare);
        ambience.update(frameDelta, session.worldTime());
        placement = buildMode || editMode ? calculatePlacement(worldAccess.actorPose()) : null;
        UUID selected = editMode && editedStructureId != null
            ? editedStructureId
            : nearby == null ? null : nearby.id();
        homesteadRenderer.render(
            worldAccess.camera(),
            session.homestead(),
            hiddenLegacyStructureIds,
            placement,
            selected
        );
        renderHomesteadHud();

        statusTimer = Math.max(0f, statusTimer - frameDelta);
        careFeedbackCooldown = Math.max(0f, careFeedbackCooldown - frameDelta);
    }

    private void renderFrozenWorldAndInventory(float frameDelta) {
        HomesteadInputContext.configure(true, true, true, true, true);
        worldAccess.setCameraObstacles(cameraObstacles());
        try {
            delegate.render(0f);
        } finally {
            HomesteadInputContext.reset();
        }
        if (game.getScreen() != this) return;

        if (HomesteadActionBus.consumeInventory() || HomesteadActionBus.consumeCancel()) inventoryOverlay.close();
        if (!inventoryOverlay.isOpen()) {
            HomesteadActionBus.reset();
            return;
        }
        homesteadRenderer.render(worldAccess.camera(), session.homestead(), hiddenLegacyStructureIds, null, null);
        inventoryOverlay.updateAndRender(game.settings().uiScale());
        statusTimer = Math.max(0f, statusTimer - frameDelta);
    }

    private void applySelectionInput(HomesteadRuntimeInput.InputResult input) {
        if (input.directSlot() >= 0) session.hotbar().select(input.directSlot());
        if (!buildMode && !editMode && input.hotbarDelta() != 0) session.hotbar().cycle(input.hotbarDelta());
        if (buildMode && input.buildTypeDelta() != 0) {
            buildTypeIndex = Math.floorMod(buildTypeIndex + input.buildTypeDelta(), BUILD_TYPES.length);
            setStatus(I18n.text("homestead.blueprint", selectedBuildType().displayName()));
        }
        if ((buildMode || editMode) && input.rotationDelta() != 0) {
            placementHeading = snapHeading(placementHeading + input.rotationDelta() * 15f);
        }
    }

    private void handleSemanticActions(PlacedStructure nearby, boolean editPressed, boolean undoPressed) {
        if (HomesteadActionBus.consumeCancel()) cancelPlacement(I18n.text("homestead.cancelled"));

        if (undoPressed && !buildMode && !editMode) {
            undoLastRanchEdit();
        }

        if (HomesteadActionBus.consumeInventory()) {
            if (buildMode || editMode) {
                setStatus(I18n.text("homestead.cancel_inventory"));
            } else {
                inventoryOverlay.open(session.inventory(), null);
                return;
            }
        }

        if (editPressed && !buildMode && !editMode) startEditMode();

        if (HomesteadActionBus.consumeBuild()) {
            if (editMode) confirmRelocation();
            else if (!buildMode) startBuildMode();
            else confirmPlacement();
        }

        if (HomesteadActionBus.consumeDismantle() && editMode) requestDismantleEditedStructure();
        if (HomesteadActionBus.consumeInteract()) interactWith(nearby);
    }

    private void startBuildMode() {
        dismantleConfirmation.cancel();
        buildMode = true;
        editMode = false;
        editedStructureId = null;
        placementHeading = snapHeading(worldAccess.actorPose().heading());
        placement = calculatePlacement(worldAccess.actorPose());
        setStatus(I18n.text("homestead.build_mode", selectedBuildType().displayName()));
    }

    private void startEditMode() {
        dismantleConfirmation.cancel();
        RanchWorldAccess.ActorPose pose = worldAccess.actorPose();
        PlacedStructure nearest = session.homestead().nearest(pose.x(), pose.z(), INTERACTION_RADIUS)
            .filter(value -> !hiddenLegacyStructureIds.contains(value.id()))
            .orElse(null);
        if (nearest == null) {
            setStatus(I18n.text("homestead.no_editable"));
            return;
        }
        buildMode = false;
        editMode = true;
        editedStructureId = nearest.id();
        placementHeading = nearest.heading();
        placement = calculatePlacement(pose);
        setStatus(I18n.text("homestead.editing", nearest.type().displayName()));
    }

    private void cancelPlacement(String message) {
        dismantleConfirmation.cancel();
        buildMode = false;
        editMode = false;
        editedStructureId = null;
        placement = null;
        setStatus(message);
    }

    private void confirmPlacement() {
        if (placement == null || !placement.valid()) {
            setStatus(placement == null ? I18n.text("homestead.no_preview") : placement.reason());
            return;
        }
        HomesteadStructureType type = selectedBuildType();
        PlacedStructure placed = session.homestead().place(
            type,
            placement.x(),
            placement.z(),
            placement.heading(),
            session.inventory()
        ).orElse(null);
        if (placed == null) {
            setStatus(I18n.text("homestead.missing_materials", costText(type)));
            return;
        }
        undoManager.recordPlacement(placed);
        ControllerRumble.pulse(game.inputProfile(), 65, 0.42f);
        setStatus(I18n.text("homestead.placed", type.displayName()));
    }

    private void confirmRelocation() {
        if (editedStructureId == null || placement == null || !placement.valid()) {
            setStatus(placement == null ? I18n.text("homestead.no_relocation_preview") : placement.reason());
            return;
        }
        PlacedStructure structure = session.homestead().find(editedStructureId).orElse(null);
        if (structure == null) {
            cancelPlacement(I18n.text("homestead.unavailable"));
            return;
        }
        float fromX = structure.x();
        float fromZ = structure.z();
        float fromHeading = structure.heading();
        if (!session.homestead().relocate(
            editedStructureId,
            placement.x(),
            placement.z(),
            placement.heading()
        )) {
            cancelPlacement(I18n.text("homestead.unavailable"));
            return;
        }
        undoManager.recordRelocation(
            structure,
            fromX,
            fromZ,
            fromHeading,
            placement.x(),
            placement.z(),
            placement.heading()
        );
        ControllerRumble.pulse(game.inputProfile(), 60, 0.38f);
        cancelPlacement(I18n.text("homestead.moved"));
    }

    private void requestDismantleEditedStructure() {
        if (editedStructureId == null) return;
        PlacedStructure structure = session.homestead().find(editedStructureId).orElse(null);
        RanchDismantleConfirmation.Decision decision = dismantleConfirmation.request(structure);
        switch (decision) {
            case ARMED -> {
                RanchAudio.play(RanchAudio.Cue.DISMANTLE_ARM);
                ControllerRumble.pulse(game.inputProfile(), 32, 0.25f);
                setStatus(I18n.text(
                    "homestead.dismantle_armed",
                    Math.round(dismantleConfirmation.remainingSeconds())
                ));
            }
            case CONFIRMED -> performDismantleEditedStructure();
            case INVALID -> cancelPlacement(I18n.text("homestead.unavailable"));
        }
    }

    private void performDismantleEditedStructure() {
        if (editedStructureId == null) return;
        HomesteadState.DismantleResult result = session.homestead().dismantle(editedStructureId, session.inventory());
        switch (result) {
            case SUCCESS -> {
                undoManager.clear();
                ControllerRumble.pulse(game.inputProfile(), 70, 0.45f);
                cancelPlacement(I18n.text("homestead.dismantled"));
            }
            case STORAGE_NOT_EMPTY -> {
                dismantleConfirmation.cancel();
                setStatus(I18n.text("homestead.empty_before_dismantle"));
            }
            case INVENTORY_FULL -> {
                dismantleConfirmation.cancel();
                setStatus(I18n.text("homestead.refund_full"));
            }
            case NOT_FOUND -> cancelPlacement(I18n.text("homestead.unavailable"));
        }
    }

    private void interactWith(PlacedStructure structure) {
        if (structure == null) return;
        if (structure.type().canToggleOpen()) {
            if (session.homestead().toggleGate(structure.id())) {
                ControllerRumble.pulse(game.inputProfile(), 42, 0.30f);
                setStatus(I18n.text(structure.isOpen() ? "homestead.gate_opened" : "homestead.gate_closed"));
            }
            return;
        }
        if (structure.type().storesItems()) {
            inventoryOverlay.open(session.inventory(), structure);
            return;
        }
        depositSelectedResource(structure);
    }

    private void depositSelectedResource(PlacedStructure structure) {
        if (structure == null || !structure.type().storesResource()) return;
        ItemId selected = session.hotbar().selectedItem();
        ItemId accepted = structure.type().acceptedResource();
        if (selected != accepted) {
            setStatus(I18n.text("homestead.accepts", structure.type().displayName(), accepted.displayName()));
            return;
        }
        int acceptedUnits = session.homestead().depositOneResource(structure.id(), session.inventory());
        if (acceptedUnits <= 0) {
            String reason = session.inventory().count(accepted) <= 0
                ? I18n.text("homestead.no_resource", accepted.displayName())
                : I18n.text("homestead.storage_full", structure.type().displayName());
            setStatus(reason);
            return;
        }
        ControllerRumble.pulse(game.inputProfile(), 45, 0.28f);
        setStatus(I18n.text(
            "homestead.deposited",
            accepted.displayName(),
            structure.storedUnits(),
            structure.type().storageCapacity()
        ));
    }

    private HomesteadRenderer.PlacementPreview calculatePlacement(RanchWorldAccess.ActorPose pose) {
        PlacedStructure edited = editMode ? session.homestead().find(editedStructureId).orElse(null) : null;
        HomesteadStructureType type = edited != null ? edited.type() : selectedBuildType();
        float x = snap(pose.x() + MathUtils.sinDeg(pose.heading()) * PLACEMENT_DISTANCE);
        float z = snap(pose.z() + MathUtils.cosDeg(pose.heading()) * PLACEMENT_DISTANCE);
        float y = Terrain.heightAt(x, z);
        String reason = validatePlacement(type, x, z, edited == null ? null : edited.id(), edited == null);
        return new HomesteadRenderer.PlacementPreview(
            true,
            type,
            x,
            y,
            z,
            snapHeading(placementHeading),
            reason == null,
            reason == null ? I18n.text("common.ready") : reason
        );
    }

    private String validatePlacement(
        HomesteadStructureType type,
        float x,
        float z,
        UUID ignoredStructureId,
        boolean checkRecipe
    ) {
        float limit = Terrain.WORLD_HALF_SIZE - 4f;
        if (Math.abs(x) > limit || Math.abs(z) > limit) return I18n.text("homestead.boundary");
        if (Terrain.isInsideLake(x, z)) return I18n.text("homestead.lake");

        float center = Terrain.heightAt(x, z);
        float maximumSlope = 0f;
        float sample = type == HomesteadStructureType.STALL ? 1.6f : 0.9f;
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x + sample, z)));
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x - sample, z)));
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x, z + sample)));
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x, z - sample)));
        if (maximumSlope > 0.85f) return I18n.text("homestead.steep");
        if (worldAccess.isNaturePlacementBlocked(x, z, type.collisionRadius() + 0.15f)) {
            return I18n.text("homestead.nature_overlap");
        }
        for (RanchWorldAccess.HorseTelemetry horse : worldAccess.horses()) {
            if (RanchPlacementCollision.overlaps(
                x, z, type.collisionRadius(), horse.x(), horse.z(), HORSE_COLLISION_RADIUS, 0.25f
            )) {
                return I18n.text("homestead.horse_overlap");
            }
        }

        for (PlacedStructure existing : session.homestead().structures()) {
            if (existing.id().equals(ignoredStructureId)) continue;
            float required = type.collisionRadius() + existing.type().collisionRadius() + 0.20f;
            if (distanceSquared(x, z, existing.x(), existing.z()) < required * required) {
                return I18n.text("homestead.structure_overlap", existing.type().displayName());
            }
        }
        if (checkRecipe) {
            for (Map.Entry<ItemId, Integer> cost : type.buildCost().entrySet()) {
                if (!session.inventory().has(cost.getKey(), cost.getValue())) {
                    return I18n.text("homestead.missing_materials", costText(type));
                }
            }
        }
        return null;
    }

    private void resolveStructureCollisions(RanchWorldAccess.ActorPose beforePose) {
        RanchWorldAccess.ActorPose afterPose = worldAccess.actorPose();
        float actorRadius = afterPose.mounted() ? HORSE_COLLISION_RADIUS : PLAYER_COLLISION_RADIUS;
        HomesteadCollisionSystem.Position actor = collisionSystem.resolve(
            beforePose.x(),
            beforePose.z(),
            afterPose.x(),
            afterPose.z(),
            actorRadius,
            session.homestead().structures()
        );
        if (actor.blocked()) worldAccess.setActorPosition(actor.x(), actor.z());

        for (RanchWorldAccess.HorseTelemetry horse : worldAccess.horses()) {
            if (horse.mounted()) {
                previousHorsePositions.put(horse.id(), new Position(actor.x(), actor.z()));
                continue;
            }
            Position previous = previousHorsePositions.getOrDefault(horse.id(), new Position(horse.x(), horse.z()));
            HomesteadCollisionSystem.Position corrected = collisionSystem.resolve(
                previous.x(),
                previous.z(),
                horse.x(),
                horse.z(),
                HORSE_COLLISION_RADIUS,
                session.homestead().structures()
            );
            if (corrected.blocked()) worldAccess.setHorsePosition(horse.id(), corrected.x(), corrected.z());
            previousHorsePositions.put(horse.id(), new Position(corrected.x(), corrected.z()));
        }
    }

    private void updateHorseCare(float fixedDelta) {
        for (RanchWorldAccess.HorseTelemetry horse : worldAccess.horses()) {
            HorseNeeds current = horseNeeds.getOrDefault(horse.id(), HorseNeeds.healthy());
            boolean moving = Math.abs(horse.speed()) > 0.15f;
            boolean galloping = Math.abs(horse.speed()) > 8f;
            if (!horse.tamed()) {
                horseNeeds.put(horse.id(), current.tick(fixedDelta, moving, galloping));
                continue;
            }
            HorseCareSystem.CareResult result = careSystem.update(
                current,
                fixedDelta,
                moving,
                galloping,
                horse.x(),
                horse.z(),
                session.homestead()
            );
            horseNeeds.put(horse.id(), result.needs());
            if (careFeedbackCooldown <= 0f && (result.fed() || result.watered() || result.rested())) {
                setStatus(I18n.text(result.fed()
                    ? "homestead.auto_hay"
                    : result.watered() ? "homestead.auto_water" : "homestead.auto_rest"));
                careFeedbackCooldown = 4f;
            }
        }
    }

    private List<RanchCameraCollisionSystem.Obstacle> cameraObstacles() {
        List<RanchCameraCollisionSystem.Obstacle> result = new ArrayList<>();
        for (PlacedStructure structure : session.homestead().structures()) {
            if (!structure.blocksMovement()) continue;
            result.add(new RanchCameraCollisionSystem.Obstacle(
                structure.x(),
                structure.z(),
                Math.max(0.45f, structure.type().collisionRadius()),
                cameraHeight(structure.type())
            ));
        }
        return List.copyOf(result);
    }

    private static float cameraHeight(HomesteadStructureType type) {
        return switch (type) {
            case FENCE -> 1.65f;
            case GATE -> 1.95f;
            case FEEDER -> 1.15f;
            case WATER_TROUGH -> 1.05f;
            case HAY_STORAGE -> 2.10f;
            case CHEST -> 1.15f;
            case STALL -> 2.85f;
        };
    }

    private SaveGame enrichSave(SaveGame base) {
        List<SaveGame.HorseData> enrichedHorses = new ArrayList<>(base.horses().size());
        for (SaveGame.HorseData horse : base.horses()) {
            HorseNeeds needs = horseNeeds.getOrDefault(horse.id(), horse.needs());
            enrichedHorses.add(new SaveGame.HorseData(
                horse.id(), horse.name(), horse.x(), horse.z(), horse.heading(), horse.trust(), horse.stamina(),
                horse.tamed(), horse.personality(), horse.bond(), horse.fear(),
                needs.hunger(), needs.thirst(), needs.energy()
            ));
        }

        LinkedHashMap<UUID, SaveGame.StructureData> structures = new LinkedHashMap<>();
        for (SaveGame.StructureData structure : session.homestead().toSaveData()) structures.put(structure.id(), structure);
        SaveGame compatibility = new SaveGame(
            SaveGame.CURRENT_VERSION,
            base.worldSeed(),
            base.savedAtEpochMillis(),
            base.worldTime(),
            base.player(),
            base.pushik(),
            enrichedHorses,
            base.fences(),
            base.harvestedTreeIds()
        );
        for (SaveGame.StructureData structure : compatibility.structures()) structures.putIfAbsent(structure.id(), structure);

        return new SaveGame(
            SaveGame.CURRENT_VERSION,
            base.worldSeed(),
            base.savedAtEpochMillis(),
            base.worldTime(),
            base.player(),
            base.pushik(),
            enrichedHorses,
            base.fences(),
            List.copyOf(structures.values()),
            session.hotbar().toSaveData(),
            base.harvestedTreeIds()
        );
    }

    private void renderHomesteadHud() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, game.settings().uiScale());
        float geometry = Math.min(ui, 1.18f);
        float slotSize = 44f * geometry;
        float gap = 5f * geometry;
        float totalWidth = Hotbar.SLOT_COUNT * slotSize + (Hotbar.SLOT_COUNT - 1) * gap;
        float startX = (width - totalWidth) * 0.5f;
        float startY = 76f * geometry;

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            shapes.setColor(i == session.hotbar().selectedIndex()
                ? new Color(0.72f, 0.55f, 0.20f, 0.96f)
                : new Color(0.06f, 0.10f, 0.08f, 0.90f));
            shapes.rect(startX + i * (slotSize + gap), startY, slotSize, slotSize);
        }
        shapes.end();

        batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        batch.begin();
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            float x = startX + i * (slotSize + gap);
            ItemId item = session.hotbar().itemAt(i);
            GameFonts.setScale(font, 0.62f * ui);
            font.setColor(Color.WHITE);
            font.draw(batch, Integer.toString(i + 1), x + 4f * geometry, startY + slotSize - 4f * geometry);
            if (item != null) {
                GameFonts.setScale(font, 0.53f * ui);
                font.draw(batch, shortName(item), x + 5f * geometry, startY + 22f * geometry);
                font.setColor(new Color(1f, 0.88f, 0.55f, 1f));
                font.draw(batch, Integer.toString(session.inventory().count(item)), x + 5f * geometry, startY + 9f * geometry);
            }
        }

        RanchWorldAccess.ActorPose pose = worldAccess.actorPose();
        RanchWorldAccess.HorseTelemetry nearest = nearestHorse(pose.x(), pose.z(), 12f);
        if (nearest != null) {
            HorseNeeds needs = horseNeeds.getOrDefault(nearest.id(), HorseNeeds.healthy());
            GameFonts.setScale(font, 0.68f * ui);
            font.setColor(new Color(0.94f, 0.90f, 0.69f, 1f));
            font.draw(
                batch,
                I18n.text(
                    "hud.horse_care",
                    Math.round(needs.hunger()),
                    Math.round(needs.thirst()),
                    Math.round(needs.energy())
                ),
                16f * geometry,
                startY + slotSize + 25f * geometry
            );
        }

        if (!buildMode && !editMode && !dismantleConfirmation.isArmed()) {
            RanchGuidance.Objective objective = RanchGuidance.next(session, worldAccess.horses());
            GameFonts.setScale(font, 0.68f * ui);
            font.setColor(new Color(0.72f, 0.92f, 1f, 1f));
            font.draw(
                batch,
                I18n.text("hud.objective", objective.title()),
                16f * geometry,
                height - 72f * geometry
            );
            GameFonts.setScale(font, 0.61f * ui);
            font.setColor(new Color(0.82f, 0.88f, 0.84f, 1f));
            font.draw(
                batch,
                I18n.text("hud.objective_detail", objective.detail()),
                16f * geometry,
                height - 94f * geometry
            );
        }

        if ((buildMode || editMode) && placement != null) {
            GameFonts.setScale(font, 0.68f * ui);
            font.setColor(placement.valid() ? new Color(0.62f, 1f, 0.68f, 1f) : new Color(1f, 0.58f, 0.50f, 1f));
            String prefix = I18n.text(editMode ? "homestead.mode_edit" : "homestead.mode_build");
            String controls = I18n.text(editMode ? "homestead.controls_edit" : "homestead.controls_build");
            String moveDelta = "";
            if (editMode && editedStructureId != null) {
                PlacedStructure edited = session.homestead().find(editedStructureId).orElse(null);
                if (edited != null) {
                    moveDelta = I18n.text(
                        "homestead.move_delta",
                        String.format(Locale.ROOT, "%.1f", edited.x()),
                        String.format(Locale.ROOT, "%.1f", edited.z()),
                        String.format(Locale.ROOT, "%.1f", placement.x()),
                        String.format(Locale.ROOT, "%.1f", placement.z())
                    );
                }
            }
            font.draw(
                batch,
                prefix + ": " + placement.type().displayName().toUpperCase(I18n.language().locale())
                    + (editMode ? "" : I18n.text("homestead.cost", costText(placement.type())))
                    + moveDelta
                    + controls
                    + (placement.valid() ? "" : I18n.text("homestead.invalid", placement.reason())),
                16f * geometry,
                height - 70f * geometry
            );
        }
        if (undoManager.hasPending()) {
            String undoLabel = I18n.text(undoManager.pendingKind() == RanchUndoManager.Kind.PLACEMENT
                ? "homestead.undo_build"
                : "homestead.undo_move");
            GameFonts.setScale(font, 0.64f * ui);
            font.setColor(new Color(0.62f, 0.88f, 1f, 1f));
            font.draw(
                batch,
                I18n.text("homestead.undo_controls", undoLabel),
                Math.max(16f * geometry, width - 330f * geometry),
                startY + slotSize + 25f * geometry
            );
        }
        if (dismantleConfirmation.isArmed()) {
            GameFonts.setScale(font, 0.72f * ui);
            font.setColor(new Color(1f, 0.54f, 0.34f, 1f));
            font.draw(
                batch,
                I18n.text(
                    "homestead.confirm_dismantle",
                    I18n.text(
                        "common.seconds_short",
                        String.format(Locale.ROOT, "%.1f", dismantleConfirmation.remainingSeconds())
                    )
                ),
                16f * geometry,
                height - 98f * geometry
            );
        }
        if (statusTimer > 0f) {
            GameFonts.setScale(font, 0.67f * ui);
            font.setColor(new Color(1f, 0.94f, 0.73f, 1f));
            font.draw(batch, homesteadStatus, 16f * geometry, startY + slotSize + 48f * geometry);
        }
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private PlacedStructure nearestInteractiveStructure(float x, float z, float radius) {
        return session.homestead().structures().stream()
            .filter(value -> value.type().storesResource() || value.type().storesItems() || value.type().canToggleOpen())
            .filter(value -> distanceSquared(x, z, value.x(), value.z()) <= radius * radius)
            .min(Comparator.comparingDouble(value -> distanceSquared(x, z, value.x(), value.z())))
            .orElse(null);
    }

    private RanchWorldAccess.HorseTelemetry nearestHorse(float x, float z, float radius) {
        float radiusSquared = Math.max(0f, radius) * Math.max(0f, radius);
        return worldAccess.horses().stream()
            .filter(value -> distanceSquared(x, z, value.x(), value.z()) <= radiusSquared)
            .min(Comparator.comparingDouble(value -> distanceSquared(x, z, value.x(), value.z())))
            .orElse(null);
    }

    private HomesteadStructureType selectedBuildType() {
        return BUILD_TYPES[Math.floorMod(buildTypeIndex, BUILD_TYPES.length)];
    }

    private static Set<UUID> legacyStructureIds(SaveGame state) {
        SaveGame compatibility = new SaveGame(
            SaveGame.CURRENT_VERSION,
            state.worldSeed(),
            state.savedAtEpochMillis(),
            state.worldTime(),
            state.player(),
            state.pushik(),
            state.horses(),
            state.fences(),
            state.harvestedTreeIds()
        );
        Set<UUID> result = new HashSet<>();
        for (SaveGame.StructureData structure : compatibility.structures()) result.add(structure.id());
        return Set.copyOf(result);
    }

    private static String costText(HomesteadStructureType type) {
        return type.buildCost().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> entry.getValue() + " " + entry.getKey().displayName())
            .reduce((left, right) -> left + ", " + right)
            .orElse(I18n.text("common.free"));
    }

    private static String shortName(ItemId item) {
        return item.shortName();
    }

    private void setStatus(String message) {
        homesteadStatus = message;
        statusTimer = 5.5f;
    }

    private static float snap(float value) {
        return Math.round(value / GRID_SIZE) * GRID_SIZE;
    }

    private static float snapHeading(float value) {
        return Math.round(value / 15f) * 15f;
    }

    private static float distanceSquared(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return dx * dx + dz * dz;
    }

    @Override
    public boolean hasUndoableRanchEdit() {
        return undoManager.hasPending();
    }

    @Override
    public String undoLastRanchEdit() {
        dismantleConfirmation.cancel();
        RanchUndoManager.UndoResult result = undoManager.undo(
            session.homestead(),
            session.inventory(),
            (structure, x, z, heading) -> validatePlacement(structure.type(), x, z, structure.id(), false) == null
        );
        String message = switch (result) {
            case PLACEMENT_REVERTED -> I18n.text("homestead.undo.placement");
            case RELOCATION_REVERTED -> I18n.text("homestead.undo.relocation");
            case NOTHING_TO_UNDO -> I18n.text("homestead.undo.none");
            case STRUCTURE_CHANGED -> I18n.text("homestead.undo.changed");
            case INVENTORY_FULL -> I18n.text("homestead.undo.inventory");
            case RESTORE_BLOCKED -> I18n.text("homestead.undo.blocked");
        };
        if (result == RanchUndoManager.UndoResult.PLACEMENT_REVERTED
            || result == RanchUndoManager.UndoResult.RELOCATION_REVERTED) {
            RanchAudio.play(RanchAudio.Cue.UNDO);
            ControllerRumble.pulse(game.inputProfile(), 55, 0.38f);
        }
        setStatus(message);
        return message;
    }

    @Override
    public void saveSession() {
        delegate.pause();
    }

    @Override
    public void resize(int width, int height) {
        delegate.resize(width, height);
    }

    @Override
    public void pause() {
        saveSession();
    }

    @Override
    public void resume() {
        delegate.resume();
        if (displayedLanguage != I18n.language()) {
            displayedLanguage = I18n.language();
            homesteadStatus = I18n.text("homestead.initial");
            statusTimer = 4f;
        }
    }

    @Override
    public void hide() {
        HomesteadInputContext.reset();
        HomesteadActionBus.reset();
        delegate.hide();
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        HomesteadInputContext.reset();
        HomesteadActionBus.reset();
        undoManager.clear();
        dismantleConfirmation.cancel();
        inventoryOverlay.dispose();
        delegate.dispose();
        saveService.clearSaveTransformer(this);
        homesteadRenderer.dispose();
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }

    private record Position(float x, float z) {
    }
}
