// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * libGDX presentation layer over a fixed-step, device-neutral gameplay loop.
 */
final class LivingRanchScreen implements Screen, RanchWorldAccess {
    private static final float PLAYER_WALK_SPEED = 5.2f;
    private static final float PLAYER_RUN_SPEED = 8.4f;
    private static final float WORLD_LIMIT = Terrain.WORLD_HALF_SIZE - 3f;
    private static final float MIN_CAMERA_DISTANCE = 2.2f;

    private final HorseboundGame game;
    private final SaveService saveService;
    private final GameSettings settings;
    private final GameSession session;
    private final GameSimulationLoop simulationLoop;
    private final Random random;

    private final Terrain terrain = new Terrain();
    private final GameModels models = new GameModels();
    private final ModelBatch modelBatch = new ModelBatch();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final String buildLabel = GameplayHudCopy.buildLabel(BuildInfo.current());
    private final PerspectiveCamera camera;
    private final Environment environment = new Environment();
    private final DirectionalLight sun = new DirectionalLight();
    private final RanchCameraCollisionSystem cameraCollisionSystem = new RanchCameraCollisionSystem();
    private final RanchCameraFadeSystem cameraFadeSystem = new RanchCameraFadeSystem();

    private final ModelInstance player = new ModelInstance(models.player);
    private final ModelInstance water = new ModelInstance(models.water);
    private final List<TreeNode> trees = new ArrayList<>();
    private final List<RockNode> rocks = new ArrayList<>();
    private final List<FenceNode> fences = new ArrayList<>();
    private final List<HorseActor> horses = new ArrayList<>();
    private final PushikActor pushik;

    private final Vector3 playerPosition = new Vector3();
    private final Vector3 tmpForward = new Vector3();
    private final Vector3 tmpRight = new Vector3();
    private final Vector3 tmpMove = new Vector3();
    private final Vector3 tmpTarget = new Vector3();
    private final Vector3 tmpDesiredCamera = new Vector3();

    private HorseActor mountedHorse;
    private InputDeviceType activeInputDevice = InputDeviceType.KEYBOARD_MOUSE;
    private List<RanchCameraCollisionSystem.Obstacle> homesteadCameraObstacles = List.of();
    private float cameraYaw = 18f;
    private float cameraPitch = 27f;
    private float cameraDistance = 10f;
    private float resolvedCameraDistance = 10f;
    private float playerFacing;
    private float playerJumpOffset;
    private float playerJumpVelocity;
    private float autosaveTimer;
    private String status = "Explore the meadow, meet a horse, and build your first paddock.";
    private float statusTimer = 8f;
    private boolean returnToMenuRequested;
    private boolean disposed;

    LivingRanchScreen(HorseboundGame game, SaveService saveService, SaveGame initialState) {
        this.game = game;
        this.saveService = saveService;
        this.settings = game.settings();
        this.session = new GameSession(initialState);
        this.simulationLoop = new GameSimulationLoop(
            session,
            new KeyboardMouseInputMapper(settings.mouseSensitivity())
        );
        this.random = new Random(session.worldSeed() ^ 0x4C4956494E47524CL);

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.08f;
        camera.far = 420f;

        environment.set(ColorAttribute.createAmbientLight(0.58f, 0.62f, 0.57f, 1f));
        sun.set(1f, 0.96f, 0.82f, -0.45f, -0.85f, -0.25f);
        environment.add(sun);

        SaveGame.PlayerData savedPlayer = initialState.player();
        playerPosition.set(clampWorld(savedPlayer.x()), 0f, clampWorld(savedPlayer.z()));
        if (Terrain.isInsideLake(playerPosition.x, playerPosition.z)) {
            playerPosition.set(0f, 0f, -18f);
        }
        playerPosition.y = Terrain.heightAt(playerPosition.x, playerPosition.z);
        playerFacing = savedPlayer.facing();

        water.transform.setToTranslation(Terrain.LAKE_X, Terrain.WATER_LEVEL, Terrain.LAKE_Z);
        generateNature();
        applyHarvestedTrees(initialState.harvestedTreeIds());
        if (initialState.horses().isEmpty()) spawnHorses(); else loadHorses(initialState.horses());
        loadFences(initialState.fences());

        SaveGame.PushikData savedPushik = initialState.pushik();
        float px = clampWorld(savedPushik.x());
        float pz = clampWorld(savedPushik.z());
        if (Terrain.isInsideLake(px, pz)) {
            SafeGroundPlacement.Position safe = SafeGroundPlacement.nearest(
                playerPosition.x,
                playerPosition.z,
                playerPosition.x + 2f,
                playerPosition.z + 1f,
                WORLD_LIMIT
            );
            px = safe.x();
            pz = safe.z();
        }
        pushik = new PushikActor(
            new ModelInstance(models.pushik),
            new Vector3(px, Terrain.heightAt(px, pz), pz),
            savedPushik.heading()
        );

        syncTransforms();
        updateLighting();
        updateCamera();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render(float delta) {
        float frameDelta = Math.min(Math.max(0f, delta), FixedStepClock.DEFAULT_MAX_FRAME_SECONDS);
        simulationLoop.advance(frameDelta, this::updateSimulation);
        activeInputDevice = simulationLoop.activeDevice();

        if (returnToMenuRequested) {
            returnToMenuRequested = false;
            saveNow(null);
            simulationLoop.resetInput();
            Gdx.input.setCursorCatched(false);
            game.returnToMenu();
            return;
        }

        updateLighting();
        updateAutosave(frameDelta);
        syncTransforms();
        updateCamera();
        renderWorld();
        renderHud();
        statusTimer = Math.max(0f, statusTimer - frameDelta);
    }

    private void updateSimulation(float fixedDelta, PlayerCommand command) {
        updateCameraInput(command);

        if (command.savePressed()) {
            saveNow("Game saved.");
        }
        if (command.pausePressed()) {
            returnToMenuRequested = true;
            return;
        }

        if (mountedHorse == null) {
            updatePlayer(fixedDelta, command);
        } else {
            updateMountedHorse(fixedDelta, command);
        }
        updateWildHorses(fixedDelta, command);
        updatePushik(fixedDelta);
        handleInteractions(command);
    }

    private void generateNature() {
        for (int i = 0; i < 72; i++) {
            float x = randomRange(-112f, 112f);
            float z = randomRange(-112f, 112f);
            if (Terrain.isInsideLake(x, z) || planarDistance(x, z, 0f, -18f) < 11f) {
                i--;
                continue;
            }
            float scale = randomRange(0.72f, 1.35f);
            ModelInstance tree = new ModelInstance(models.tree);
            isolateMaterials(tree);
            tree.transform.setToTranslation(x, Terrain.heightAt(x, z), z)
                .rotate(Vector3.Y, randomRange(0f, 360f))
                .scale(scale, scale, scale);
            trees.add(new TreeNode(
                i,
                tree,
                x,
                z,
                RanchNatureCameraObstacle.tree(x, z, scale)
            ));
        }

        for (int i = 0; i < 30; i++) {
            float x = randomRange(-110f, 110f);
            float z = randomRange(-110f, 110f);
            if (Terrain.isInsideLake(x, z) || planarDistance(x, z, 0f, -18f) < 8f) {
                i--;
                continue;
            }
            float scale = randomRange(0.55f, 1.75f);
            ModelInstance rock = new ModelInstance(models.rock);
            isolateMaterials(rock);
            rock.transform.setToTranslation(x, Terrain.heightAt(x, z) + 0.35f * scale, z)
                .rotate(Vector3.Y, randomRange(0f, 360f))
                .scale(scale, scale, scale);
            rocks.add(new RockNode(
                i,
                rock,
                x,
                z,
                RanchNatureCameraObstacle.rock(x, z, scale)
            ));
        }
    }

    private void applyHarvestedTrees(List<Integer> harvestedIds) {
        Set<Integer> ids = new HashSet<>(harvestedIds);
        for (TreeNode tree : trees) {
            tree.harvested = ids.contains(tree.id);
        }
    }

    private void spawnHorses() {
        horses.add(newHorse("Ember", 16f, 8f, 25f));
        horses.add(newHorse("Willow", 29f, -9f, 132f));
        horses.add(newHorse("Comet", -8f, 33f, 220f));
        horses.add(newHorse("Hazel", 43f, 28f, 310f));
    }

    private HorseActor newHorse(String name, float x, float z, float heading) {
        UUID id = UUID.nameUUIDFromBytes(
            (session.worldSeed() + ":horse:" + name).getBytes(StandardCharsets.UTF_8)
        );
        HorseActor horse = new HorseActor(
            id,
            name,
            new ModelInstance(models.horse),
            new Vector3(x, Terrain.heightAt(x, z), z),
            heading,
            HorsePersonality.fromIdentity(id),
            HorseRelationship.wild()
        );
        horse.wanderTimer = randomRange(1.5f, 5f);
        return horse;
    }

    private void loadHorses(List<SaveGame.HorseData> data) {
        for (SaveGame.HorseData saved : data) {
            float x = clampWorld(saved.x());
            float z = clampWorld(saved.z());
            if (Terrain.isInsideLake(x, z)) {
                x = 0f;
                z = 0f;
            }
            HorseActor horse = new HorseActor(
                saved.id(),
                saved.name(),
                new ModelInstance(models.horse),
                new Vector3(x, Terrain.heightAt(x, z), z),
                saved.heading(),
                saved.personality(),
                new HorseRelationship(saved.trust(), saved.bond(), saved.fear())
            );
            horse.stamina = MathUtils.clamp(saved.stamina(), 0f, 100f);
            horse.tamed = saved.tamed();
            horse.wanderTimer = randomRange(1.5f, 5f);
            horses.add(horse);
        }
    }

    private void loadFences(List<SaveGame.FenceData> data) {
        for (SaveGame.FenceData saved : data) {
            float x = clampWorld(saved.x());
            float z = clampWorld(saved.z());
            if (Terrain.isInsideLake(x, z)) continue;
            ModelInstance model = new ModelInstance(models.fence);
            model.transform.setToTranslation(x, Terrain.heightAt(x, z), z)
                .rotate(Vector3.Y, saved.heading());
            fences.add(new FenceNode(model, x, z, saved.heading()));
        }
    }

    private void updateCameraInput(PlayerCommand command) {
        cameraYaw += command.lookYaw();
        cameraPitch += command.lookPitch();
        cameraPitch = MathUtils.clamp(cameraPitch, 12f, 62f);
    }

    private void updatePlayer(float dt, PlayerCommand command) {
        getCameraForward(tmpForward);
        tmpRight.set(
    CameraRelativeAxes.rightX(tmpForward.z),
    0f,
    CameraRelativeAxes.rightZ(tmpForward.x)
);
        tmpMove.setZero()
            .mulAdd(tmpForward, command.moveForward())
            .mulAdd(tmpRight, command.moveRight());

        if (!tmpMove.isZero(0.001f)) {
            if (tmpMove.len2() > 1f) tmpMove.nor();
            float speed = command.sprint() ? PLAYER_RUN_SPEED : PLAYER_WALK_SPEED;
            float nx = MathUtils.clamp(playerPosition.x + tmpMove.x * speed * dt, -WORLD_LIMIT, WORLD_LIMIT);
            float nz = MathUtils.clamp(playerPosition.z + tmpMove.z * speed * dt, -WORLD_LIMIT, WORLD_LIMIT);
            if (!Terrain.isInsideLake(nx, nz)) {
                playerPosition.x = nx;
                playerPosition.z = nz;
            }
            playerFacing = MathUtils.atan2(tmpMove.x, tmpMove.z) * MathUtils.radiansToDegrees;
        }

        if (command.jumpPressed() && playerJumpOffset <= 0.001f) {
            playerJumpVelocity = 7.1f;
        }
        playerJumpVelocity -= 18.5f * dt;
        playerJumpOffset += playerJumpVelocity * dt;
        if (playerJumpOffset < 0f) {
            playerJumpOffset = 0f;
            playerJumpVelocity = 0f;
        }
        playerPosition.y = Terrain.heightAt(playerPosition.x, playerPosition.z);
    }

    private void updateMountedHorse(float dt, PlayerCommand command) {
        HorseActor horse = mountedHorse;
        float forward = command.moveForward();
        float targetSpeed = 0f;
        boolean gallop = forward > 0f && command.sprint() && horse.stamina > 1f;

        if (forward > 0f) {
            targetSpeed = forward * (gallop ? 11.5f : 6.4f);
            if (gallop) horse.stamina = Math.max(0f, horse.stamina - 12f * dt);
        } else if (forward < 0f) {
            targetSpeed = forward * 2.8f;
        }
        if (!gallop) {
            horse.stamina = Math.min(100f, horse.stamina + 7f * dt);
        }

        horse.speed = MathUtils.lerp(horse.speed, targetSpeed, Math.min(1f, 3.4f * dt));
        float steeringScale = 35f + Math.min(45f, Math.abs(horse.speed) * 4f);
        horse.heading -= command.moveRight() * steeringScale * dt;

        if (command.jumpPressed() && horse.jumpOffset <= 0.001f) {
            horse.jumpVelocity = 7.4f;
        }
        horse.jumpVelocity -= 19f * dt;
        horse.jumpOffset += horse.jumpVelocity * dt;
        if (horse.jumpOffset < 0f) {
            horse.jumpOffset = 0f;
            horse.jumpVelocity = 0f;
        }

        float nx = MathUtils.clamp(
            horse.position.x + MathUtils.sinDeg(horse.heading) * horse.speed * dt,
            -WORLD_LIMIT,
            WORLD_LIMIT
        );
        float nz = MathUtils.clamp(
            horse.position.z + MathUtils.cosDeg(horse.heading) * horse.speed * dt,
            -WORLD_LIMIT,
            WORLD_LIMIT
        );
        if (!Terrain.isInsideLake(nx, nz)) {
            horse.position.x = nx;
            horse.position.z = nz;
        } else {
            horse.speed *= 0.65f;
        }
        horse.position.y = Terrain.heightAt(horse.position.x, horse.position.z);
        horse.relationship.calm(dt * 0.35f);
        playerPosition.set(horse.position);
        playerFacing = horse.heading;
    }

    private void updateWildHorses(float dt, PlayerCommand command) {
        Vector3 danger = mountedHorse == null ? playerPosition : mountedHorse.position;
        boolean moving = Math.abs(command.moveForward()) > 0.1f || Math.abs(command.moveRight()) > 0.1f;
        boolean rushing = mountedHorse != null || (command.sprint() && moving);

        for (HorseActor horse : horses) {
            if (horse == mountedHorse) continue;

            horse.relationship.calm(dt);
            float distance = planarDistance(horse.position.x, horse.position.z, danger.x, danger.z);
            float desiredSpeed;
            if (!horse.tamed && distance < 8f) {
                horse.heading = MathUtils.atan2(
                    horse.position.x - danger.x,
                    horse.position.z - danger.z
                ) * MathUtils.radiansToDegrees;
                float threat = Math.max(0f, 8f - distance) * 1.7f * dt * (rushing ? 2.2f : 1f);
                horse.relationship.observeThreat(threat, horse.personality);
                desiredSpeed = 5.2f + horse.relationship.fear() * 0.025f;
                horse.wanderTimer = randomRange(2f, 4f);
            } else {
                horse.wanderTimer -= dt;
                if (horse.wanderTimer <= 0f) {
                    horse.heading += randomRange(-75f, 75f);
                    horse.wanderTimer = randomRange(2.2f, 6.5f);
                }
                desiredSpeed = horse.tamed ? 0.45f : 0.75f;
            }

            horse.speed = MathUtils.lerp(horse.speed, desiredSpeed, Math.min(1f, 2.2f * dt));
            float nx = horse.position.x + MathUtils.sinDeg(horse.heading) * horse.speed * dt;
            float nz = horse.position.z + MathUtils.cosDeg(horse.heading) * horse.speed * dt;
            if (Math.abs(nx) > WORLD_LIMIT || Math.abs(nz) > WORLD_LIMIT || Terrain.isInsideLake(nx, nz)) {
                horse.heading += 150f + randomRange(-25f, 25f);
                horse.speed *= 0.3f;
            } else {
                horse.position.x = nx;
                horse.position.z = nz;
            }
            horse.position.y = Terrain.heightAt(horse.position.x, horse.position.z);
        }
    }

    private void updatePushik(float dt) {
        Vector3 target = mountedHorse == null ? playerPosition : mountedHorse.position;
        float distance = planarDistance(pushik.position.x, pushik.position.z, target.x, target.z);

        if (distance > 28f) {
            SafeGroundPlacement.Position safe = SafeGroundPlacement.nearest(
                target.x,
                target.z,
                target.x - 2f,
                target.z - 1f,
                WORLD_LIMIT
            );
            pushik.position.set(safe.x(), Terrain.heightAt(safe.x(), safe.z()), safe.z());
            session.pushikMind().reunited();
            distance = planarDistance(pushik.position.x, pushik.position.z, target.x, target.z);
        }

        session.pushikMind().tick(dt, distance, session.worldTime());
        PushikState state = session.pushikMind().state();
        if ((state == PushikState.FOLLOW || state == PushikState.GREET) && distance > 2.2f) {
            float dx = target.x - pushik.position.x;
            float dz = target.z - pushik.position.z;
            pushik.heading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
            float speed = Math.min(mountedHorse == null ? 5.6f : 9.4f, 1.5f + distance * 0.72f);
            movePushik(speed, dt);
        } else if (state == PushikState.EXPLORE) {
            pushik.wanderTimer -= dt;
            if (pushik.wanderTimer <= 0f) {
                pushik.heading += randomRange(-90f, 90f);
                pushik.wanderTimer = randomRange(2f, 5f);
            }
            movePushik(0.65f, dt);
        }
        pushik.position.y = Terrain.heightAt(pushik.position.x, pushik.position.z);
    }

    private void movePushik(float speed, float dt) {
        float nx = pushik.position.x + MathUtils.sinDeg(pushik.heading) * speed * dt;
        float nz = pushik.position.z + MathUtils.cosDeg(pushik.heading) * speed * dt;
        if (!Terrain.isInsideLake(nx, nz) && Math.abs(nx) < WORLD_LIMIT && Math.abs(nz) < WORLD_LIMIT) {
            pushik.position.x = nx;
            pushik.position.z = nz;
        }
    }

    private void handleInteractions(PlayerCommand command) {
        if (command.interactPressed()) {
            if (planarDistance(playerPosition.x, playerPosition.z, pushik.position.x, pushik.position.z) < 3.1f) {
                session.pushikMind().pet();
                setStatus(
                    "Pushik purrs. Affection " + Math.round(session.pushikMind().affection())
                        + "% — his fluffy black paws are almost silent."
                );
                return;
            }

            HorseActor horse = nearestHorse(4.4f);
            if (horse != null) {
                interactHorse(horse);
                return;
            }

            TreeNode tree = nearestTree(3.7f);
            if (tree != null && !tree.harvested) {
                tree.harvested = true;
                int accepted = session.inventory().add(ItemId.WOOD, 2);
                setStatus("Collected " + accepted + " wood. Fence cost: 2 wood.");
                return;
            }
            setStatus("Nothing close enough to interact with.");
        }

        if (command.mountPressed()) toggleMount();
        if (command.buildPressed()) buildFence();
    }

    private void interactHorse(HorseActor horse) {
        if (!horse.tamed) {
            if (!session.inventory().remove(ItemId.APPLE, 1)) {
                setStatus(horse.name + " watches you carefully. You need an apple.");
                return;
            }
            horse.relationship.feed(horse.personality);
            if (horse.relationship.isReadyToTame()) {
                horse.tamed = true;
                saveNow(null);
                setStatus(
                    horse.name + " trusts you now. Bond " + Math.round(horse.relationship.bond())
                        + "%. Progress saved."
                );
            } else if (horse.relationship.trust() >= 100f) {
                setStatus(
                    horse.name + " trusts you but needs calm. Fear "
                        + Math.round(horse.relationship.fear()) + "% ."
                );
            } else {
                setStatus(
                    horse.name + " accepted the apple. " + horse.personality.displayName()
                        + " | trust " + Math.round(horse.relationship.trust())
                        + "% | fear " + Math.round(horse.relationship.fear()) + "%"
                );
            }
        } else {
            horse.relationship.pet(horse.personality);
            setStatus("You pet " + horse.name + ". Bond " + Math.round(horse.relationship.bond()) + "%.");
        }
    }

    private void toggleMount() {
        if (mountedHorse != null) {
            HorseActor old = mountedHorse;
            old.mounted = false;
            old.speed = 0f;
            mountedHorse = null;
            float preferredX = old.position.x + MathUtils.cosDeg(old.heading) * 2f;
            float preferredZ = old.position.z - MathUtils.sinDeg(old.heading) * 2f;
            SafeGroundPlacement.Position safe = SafeGroundPlacement.nearest(
                old.position.x,
                old.position.z,
                preferredX,
                preferredZ,
                WORLD_LIMIT
            );
            playerPosition.set(safe.x(), Terrain.heightAt(safe.x(), safe.z()), safe.z());
            setStatus("Dismounted " + old.name + ".");
            return;
        }

        HorseActor horse = nearestHorse(4.8f);
        if (horse == null) {
            setStatus("Stand closer to a horse to mount.");
        } else if (!horse.tamed) {
            setStatus(
                horse.name + " is not ready yet. Trust "
                    + Math.round(horse.relationship.trust()) + "%.");
        } else {
            mountedHorse = horse;
            horse.mounted = true;
            horse.speed = 0f;
            setStatus("Mounted " + horse.name + ". Sprint gallops; jump clears obstacles.");
        }
    }

    private void buildFence() {
        if (!session.inventory().remove(ItemId.WOOD, 2)) {
            setStatus("You need 2 wood for a fence segment.");
            return;
        }

        Vector3 origin = mountedHorse == null ? playerPosition : mountedHorse.position;
        getActorForward(tmpForward);
        float x = origin.x + tmpForward.x * 3.3f;
        float z = origin.z + tmpForward.z * 3.3f;
        if (Terrain.isInsideLake(x, z) || Math.abs(x) > WORLD_LIMIT || Math.abs(z) > WORLD_LIMIT) {
            session.inventory().add(ItemId.WOOD, 2);
            setStatus("You cannot build there.");
            return;
        }

        float heading = mountedHorse == null ? playerFacing : mountedHorse.heading;
        ModelInstance fence = new ModelInstance(models.fence);
        fence.transform.setToTranslation(x, Terrain.heightAt(x, z), z).rotate(Vector3.Y, heading);
        fences.add(new FenceNode(fence, x, z, heading));
        setStatus("Fence placed. Ranch construction is persistent.");
    }

    private void updateLighting() {
        float solarAngle = session.worldTime() * 360f - 90f;
        float sunHeight = MathUtils.sinDeg(solarAngle);
        float daylight = MathUtils.clamp((sunHeight + 0.20f) / 1.05f, 0.10f, 1f);
        sun.direction.set(
            -MathUtils.cosDeg(solarAngle) * 0.55f,
            -Math.max(0.18f, sunHeight),
            -0.34f
        ).nor();
        sun.color.set(
            0.95f * daylight + 0.05f,
            0.87f * daylight + 0.08f,
            0.72f * daylight + 0.12f,
            1f
        );
        environment.set(ColorAttribute.createAmbientLight(
            0.10f + daylight * 0.48f,
            0.12f + daylight * 0.50f,
            0.16f + daylight * 0.42f,
            1f
        ));
    }

    private void updateAutosave(float dt) {
        autosaveTimer += dt;
        if (autosaveTimer >= settings.autosaveSeconds()) {
            autosaveTimer = 0f;
            saveNow("Autosaved.");
        }
    }

    private void syncTransforms() {
        if (mountedHorse == null) {
            player.transform.idt()
                .translate(playerPosition.x, playerPosition.y + playerJumpOffset, playerPosition.z)
                .rotate(Vector3.Y, playerFacing);
        } else {
            player.transform.idt()
                .translate(
                    mountedHorse.position.x,
                    mountedHorse.position.y + mountedHorse.jumpOffset + 2.35f,
                    mountedHorse.position.z
                )
                .rotate(Vector3.Y, mountedHorse.heading)
                .scale(0.88f, 0.88f, 0.88f);
        }

        for (HorseActor horse : horses) {
            horse.instance.transform.idt()
                .translate(horse.position.x, horse.position.y + horse.jumpOffset, horse.position.z)
                .rotate(Vector3.Y, horse.heading);
        }
        pushik.instance.transform.idt()
            .translate(pushik.position.x, pushik.position.y, pushik.position.z)
            .rotate(Vector3.Y, pushik.heading);
    }

    private void updateCamera() {
        Vector3 actor = mountedHorse == null ? playerPosition : mountedHorse.position;
        float targetHeight = mountedHorse == null ? 1.45f + playerJumpOffset : 2.15f + mountedHorse.jumpOffset;
        tmpTarget.set(actor.x, actor.y + targetHeight, actor.z);

        float cosPitch = MathUtils.cosDeg(cameraPitch);
        tmpDesiredCamera.set(
            tmpTarget.x - MathUtils.sinDeg(cameraYaw) * cosPitch * cameraDistance,
            tmpTarget.y + MathUtils.sinDeg(cameraPitch) * cameraDistance,
            tmpTarget.z - MathUtils.cosDeg(cameraYaw) * cosPitch * cameraDistance
        );
        float allowedDistance = cameraCollisionSystem.resolveDistance(
            tmpTarget.x,
            tmpTarget.y,
            tmpTarget.z,
            tmpDesiredCamera.x,
            tmpDesiredCamera.y,
            tmpDesiredCamera.z,
            MIN_CAMERA_DISTANCE,
            combinedCameraObstacles(),
            Terrain::heightAt
        );
        float frameDelta = Math.min(
            Math.max(0f, Gdx.graphics.getDeltaTime()),
            FixedStepClock.DEFAULT_MAX_FRAME_SECONDS
        );
        float response = allowedDistance < resolvedCameraDistance ? 16f : 5f;
        resolvedCameraDistance = MathUtils.lerp(
            resolvedCameraDistance,
            allowedDistance,
            Math.min(1f, response * frameDelta)
        );
        float distanceRatio = cameraDistance <= 0.001f ? 1f : resolvedCameraDistance / cameraDistance;
        camera.position.set(
            tmpTarget.x + (tmpDesiredCamera.x - tmpTarget.x) * distanceRatio,
            tmpTarget.y + (tmpDesiredCamera.y - tmpTarget.y) * distanceRatio,
            tmpTarget.z + (tmpDesiredCamera.z - tmpTarget.z) * distanceRatio
        );
        camera.up.set(Vector3.Y);
        camera.lookAt(tmpTarget);
        camera.update();
    }

    private List<RanchCameraCollisionSystem.Obstacle> combinedCameraObstacles() {
        int capacity = homesteadCameraObstacles.size() + trees.size() + rocks.size();
        List<RanchCameraCollisionSystem.Obstacle> result = new ArrayList<>(capacity);
        result.addAll(homesteadCameraObstacles);
        for (TreeNode tree : trees) if (!tree.harvested) result.add(tree.obstacle);
        for (RockNode rock : rocks) result.add(rock.obstacle);
        return List.copyOf(result);
    }

    private float fadeAlpha(RanchCameraCollisionSystem.Obstacle obstacle) {
        return cameraFadeSystem.alphaFor(
            obstacle,
            Terrain.heightAt(obstacle.x(), obstacle.z()),
            tmpTarget.x,
            tmpTarget.y,
            tmpTarget.z,
            camera.position.x,
            camera.position.y,
            camera.position.z
        );
    }

    private static void isolateMaterials(ModelInstance instance) {
        Map<Material, Material> copies = new IdentityHashMap<>();
        for (Node node : instance.nodes) isolateNodeMaterials(node, copies);
        instance.materials.clear();
        for (Material material : copies.values()) instance.materials.add(material);
    }

    private static void isolateNodeMaterials(Node node, Map<Material, Material> copies) {
        for (NodePart part : node.parts) {
            Material source = part.material;
            part.material = copies.computeIfAbsent(source, Material::new);
        }
        for (Node child : node.getChildren()) isolateNodeMaterials(child, copies);
    }

    private static void applyOpacity(ModelInstance instance, float alpha) {
        float safeAlpha = Float.isFinite(alpha) ? MathUtils.clamp(alpha, RanchCameraFadeSystem.MIN_ALPHA, 1f) : 1f;
        for (Material material : instance.materials) {
            ColorAttribute diffuse = (ColorAttribute) material.get(ColorAttribute.Diffuse);
            if (diffuse != null) diffuse.color.a = safeAlpha;
            if (safeAlpha < 0.999f) {
                material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, safeAlpha));
            } else {
                material.remove(BlendingAttribute.Type);
            }
        }
    }

    private void renderWorld() {
        float solarAngle = session.worldTime() * 360f - 90f;
        float daylight = MathUtils.clamp(
            (MathUtils.sinDeg(solarAngle) + 0.20f) / 1.05f,
            0.08f,
            1f
        );
        Gdx.gl.glClearColor(
            0.035f + daylight * 0.39f,
            0.055f + daylight * 0.58f,
            0.09f + daylight * 0.68f,
            1f
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);
        modelBatch.render(terrain.instance, environment);
        modelBatch.render(water, environment);
        for (TreeNode tree : trees) {
            if (tree.harvested) continue;
            applyOpacity(tree.instance, fadeAlpha(tree.obstacle));
            modelBatch.render(tree.instance, environment);
        }
        for (RockNode rock : rocks) {
            applyOpacity(rock.instance, fadeAlpha(rock.obstacle));
            modelBatch.render(rock.instance, environment);
        }
        for (FenceNode fence : fences) modelBatch.render(fence.instance, environment);
        for (HorseActor horse : horses) modelBatch.render(horse.instance, environment);
        modelBatch.render(pushik.instance, environment);
        modelBatch.render(player, environment);
        modelBatch.end();
    }

    private void renderHud() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float ui = UiScale.effective(width, height, game.settings().uiScale());
        float geometry = Math.min(ui, 1.18f);
        float left = 18f * geometry;
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        spriteBatch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1f * ui);
        font.draw(spriteBatch, buildLabel, left, height - 18f * geometry);
        font.getData().setScale(0.76f * ui);
        font.setColor(new Color(0.88f, 0.91f, 0.86f, 1f));
        font.draw(spriteBatch, inputHint(), left, height - 42f * geometry);
        font.draw(
            spriteBatch,
            "Wood " + session.inventory().count(ItemId.WOOD)
                + " | Apples " + session.inventory().count(ItemId.APPLE)
                + " | Seed " + session.worldSeed()
                + " | Save " + saveService.activeSlot()
                + " | Input " + activeInputDevice,
            left,
            height - 64f * geometry
        );

        HorseActor focus = mountedHorse != null ? mountedHorse : nearestHorse(7f);
        if (focus != null) {
            font.setColor(new Color(1f, 0.87f, 0.57f, 1f));
            font.draw(
                spriteBatch,
                focus.name + " | " + focus.personality.displayName()
                    + " | trust " + Math.round(focus.relationship.trust())
                    + "% | bond " + Math.round(focus.relationship.bond())
                    + "% | fear " + Math.round(focus.relationship.fear())
                    + "%" + (focus.tamed ? " | tamed" : " | wild")
                    + (focus == mountedHorse
                    ? " | stamina " + Math.round(focus.stamina)
                        + "% | speed " + String.format(Locale.ROOT, "%.1f", Math.abs(focus.speed))
                    : ""),
                left,
                height - 86f * geometry
            );
        }

        if (planarDistance(playerPosition.x, playerPosition.z, pushik.position.x, pushik.position.z) < 9f) {
            font.setColor(new Color(0.82f, 0.82f, 0.86f, 1f));
            font.draw(
                spriteBatch,
                "Pushik: " + session.pushikMind().state()
                    + " | affection " + Math.round(session.pushikMind().affection())
                    + "% | fluffy paws: silent",
                left,
                52f * geometry
            );
        }
        if (statusTimer > 0f) {
            font.setColor(new Color(1f, 0.96f, 0.78f, 1f));
            font.draw(spriteBatch, status, left, 82f * geometry);
        }

        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.getData().setScale(0.68f * ui);
        font.draw(
            spriteBatch,
            "Created by Dimash Janibekov (DizZyZ7) | (c) 2026 All rights reserved",
            Math.max(left, width - 430f * geometry),
            20f * geometry
        );
        spriteBatch.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private String inputHint() {
        return GameplayHudCopy.inputHint(activeInputDevice, InputProfileContext.current());
    }

    private HorseActor nearestHorse(float radius) {
        HorseActor nearest = null;
        float best = radius;
        Vector3 origin = mountedHorse == null ? playerPosition : mountedHorse.position;
        for (HorseActor horse : horses) {
            if (horse == mountedHorse) continue;
            float distance = planarDistance(origin.x, origin.z, horse.position.x, horse.position.z);
            if (distance < best) {
                best = distance;
                nearest = horse;
            }
        }
        return nearest;
    }

    private TreeNode nearestTree(float radius) {
        TreeNode nearest = null;
        float best = radius;
        Vector3 origin = mountedHorse == null ? playerPosition : mountedHorse.position;
        for (TreeNode tree : trees) {
            if (tree.harvested) continue;
            float distance = planarDistance(origin.x, origin.z, tree.x, tree.z);
            if (distance < best) {
                best = distance;
                nearest = tree;
            }
        }
        return nearest;
    }

    private void saveNow(String message) {
        try {
            saveService.save(captureSave());
            if (message != null) setStatus(message);
        } catch (SaveRepository.SaveException ex) {
            Gdx.app.error("HORSEBOUND", "Save failed", ex);
            setStatus("Save failed. Current session remains playable.");
        }
    }

    private SaveGame captureSave() {
        List<SaveGame.HorseData> horseData = new ArrayList<>(horses.size());
        for (HorseActor horse : horses) {
            horseData.add(new SaveGame.HorseData(
                horse.id,
                horse.name,
                horse.position.x,
                horse.position.z,
                horse.heading,
                horse.relationship.trust(),
                horse.stamina,
                horse.tamed,
                horse.personality,
                horse.relationship.bond(),
                horse.relationship.fear()
            ));
        }

        List<SaveGame.FenceData> fenceData = new ArrayList<>(fences.size());
        for (FenceNode fence : fences) {
            fenceData.add(new SaveGame.FenceData(fence.x, fence.z, fence.heading));
        }

        List<Integer> harvested = new ArrayList<>();
        for (TreeNode tree : trees) {
            if (tree.harvested) harvested.add(tree.id);
        }

        List<SaveGame.ItemStackData> inventoryData = session.inventory().snapshot().entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> new SaveGame.ItemStackData(entry.getKey().name(), entry.getValue()))
            .toList();

        return new SaveGame(
            SaveGame.CURRENT_VERSION,
            session.worldSeed(),
            System.currentTimeMillis(),
            session.worldTime(),
            new SaveGame.PlayerData(
                playerPosition.x,
                playerPosition.z,
                playerFacing,
                session.inventory().count(ItemId.WOOD),
                session.inventory().count(ItemId.APPLE),
                inventoryData
            ),
            new SaveGame.PushikData(
                pushik.position.x,
                pushik.position.z,
                pushik.heading,
                session.pushikMind().affection(),
                session.pushikMind().state()
            ),
            horseData,
            fenceData,
            harvested
        );
    }

    private void getCameraForward(Vector3 out) {
        out.set(MathUtils.sinDeg(cameraYaw), 0f, MathUtils.cosDeg(cameraYaw)).nor();
    }

    private void getActorForward(Vector3 out) {
        float yaw = mountedHorse == null ? playerFacing : mountedHorse.heading;
        out.set(MathUtils.sinDeg(yaw), 0f, MathUtils.cosDeg(yaw)).nor();
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static float planarDistance(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    private static float clampWorld(float value) {
        if (!Float.isFinite(value)) return 0f;
        return MathUtils.clamp(value, -WORLD_LIMIT, WORLD_LIMIT);
    }

    private void setStatus(String message) {
        status = message;
        statusTimer = 5f;
    }

    @Override
    public PerspectiveCamera camera() {
        return camera;
    }

    @Override
    public ActorPose actorPose() {
        if (mountedHorse != null) {
            return new ActorPose(
                mountedHorse.position.x,
                mountedHorse.position.z,
                mountedHorse.heading,
                true
            );
        }
        return new ActorPose(playerPosition.x, playerPosition.z, playerFacing, false);
    }

    @Override
    public List<HorseTelemetry> horses() {
        List<HorseTelemetry> result = new ArrayList<>(horses.size());
        for (HorseActor horse : horses) {
            result.add(new HorseTelemetry(
                horse.id,
                horse.position.x,
                horse.position.z,
                horse.speed,
                horse == mountedHorse,
                horse.tamed
            ));
        }
        return List.copyOf(result);
    }

    @Override
    public void setActorPosition(float x, float z) {
        if (!Float.isFinite(x) || !Float.isFinite(z)) return;
        float safeX = clampWorld(x);
        float safeZ = clampWorld(z);
        if (mountedHorse != null) {
            setHorseCoordinates(mountedHorse, safeX, safeZ);
            playerPosition.set(mountedHorse.position);
            playerFacing = mountedHorse.heading;
            return;
        }
        playerPosition.set(safeX, Terrain.heightAt(safeX, safeZ), safeZ);
    }

    @Override
    public boolean setHorsePosition(UUID horseId, float x, float z) {
        if (horseId == null || !Float.isFinite(x) || !Float.isFinite(z)) return false;
        float safeX = clampWorld(x);
        float safeZ = clampWorld(z);
        for (HorseActor horse : horses) {
            if (!horse.id.equals(horseId)) continue;
            setHorseCoordinates(horse, safeX, safeZ);
            if (horse == mountedHorse) {
                playerPosition.set(horse.position);
                playerFacing = horse.heading;
            }
            return true;
        }
        return false;
    }

    @Override
    public void setCameraObstacles(List<RanchCameraCollisionSystem.Obstacle> obstacles) {
        if (obstacles == null || obstacles.isEmpty()) {
            homesteadCameraObstacles = List.of();
            return;
        }
        List<RanchCameraCollisionSystem.Obstacle> safe = new ArrayList<>(obstacles.size());
        for (RanchCameraCollisionSystem.Obstacle obstacle : obstacles) {
            if (obstacle != null && obstacle.radius() > 0f && obstacle.height() > 0f) safe.add(obstacle);
        }
        homesteadCameraObstacles = List.copyOf(safe);
    }

    @Override
    public boolean isNaturePlacementBlocked(float x, float z, float radius) {
        if (!Float.isFinite(x) || !Float.isFinite(z) || !Float.isFinite(radius)) return true;
        float safeRadius = Math.max(0f, radius);
        for (TreeNode tree : trees) {
            if (tree.harvested) continue;
            if (RanchPlacementCollision.overlaps(
                x, z, safeRadius, tree.x, tree.z, tree.obstacle.radius(), 0f
            )) return true;
        }
        for (RockNode rock : rocks) {
            if (RanchPlacementCollision.overlaps(
                x, z, safeRadius, rock.x, rock.z, rock.obstacle.radius(), 0f
            )) return true;
        }
        return false;
    }

    private static void setHorseCoordinates(HorseActor horse, float x, float z) {
        horse.position.set(x, Terrain.heightAt(x, z), z);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
        simulationLoop.resetInput();
        saveNow(null);
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        simulationLoop.resetInput();
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;
        simulationLoop.resetInput();
        saveNow(null);
        Gdx.input.setCursorCatched(false);
        modelBatch.dispose();
        spriteBatch.dispose();
        font.dispose();
        terrain.dispose();
        models.dispose();
    }

    private static final class TreeNode {
        final int id;
        final ModelInstance instance;
        final float x;
        final float z;
        final RanchCameraCollisionSystem.Obstacle obstacle;
        boolean harvested;

        TreeNode(
            int id,
            ModelInstance instance,
            float x,
            float z,
            RanchCameraCollisionSystem.Obstacle obstacle
        ) {
            this.id = id;
            this.instance = instance;
            this.x = x;
            this.z = z;
            this.obstacle = obstacle;
        }
    }

    private static final class RockNode {
        final int id;
        final ModelInstance instance;
        final float x;
        final float z;
        final RanchCameraCollisionSystem.Obstacle obstacle;

        RockNode(
            int id,
            ModelInstance instance,
            float x,
            float z,
            RanchCameraCollisionSystem.Obstacle obstacle
        ) {
            this.id = id;
            this.instance = instance;
            this.x = x;
            this.z = z;
            this.obstacle = obstacle;
        }
    }

    private static final class FenceNode {
        final ModelInstance instance;
        final float x;
        final float z;
        final float heading;

        FenceNode(ModelInstance instance, float x, float z, float heading) {
            this.instance = instance;
            this.x = x;
            this.z = z;
            this.heading = heading;
        }
    }

    private static final class HorseActor {
        final UUID id;
        final String name;
        final ModelInstance instance;
        final Vector3 position;
        final HorsePersonality personality;
        final HorseRelationship relationship;
        float heading;
        float stamina = 100f;
        float speed;
        float wanderTimer;
        float jumpOffset;
        float jumpVelocity;
        boolean tamed;
        boolean mounted;

        HorseActor(
            UUID id,
            String name,
            ModelInstance instance,
            Vector3 position,
            float heading,
            HorsePersonality personality,
            HorseRelationship relationship
        ) {
            this.id = id;
            this.name = name;
            this.instance = instance;
            this.position = position;
            this.heading = heading;
            this.personality = personality;
            this.relationship = relationship;
        }
    }

    private static final class PushikActor {
        final ModelInstance instance;
        final Vector3 position;
        float heading;
        float wanderTimer = 2f;

        PushikActor(ModelInstance instance, Vector3 position, float heading) {
            this.instance = instance;
            this.position = position;
            this.heading = heading;
        }
    }
}
