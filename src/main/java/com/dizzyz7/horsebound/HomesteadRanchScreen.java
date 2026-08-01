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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Live 0.5 Homestead presentation layered over the validated legacy ranch renderer.
 * Domain truth remains in the captured GameSession and SaveGame v4.
 */
final class HomesteadRanchScreen implements RanchSessionScreen {
    private static final HomesteadStructureType[] BUILD_TYPES = HomesteadStructureType.values();
    private static final float PLACEMENT_DISTANCE = 4.4f;
    private static final float GRID_SIZE = 0.5f;
    private static final float INTERACTION_RADIUS = 3.8f;

    private final HorseboundGame game;
    private final SaveService saveService;
    private final LivingRanchScreen delegate;
    private final GameSession session;
    private final LivingRanchTelemetryAdapter telemetry;
    private final HomesteadRuntimeInput runtimeInput = new HomesteadRuntimeInput();
    private final HomesteadRenderer homesteadRenderer = new HomesteadRenderer();
    private final HorseCareSystem careSystem = new HorseCareSystem();
    private final FixedStepClock careClock = new FixedStepClock();
    private final EnumMap<HomesteadStructureType, Float> collisionRadius = new EnumMap<>(HomesteadStructureType.class);
    private final Map<UUID, HorseNeeds> horseNeeds = new LinkedHashMap<>();
    private final Set<UUID> hiddenLegacyStructureIds;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();

    private HomesteadRenderer.PlacementPreview placement;
    private int buildTypeIndex;
    private float placementHeading;
    private boolean buildMode;
    private boolean disposed;
    private String homesteadStatus = "Select hotbar slots with 1–8 or D-pad Left/Right.";
    private float statusTimer = 7f;
    private float careFeedbackCooldown;

    HomesteadRanchScreen(
        HorseboundGame game,
        SaveService saveService,
        SaveGame initialState
    ) {
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
        session = captured;
        telemetry = new LivingRanchTelemetryAdapter(delegate);

        for (SaveGame.HorseData horse : initialState.horses()) horseNeeds.put(horse.id(), horse.needs());
        hiddenLegacyStructureIds = legacyStructureIds(initialState);

        collisionRadius.put(HomesteadStructureType.FENCE, 1.45f);
        collisionRadius.put(HomesteadStructureType.GATE, 1.75f);
        collisionRadius.put(HomesteadStructureType.FEEDER, 1.35f);
        collisionRadius.put(HomesteadStructureType.WATER_TROUGH, 1.55f);
        collisionRadius.put(HomesteadStructureType.HAY_STORAGE, 2.10f);
        collisionRadius.put(HomesteadStructureType.CHEST, 1.05f);
        collisionRadius.put(HomesteadStructureType.STALL, 2.65f);

        saveService.setSaveTransformer(this, this::enrichSave);
    }

    @Override
    public void show() {
        HomesteadActionBus.reset();
        HomesteadInputContext.configure(true, false, false);
        delegate.show();
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(Math.max(0f, delta), FixedStepClock.DEFAULT_MAX_FRAME_SECONDS);
        LivingRanchTelemetryAdapter.ActorPose pose = telemetry.actorPose();
        PlacedStructure nearbyStorage = nearestResourceStructure(pose.x(), pose.z(), INTERACTION_RADIUS);
        HomesteadInputContext.configure(true, nearbyStorage != null, buildMode);

        HomesteadRuntimeInput.InputResult input = runtimeInput.sample(buildMode);
        applySelectionInput(input);
        placement = buildMode ? calculatePlacement(telemetry.actorPose()) : null;

        try {
            delegate.render(delta);
        } finally {
            HomesteadInputContext.reset();
        }
        if (game.getScreen() != this) return;

        handleSemanticActions(nearbyStorage);
        careClock.advance(frameDelta, this::updateHorseCare);
        placement = buildMode ? calculatePlacement(telemetry.actorPose()) : null;
        homesteadRenderer.render(
            telemetry.camera(),
            session.homestead(),
            hiddenLegacyStructureIds,
            placement
        );
        renderHomesteadHud();

        statusTimer = Math.max(0f, statusTimer - frameDelta);
        careFeedbackCooldown = Math.max(0f, careFeedbackCooldown - frameDelta);
    }

    private void applySelectionInput(HomesteadRuntimeInput.InputResult input) {
        if (input.directSlot() >= 0) session.hotbar().select(input.directSlot());
        if (!buildMode && input.hotbarDelta() != 0) session.hotbar().cycle(input.hotbarDelta());
        if (input.buildTypeDelta() != 0) {
            buildTypeIndex = Math.floorMod(buildTypeIndex + input.buildTypeDelta(), BUILD_TYPES.length);
            setStatus("Build blueprint: " + selectedBuildType().displayName() + ".");
        }
        if (input.rotationDelta() != 0) {
            placementHeading = snapHeading(placementHeading + input.rotationDelta() * 15f);
        }
    }

    private void handleSemanticActions(PlacedStructure nearbyStorage) {
        if (HomesteadActionBus.consumeCancel()) {
            buildMode = false;
            placement = null;
            setStatus("Build placement cancelled.");
        }
        if (HomesteadActionBus.consumeBuild()) {
            if (!buildMode) {
                buildMode = true;
                placementHeading = snapHeading(telemetry.actorPose().heading());
                placement = calculatePlacement(telemetry.actorPose());
                setStatus("Build mode: " + selectedBuildType().displayName() + ". B/L1 confirms; Esc/B cancels.");
            } else {
                confirmPlacement();
            }
        }
        if (HomesteadActionBus.consumeInteract()) depositSelectedResource(nearbyStorage);
    }

    private void confirmPlacement() {
        if (placement == null || !placement.valid()) {
            setStatus(placement == null ? "No placement preview." : placement.reason());
            return;
        }
        HomesteadStructureType type = selectedBuildType();
        if (session.homestead().place(
            type,
            placement.x(),
            placement.z(),
            placement.heading(),
            session.inventory()
        ).isEmpty()) {
            setStatus("Missing materials: " + costText(type) + ".");
            return;
        }
        ControllerRumble.pulse(game.inputProfile(), 65, 0.42f);
        setStatus(type.displayName() + " placed. Cost: " + costText(type) + ".");
    }

    private void depositSelectedResource(PlacedStructure structure) {
        if (structure == null || !structure.type().storesResource()) return;
        ItemId selected = session.hotbar().selectedItem();
        ItemId accepted = structure.type().acceptedResource();
        if (selected != accepted) {
            setStatus(structure.type().displayName() + " accepts " + accepted.displayName() + ".");
            return;
        }
        int acceptedUnits = session.homestead().depositOneResource(structure.id(), session.inventory());
        if (acceptedUnits <= 0) {
            String reason = session.inventory().count(accepted) <= 0
                ? "No " + accepted.displayName() + " in inventory."
                : structure.type().displayName() + " is full.";
            setStatus(reason);
            return;
        }
        ControllerRumble.pulse(game.inputProfile(), 45, 0.28f);
        setStatus(
            "Deposited " + accepted.displayName() + ": "
                + structure.storedUnits() + "/" + structure.type().storageCapacity() + " units."
        );
    }

    private HomesteadRenderer.PlacementPreview calculatePlacement(
        LivingRanchTelemetryAdapter.ActorPose pose
    ) {
        HomesteadStructureType type = selectedBuildType();
        float x = snap(pose.x() + MathUtils.sinDeg(pose.heading()) * PLACEMENT_DISTANCE);
        float z = snap(pose.z() + MathUtils.cosDeg(pose.heading()) * PLACEMENT_DISTANCE);
        float y = Terrain.heightAt(x, z);
        String reason = validatePlacement(type, x, z);
        return new HomesteadRenderer.PlacementPreview(
            true,
            type,
            x,
            y,
            z,
            snapHeading(placementHeading),
            reason == null,
            reason == null ? "Ready" : reason
        );
    }

    private String validatePlacement(HomesteadStructureType type, float x, float z) {
        float limit = Terrain.WORLD_HALF_SIZE - 4f;
        if (Math.abs(x) > limit || Math.abs(z) > limit) return "Too close to the world boundary.";
        if (Terrain.isInsideLake(x, z)) return "Structures cannot be placed in the lake.";

        float center = Terrain.heightAt(x, z);
        float maximumSlope = 0f;
        float sample = type == HomesteadStructureType.STALL ? 1.6f : 0.9f;
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x + sample, z)));
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x - sample, z)));
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x, z + sample)));
        maximumSlope = Math.max(maximumSlope, Math.abs(center - Terrain.heightAt(x, z - sample)));
        if (maximumSlope > 0.85f) return "Ground is too steep for this structure.";

        float radius = collisionRadius.get(type);
        for (PlacedStructure existing : session.homestead().structures()) {
            float required = radius + collisionRadius.get(existing.type());
            if (distanceSquared(x, z, existing.x(), existing.z()) < required * required) {
                return "Placement overlaps " + existing.type().displayName() + ".";
            }
        }
        for (Map.Entry<ItemId, Integer> cost : type.buildCost().entrySet()) {
            if (!session.inventory().has(cost.getKey(), cost.getValue())) {
                return "Missing materials: " + costText(type) + ".";
            }
        }
        return null;
    }

    private void updateHorseCare(float fixedDelta) {
        for (LivingRanchTelemetryAdapter.HorseTelemetry horse : telemetry.horses()) {
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
                String action = result.fed() ? "ate hay" : result.watered() ? "drank water" : "is resting in a stall";
                setStatus("A ranch horse " + action + " automatically.");
                careFeedbackCooldown = 4f;
            }
        }
    }

    private SaveGame enrichSave(SaveGame base) {
        List<SaveGame.HorseData> enrichedHorses = new ArrayList<>(base.horses().size());
        for (SaveGame.HorseData horse : base.horses()) {
            HorseNeeds needs = horseNeeds.getOrDefault(horse.id(), horse.needs());
            enrichedHorses.add(new SaveGame.HorseData(
                horse.id(),
                horse.name(),
                horse.x(),
                horse.z(),
                horse.heading(),
                horse.trust(),
                horse.stamina(),
                horse.tamed(),
                horse.personality(),
                horse.bond(),
                horse.fear(),
                needs.hunger(),
                needs.thirst(),
                needs.energy()
            ));
        }

        LinkedHashMap<UUID, SaveGame.StructureData> structures = new LinkedHashMap<>();
        for (SaveGame.StructureData structure : session.homestead().toSaveData()) {
            structures.put(structure.id(), structure);
        }
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
        for (SaveGame.StructureData structure : compatibility.structures()) {
            structures.putIfAbsent(structure.id(), structure);
        }

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
            font.getData().setScale(0.62f * ui);
            font.setColor(Color.WHITE);
            font.draw(batch, Integer.toString(i + 1), x + 4f * geometry, startY + slotSize - 4f * geometry);
            if (item != null) {
                font.getData().setScale(0.53f * ui);
                font.draw(batch, shortName(item), x + 5f * geometry, startY + 22f * geometry);
                font.setColor(new Color(1f, 0.88f, 0.55f, 1f));
                font.draw(batch, Integer.toString(session.inventory().count(item)), x + 5f * geometry, startY + 9f * geometry);
            }
        }

        LivingRanchTelemetryAdapter.ActorPose pose = telemetry.actorPose();
        LivingRanchTelemetryAdapter.HorseTelemetry nearest = nearestHorse(pose.x(), pose.z());
        if (nearest != null) {
            HorseNeeds needs = horseNeeds.getOrDefault(nearest.id(), HorseNeeds.healthy());
            font.getData().setScale(0.68f * ui);
            font.setColor(new Color(0.94f, 0.90f, 0.69f, 1f));
            font.draw(
                batch,
                "Horse care | hunger " + Math.round(needs.hunger())
                    + "% | thirst " + Math.round(needs.thirst())
                    + "% | energy " + Math.round(needs.energy()) + "%",
                16f * geometry,
                startY + slotSize + 25f * geometry
            );
        }

        if (buildMode && placement != null) {
            font.getData().setScale(0.70f * ui);
            font.setColor(placement.valid() ? new Color(0.62f, 1f, 0.68f, 1f) : new Color(1f, 0.58f, 0.50f, 1f));
            font.draw(
                batch,
                "BUILD: " + placement.type().displayName().toUpperCase(Locale.ROOT)
                    + " | cost " + costText(placement.type())
                    + " | [ / ] type | R rotate | B/L1 place | Esc/B cancel"
                    + (placement.valid() ? "" : " | " + placement.reason()),
                16f * geometry,
                height - 70f * geometry
            );
        }
        if (statusTimer > 0f) {
            font.getData().setScale(0.67f * ui);
            font.setColor(new Color(1f, 0.94f, 0.73f, 1f));
            font.draw(batch, homesteadStatus, 16f * geometry, startY + slotSize + 48f * geometry);
        }
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private PlacedStructure nearestResourceStructure(float x, float z, float radius) {
        return session.homestead().structures().stream()
            .filter(value -> value.type().storesResource())
            .filter(value -> distanceSquared(x, z, value.x(), value.z()) <= radius * radius)
            .min(Comparator.comparingDouble(value -> distanceSquared(x, z, value.x(), value.z())))
            .orElse(null);
    }

    private LivingRanchTelemetryAdapter.HorseTelemetry nearestHorse(float x, float z) {
        return telemetry.horses().stream()
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
            .orElse("free");
    }

    private static String shortName(ItemId item) {
        return switch (item) {
            case WATER_BUCKET -> "WATER";
            default -> item.name();
        };
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
        delegate.dispose();
        saveService.clearSaveTransformer(this);
        homesteadRenderer.dispose();
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
