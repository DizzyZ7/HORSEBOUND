// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

final class WorldScreen implements Screen {
    private static final float PLAYER_WALK_SPEED = 5.2f;
    private static final float PLAYER_RUN_SPEED = 8.4f;
    private static final float WORLD_LIMIT = Terrain.WORLD_HALF_SIZE - 3f;

    private final HorseboundGame game;
    private final SaveService saveService;
    private final GameSettings settings;
    private final long worldSeed;
    private final Random random;

    private final Terrain terrain = new Terrain();
    private final GameModels models = new GameModels();
    private final ModelBatch modelBatch = new ModelBatch();
    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final PerspectiveCamera camera;
    private final Environment environment = new Environment();
    private final DirectionalLight sun = new DirectionalLight();

    private final ModelInstance player = new ModelInstance(models.player);
    private final ModelInstance water = new ModelInstance(models.water);
    private final List<TreeNode> trees = new ArrayList<>();
    private final List<ModelInstance> rocks = new ArrayList<>();
    private final List<FenceNode> fences = new ArrayList<>();
    private final List<Horse> horses = new ArrayList<>();
    private final Pushik pushik;

    private final Vector3 playerPosition = new Vector3();
    private final Vector3 tmpForward = new Vector3();
    private final Vector3 tmpRight = new Vector3();
    private final Vector3 tmpMove = new Vector3();
    private final Vector3 tmpTarget = new Vector3();

    private Horse mountedHorse;
    private float cameraYaw = 18f;
    private float cameraPitch = 27f;
    private float cameraDistance = 10f;
    private float playerFacing;
    private float playerJumpOffset;
    private float playerJumpVelocity;
    private float worldTime;
    private float autosaveTimer;
    private int wood;
    private int apples;
    private String status = "Find a wild horse, earn its trust, and build your first paddock.";
    private float statusTimer = 8f;
    private boolean disposed;

    WorldScreen(HorseboundGame game, SaveService saveService, SaveGame initialState) {
        this.game = game;
        this.saveService = saveService;
        this.settings = game.settings();
        this.worldSeed = initialState.worldSeed();
        this.random = new Random(worldSeed ^ 0x484F525345424F55L);

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.08f;
        camera.far = 420f;

        environment.set(ColorAttribute.createAmbientLight(0.58f, 0.62f, 0.57f, 1f));
        sun.set(1f, 0.96f, 0.82f, -0.45f, -0.85f, -0.25f);
        environment.add(sun);

        SaveGame.PlayerData savedPlayer = initialState.player();
        playerPosition.set(
            clampWorld(savedPlayer.x()),
            0f,
            clampWorld(savedPlayer.z())
        );
        if (Terrain.isInsideLake(playerPosition.x, playerPosition.z)) {
            playerPosition.set(0f, 0f, -18f);
        }
        playerPosition.y = Terrain.heightAt(playerPosition.x, playerPosition.z);
        playerFacing = savedPlayer.facing();
        wood = Math.max(0, savedPlayer.wood());
        apples = Math.max(0, savedPlayer.apples());
        worldTime = normalizeTime(initialState.worldTime());

        water.transform.setToTranslation(Terrain.LAKE_X, Terrain.WATER_LEVEL, Terrain.LAKE_Z);

        generateNature();
        applyHarvestedTrees(initialState.harvestedTreeIds());

        if (initialState.horses().isEmpty()) {
            spawnHorses();
        } else {
            loadHorses(initialState.horses());
        }
        loadFences(initialState.fences());

        SaveGame.PushikData savedPushik = initialState.pushik();
        float pushikX = clampWorld(savedPushik.x());
        float pushikZ = clampWorld(savedPushik.z());
        if (Terrain.isInsideLake(pushikX, pushikZ)) {
            pushikX = playerPosition.x + 2f;
            pushikZ = playerPosition.z + 1f;
        }
        pushik = new Pushik(
            new ModelInstance(models.pushik),
            new Vector3(pushikX, Terrain.heightAt(pushikX, pushikZ), pushikZ)
        );
        pushik.heading = savedPushik.heading();

        syncTransforms();
        updateCamera();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 0.05f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            saveNow("Game saved.");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            saveNow(null);
            Gdx.input.setCursorCatched(false);
            game.returnToMenu();
            return;
        }

        updateCameraInput();
        if (mountedHorse == null) {
            updatePlayer(dt);
        } else {
            updateMountedHorse(dt);
        }
        updateWildHorses(dt);
        updatePushik(dt);
        handleInteractions();
        updateDayNight(dt);
        updateAutosave(dt);
        syncTransforms();
        updateCamera();
        renderWorld();
        renderHud();

        if (statusTimer > 0f) {
            statusTimer -= dt;
        }
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
            tree.transform.setToTranslation(x, Terrain.heightAt(x, z), z)
                .rotate(Vector3.Y, randomRange(0f, 360f))
                .scale(scale, scale, scale);
            trees.add(new TreeNode(i, tree, x, z));
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
            rock.transform.setToTranslation(x, Terrain.heightAt(x, z) + 0.35f * scale, z)
                .rotate(Vector3.Y, randomRange(0f, 360f))
                .scale(scale, scale, scale);
            rocks.add(rock);
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
        for (Horse horse : horses) {
            horse.wanderTimer = randomRange(1.5f, 5f);
        }
    }

    private Horse newHorse(String name, float x, float z, float heading) {
        UUID id = UUID.nameUUIDFromBytes((worldSeed + ":horse:" + name).getBytes(StandardCharsets.UTF_8));
        return new Horse(
            id,
            name,
            new ModelInstance(models.horse),
            new Vector3(x, Terrain.heightAt(x, z), z),
            heading
        );
    }

    private void loadHorses(List<SaveGame.HorseData> savedHorses) {
        for (SaveGame.HorseData data : savedHorses) {
            float x = clampWorld(data.x());
            float z = clampWorld(data.z());
            if (Terrain.isInsideLake(x, z)) {
                x = 0f;
                z = 0f;
            }
            Horse horse = new Horse(
                data.id(),
                data.name(),
                new ModelInstance(models.horse),
                new Vector3(x, Terrain.heightAt(x, z), z),
                data.heading()
            );
            horse.trust = MathUtils.clamp(data.trust(), 0f, 100f);
            horse.stamina = MathUtils.clamp(data.stamina(), 0f, 100f);
            horse.tamed = data.tamed();
            horse.wanderTimer = randomRange(1.5f, 5f);
            horses.add(horse);
        }
    }

    private void loadFences(List<SaveGame.FenceData> savedFences) {
        for (SaveGame.FenceData data : savedFences) {
            float x = clampWorld(data.x());
            float z = clampWorld(data.z());
            if (Terrain.isInsideLake(x, z)) {
                continue;
            }
            ModelInstance fence = new ModelInstance(models.fence);
            fence.transform.setToTranslation(x, Terrain.heightAt(x, z), z).rotate(Vector3.Y, data.heading());
            fences.add(new FenceNode(fence, x, z, data.heading()));
        }
    }

    private void updateCameraInput() {
        float sensitivity = settings.mouseSensitivity();
        cameraYaw -= Gdx.input.getDeltaX() * sensitivity;
        cameraPitch += Gdx.input.getDeltaY() * sensitivity * 0.75f;
        cameraPitch = MathUtils.clamp(cameraPitch, 12f, 62f);
    }

    private void updatePlayer(float dt) {
        getCameraForward(tmpForward);
        tmpRight.set(tmpForward.z, 0f, -tmpForward.x);
        tmpMove.setZero();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) tmpMove.add(tmpForward);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) tmpMove.sub(tmpForward);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) tmpMove.add(tmpRight);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) tmpMove.sub(tmpRight);

        if (!tmpMove.isZero(0.001f)) {
            tmpMove.nor();
            float speed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? PLAYER_RUN_SPEED : PLAYER_WALK_SPEED;
            float nextX = playerPosition.x + tmpMove.x * speed * dt;
            float nextZ = playerPosition.z + tmpMove.z * speed * dt;
            nextX = MathUtils.clamp(nextX, -WORLD_LIMIT, WORLD_LIMIT);
            nextZ = MathUtils.clamp(nextZ, -WORLD_LIMIT, WORLD_LIMIT);
            if (!Terrain.isInsideLake(nextX, nextZ)) {
                playerPosition.x = nextX;
                playerPosition.z = nextZ;
            }
            playerFacing = MathUtils.atan2(tmpMove.x, tmpMove.z) * MathUtils.radiansToDegrees;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && playerJumpOffset <= 0.001f) {
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

    private void updateMountedHorse(float dt) {
        Horse horse = mountedHorse;
        float targetSpeed = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            boolean gallop = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && horse.stamina > 1f;
            targetSpeed = gallop ? 11.5f : 6.4f;
            if (gallop) horse.stamina = Math.max(0f, horse.stamina - 12f * dt);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            targetSpeed = -2.8f;
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || targetSpeed <= 6.4f) {
            horse.stamina = Math.min(100f, horse.stamina + 7f * dt);
        }

        horse.speed = MathUtils.lerp(horse.speed, targetSpeed, Math.min(1f, 3.4f * dt));
        float steering = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) steering += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) steering -= 1f;
        float steeringScale = 35f + Math.min(45f, Math.abs(horse.speed) * 4f);
        horse.heading += steering * steeringScale * dt;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && horse.jumpOffset <= 0.001f) {
            horse.jumpVelocity = 7.4f;
        }
        horse.jumpVelocity -= 19f * dt;
        horse.jumpOffset += horse.jumpVelocity * dt;
        if (horse.jumpOffset < 0f) {
            horse.jumpOffset = 0f;
            horse.jumpVelocity = 0f;
        }

        float sin = MathUtils.sinDeg(horse.heading);
        float cos = MathUtils.cosDeg(horse.heading);
        float nextX = horse.position.x + sin * horse.speed * dt;
        float nextZ = horse.position.z + cos * horse.speed * dt;
        nextX = MathUtils.clamp(nextX, -WORLD_LIMIT, WORLD_LIMIT);
        nextZ = MathUtils.clamp(nextZ, -WORLD_LIMIT, WORLD_LIMIT);
        if (!Terrain.isInsideLake(nextX, nextZ)) {
            horse.position.x = nextX;
            horse.position.z = nextZ;
        } else {
            horse.speed *= 0.65f;
        }
        horse.position.y = Terrain.heightAt(horse.position.x, horse.position.z);
        playerPosition.set(horse.position);
        playerFacing = horse.heading;
    }

    private void updateWildHorses(float dt) {
        Vector3 danger = mountedHorse == null ? playerPosition : mountedHorse.position;
        for (Horse horse : horses) {
            if (horse == mountedHorse) continue;

            float distance = planarDistance(horse.position.x, horse.position.z, danger.x, danger.z);
            float desiredSpeed;
            if (!horse.tamed && distance < 8f) {
                float awayX = horse.position.x - danger.x;
                float awayZ = horse.position.z - danger.z;
                horse.heading = MathUtils.atan2(awayX, awayZ) * MathUtils.radiansToDegrees;
                desiredSpeed = 5.9f;
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
            float nextX = horse.position.x + MathUtils.sinDeg(horse.heading) * horse.speed * dt;
            float nextZ = horse.position.z + MathUtils.cosDeg(horse.heading) * horse.speed * dt;
            if (Math.abs(nextX) > WORLD_LIMIT || Math.abs(nextZ) > WORLD_LIMIT || Terrain.isInsideLake(nextX, nextZ)) {
                horse.heading += 150f + randomRange(-25f, 25f);
                horse.speed *= 0.3f;
            } else {
                horse.position.x = nextX;
                horse.position.z = nextZ;
            }
            horse.position.y = Terrain.heightAt(horse.position.x, horse.position.z);
        }
    }

    private void updatePushik(float dt) {
        Vector3 target = mountedHorse == null ? playerPosition : mountedHorse.position;
        float dx = target.x - pushik.position.x;
        float dz = target.z - pushik.position.z;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);

        if (distance > 28f) {
            pushik.position.set(target.x - 2f, Terrain.heightAt(target.x - 2f, target.z - 1f), target.z - 1f);
            distance = 0f;
        }
        if (distance > 2.3f) {
            pushik.heading = MathUtils.atan2(dx, dz) * MathUtils.radiansToDegrees;
            float speed = Math.min(mountedHorse == null ? 5.6f : 9.4f, 1.5f + distance * 0.72f);
            float nx = pushik.position.x + MathUtils.sinDeg(pushik.heading) * speed * dt;
            float nz = pushik.position.z + MathUtils.cosDeg(pushik.heading) * speed * dt;
            if (!Terrain.isInsideLake(nx, nz)) {
                pushik.position.x = nx;
                pushik.position.z = nz;
            }
        }
        pushik.position.y = Terrain.heightAt(pushik.position.x, pushik.position.z);
    }

    private void handleInteractions() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            if (planarDistance(playerPosition.x, playerPosition.z, pushik.position.x, pushik.position.z) < 3.1f) {
                setStatus("Pushik purrs softly. Even his fluffy black paws barely make a sound.");
                return;
            }

            Horse horse = nearestHorse(4.4f);
            if (horse != null) {
                if (!horse.tamed) {
                    if (apples <= 0) {
                        setStatus(horse.name + " watches you carefully. You need an apple.");
                        return;
                    }
                    apples--;
                    horse.trust = Math.min(100f, horse.trust + 34f);
                    if (horse.trust >= 100f) {
                        horse.tamed = true;
                        horse.trust = 100f;
                        saveNow(null);
                        setStatus(horse.name + " trusts you now. Progress saved. Press F nearby to ride.");
                    } else {
                        setStatus(horse.name + " accepted the apple. Trust: " + Math.round(horse.trust) + "%");
                    }
                } else {
                    setStatus("You pet " + horse.name + ". The horse relaxes beside you.");
                }
                return;
            }

            TreeNode tree = nearestTree(3.7f);
            if (tree != null && !tree.harvested) {
                tree.harvested = true;
                wood += 2;
                setStatus("Collected 2 wood. Press B to place a fence segment (cost: 2 wood).");
                return;
            }
            setStatus("Nothing close enough to interact with.");
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            if (mountedHorse != null) {
                Horse old = mountedHorse;
                old.mounted = false;
                old.speed = 0f;
                mountedHorse = null;
                playerPosition.set(
                    old.position.x + MathUtils.cosDeg(old.heading) * 2f,
                    0f,
                    old.position.z - MathUtils.sinDeg(old.heading) * 2f
                );
                playerPosition.y = Terrain.heightAt(playerPosition.x, playerPosition.z);
                setStatus("Dismounted " + old.name + ".");
                return;
            }
            Horse horse = nearestHorse(4.8f);
            if (horse == null) {
                setStatus("Stand closer to a horse to mount.");
            } else if (!horse.tamed) {
                setStatus(horse.name + " does not trust you enough yet. Feed with E.");
            } else {
                mountedHorse = horse;
                horse.mounted = true;
                horse.speed = 0f;
                setStatus("Mounted " + horse.name + ". W/S move, A/D steer, Shift gallop, Space jump.");
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            if (wood < 2) {
                setStatus("You need 2 wood for a fence segment. Gather from a nearby tree with E.");
                return;
            }
            Vector3 origin = mountedHorse == null ? playerPosition : mountedHorse.position;
            getActorForward(tmpForward);
            float x = origin.x + tmpForward.x * 3.3f;
            float z = origin.z + tmpForward.z * 3.3f;
            if (Terrain.isInsideLake(x, z) || Math.abs(x) > WORLD_LIMIT || Math.abs(z) > WORLD_LIMIT) {
                setStatus("You cannot build there.");
                return;
            }
            ModelInstance fence = new ModelInstance(models.fence);
            float heading = mountedHorse == null ? playerFacing : mountedHorse.heading;
            fence.transform.setToTranslation(x, Terrain.heightAt(x, z), z).rotate(Vector3.Y, heading);
            fences.add(new FenceNode(fence, x, z, heading));
            wood -= 2;
            setStatus("Fence placed. Your first paddock has begun.");
        }
    }

    private void updateDayNight(float dt) {
        worldTime = (worldTime + dt / 1200f) % 1f;
        float solarAngle = worldTime * 360f - 90f;
        float sunHeight = MathUtils.sinDeg(solarAngle);
        float daylight = MathUtils.clamp((sunHeight + 0.20f) / 1.05f, 0.10f, 1f);

        sun.direction.set(-MathUtils.cosDeg(solarAngle) * 0.55f, -Math.max(0.18f, sunHeight), -0.34f).nor();
        sun.color.set(0.95f * daylight + 0.05f, 0.87f * daylight + 0.08f, 0.72f * daylight + 0.12f, 1f);
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
                .translate(mountedHorse.position.x, mountedHorse.position.y + mountedHorse.jumpOffset + 2.35f, mountedHorse.position.z)
                .rotate(Vector3.Y, mountedHorse.heading)
                .scale(0.88f, 0.88f, 0.88f);
        }

        for (Horse horse : horses) {
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
        float offsetX = -MathUtils.sinDeg(cameraYaw) * cosPitch * cameraDistance;
        float offsetZ = -MathUtils.cosDeg(cameraYaw) * cosPitch * cameraDistance;
        float offsetY = MathUtils.sinDeg(cameraPitch) * cameraDistance;
        camera.position.set(tmpTarget.x + offsetX, tmpTarget.y + offsetY, tmpTarget.z + offsetZ);
        camera.up.set(Vector3.Y);
        camera.lookAt(tmpTarget);
        camera.update();
    }

    private void renderWorld() {
        float solarAngle = worldTime * 360f - 90f;
        float daylight = MathUtils.clamp((MathUtils.sinDeg(solarAngle) + 0.20f) / 1.05f, 0.08f, 1f);
        float r = 0.035f + daylight * 0.39f;
        float g = 0.055f + daylight * 0.58f;
        float b = 0.09f + daylight * 0.68f;
        Gdx.gl.glClearColor(r, g, b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);
        modelBatch.render(terrain.instance, environment);
        modelBatch.render(water, environment);
        for (TreeNode tree : trees) {
            if (!tree.harvested) modelBatch.render(tree.instance, environment);
        }
        for (ModelInstance rock : rocks) modelBatch.render(rock, environment);
        for (FenceNode fence : fences) modelBatch.render(fence.instance, environment);
        for (Horse horse : horses) modelBatch.render(horse.instance, environment);
        modelBatch.render(pushik.instance, environment);
        modelBatch.render(player, environment);
        modelBatch.end();
    }

    private void renderHud() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        spriteBatch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        spriteBatch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);
        font.draw(spriteBatch, "HORSEBOUND", 18f, height - 18f);
        font.getData().setScale(0.82f);
        font.setColor(new Color(0.88f, 0.91f, 0.86f, 1f));
        font.draw(spriteBatch, "WASD move | Mouse camera | E interact | F mount | B build | F5 save | Shift sprint/gallop | Space jump", 18f, height - 42f);
        font.draw(spriteBatch,
            "Wood: " + wood + "    Apples: " + apples + "    Seed: " + worldSeed + "    Save: " + saveService.activeSlot(),
            18f,
            height - 64f
        );

        if (mountedHorse != null) {
            font.setColor(new Color(1f, 0.87f, 0.57f, 1f));
            font.draw(spriteBatch, mountedHorse.name + " | stamina " + Math.round(mountedHorse.stamina) + "% | speed " + String.format(Locale.ROOT, "%.1f", Math.abs(mountedHorse.speed)), 18f, height - 86f);
        } else {
            Horse close = nearestHorse(7f);
            if (close != null) {
                font.setColor(new Color(1f, 0.87f, 0.57f, 1f));
                font.draw(spriteBatch, close.name + " | trust " + Math.round(close.trust) + "%" + (close.tamed ? " | tamed" : " | wild"), 18f, height - 86f);
            }
        }

        if (planarDistance(playerPosition.x, playerPosition.z, pushik.position.x, pushik.position.z) < 8f) {
            font.setColor(new Color(0.82f, 0.82f, 0.86f, 1f));
            font.draw(spriteBatch, "Pushik is nearby, padding silently on his fluffy black paws.", 18f, 52f);
        }

        if (statusTimer > 0f && status != null) {
            font.setColor(new Color(1f, 0.96f, 0.78f, 1f));
            font.draw(spriteBatch, status, 18f, 82f);
        }

        font.setColor(new Color(0.68f, 0.73f, 0.69f, 1f));
        font.getData().setScale(0.72f);
        font.draw(spriteBatch, "Created by Dimash Janibekov (DizZyZ7) | (c) 2026 All rights reserved", Math.max(18f, width - 420f), 20f);
        spriteBatch.end();
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    private Horse nearestHorse(float radius) {
        Horse nearest = null;
        float best = radius;
        Vector3 origin = mountedHorse == null ? playerPosition : mountedHorse.position;
        for (Horse horse : horses) {
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

    private void saveNow(String message) {
        try {
            saveService.save(captureSave());
            if (message != null) {
                setStatus(message);
            }
        } catch (SaveRepository.SaveException ex) {
            Gdx.app.error("HORSEBOUND", "Save failed", ex);
            setStatus("Save failed. Your current session is still running.");
        }
    }

    private SaveGame captureSave() {
        List<SaveGame.HorseData> horseData = new ArrayList<>(horses.size());
        for (Horse horse : horses) {
            horseData.add(new SaveGame.HorseData(
                horse.id,
                horse.name,
                horse.position.x,
                horse.position.z,
                horse.heading,
                horse.trust,
                horse.stamina,
                horse.tamed
            ));
        }

        List<SaveGame.FenceData> fenceData = new ArrayList<>(fences.size());
        for (FenceNode fence : fences) {
            fenceData.add(new SaveGame.FenceData(fence.x, fence.z, fence.heading));
        }

        List<Integer> harvestedTrees = new ArrayList<>();
        for (TreeNode tree : trees) {
            if (tree.harvested) {
                harvestedTrees.add(tree.id);
            }
        }

        return new SaveGame(
            SaveGame.CURRENT_VERSION,
            worldSeed,
            System.currentTimeMillis(),
            worldTime,
            new SaveGame.PlayerData(playerPosition.x, playerPosition.z, playerFacing, wood, apples),
            new SaveGame.PushikData(pushik.position.x, pushik.position.z, pushik.heading),
            horseData,
            fenceData,
            harvestedTrees
        );
    }

    private static float planarDistance(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    private static float clampWorld(float value) {
        if (!Float.isFinite(value)) {
            return 0f;
        }
        return MathUtils.clamp(value, -WORLD_LIMIT, WORLD_LIMIT);
    }

    private static float normalizeTime(float value) {
        if (!Float.isFinite(value)) {
            return 0.29f;
        }
        float normalized = value % 1f;
        return normalized < 0f ? normalized + 1f : normalized;
    }

    private void setStatus(String message) {
        status = message;
        statusTimer = 5f;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
        saveNow(null);
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
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
        boolean harvested;

        TreeNode(int id, ModelInstance instance, float x, float z) {
            this.id = id;
            this.instance = instance;
            this.x = x;
            this.z = z;
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

    private static final class Horse {
        final UUID id;
        final String name;
        final ModelInstance instance;
        final Vector3 position;
        float heading;
        float trust;
        float stamina = 100f;
        float speed;
        float wanderTimer;
        float jumpOffset;
        float jumpVelocity;
        boolean tamed;
        boolean mounted;

        Horse(UUID id, String name, ModelInstance instance, Vector3 position, float heading) {
            this.id = id;
            this.name = name;
            this.instance = instance;
            this.position = position;
            this.heading = heading;
        }
    }

    private static final class Pushik {
        final ModelInstance instance;
        final Vector3 position;
        float heading;

        Pushik(ModelInstance instance, Vector3 position) {
            this.instance = instance;
            this.position = position;
        }
    }
}
