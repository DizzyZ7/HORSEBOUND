# HORSEBOUND — Production Architecture

Created by Dimash Janibekov (DizZyZ7). Copyright © 2026. All rights reserved.

## Goal

HORSEBOUND is a single-player cozy horse sandbox built in Java 21 with libGDX/LWJGL3 as the presentation/platform layer. Gameplay rules, simulation, persistence contracts and content models should remain pure Java wherever practical.

## Architectural rule

Presentation must not own game truth.

Target dependency direction:

```text
presentation.gdx -> application -> domain
persistence      -> domain DTO contracts
platform         -> application
```

The domain must not depend on libGDX input, rendering or window classes.

## Layers

### domain

Pure Java state and rules:

- `GameSession`
- `Inventory`
- horse identity/personality/relationship/needs
- Pushik companion mind
- item definitions
- crafting recipes
- building definitions and placement rules
- world clock/weather state

### application

Use-cases and orchestration:

- player commands
- interaction resolution
- fixed-step simulation
- horse system
- companion system
- building service
- crafting service
- save snapshot assembly

### presentation.gdx

libGDX-facing code only:

- screens
- input mapping
- cameras
- model/animation instances
- HUD/menu rendering
- audio playback
- visual interpolation

### persistence

Versioned save/load infrastructure:

- save DTOs
- migrations
- validation
- atomic writes
- backup recovery
- application settings

## Next sequence

### 0.4.1 Stabilization

1. Save format v3 persists typed inventory and Pushik companion data.
2. Preserve v1/v2 migration.
3. Add explicit snapshot assembler instead of constructing save DTOs inside the screen.
4. Remove legacy `WorldScreen` after validation.
5. Establish fixed-step simulation boundary.

### 0.5 Homestead Core

1. Item catalog and inventory/hotbar.
2. Recipes and crafting service.
3. Building catalog: fence, gate, stall, feeder, trough, hay storage, chest.
4. Placement validator + snapping + refund/undo semantics.
5. Horse hunger/thirst/energy and feeding/watering gameplay.
6. Persist every new domain state before adding the next content group.

### 0.6 Living World

1. Chunk/region streaming.
2. Deterministic world generation by seed.
3. Biomes and points of interest.
4. Resource respawn policy.
5. Weather and ambient simulation.
6. Wildlife/NPC scheduling only after streaming is stable.

### 0.7 Presentation

1. Production GLTF/GLB asset pipeline.
2. Horse/player/Pushik animation state machines.
3. Camera collision, spring and gait feel.
4. Spatial audio and ambient layers.
5. Grass/lighting/shader polish and LOD.
6. Controller support and input rebinding.

### 0.8 Release Foundation

1. Crash/error logs.
2. Save compatibility tests across released formats.
3. Performance budgets and profiling scenes.
4. Localization-ready strings.
5. Steam integration only after the single-player loop is stable.

## Fixed-step simulation

Simulation will use an accumulator and a fixed update interval. Rendering may run at a different frame rate and later interpolate visual transforms. Domain systems receive commands/context rather than reading `Gdx.input` directly.

This makes horse AI, needs, weather and save/replay tests deterministic enough to validate independently from GPU frame rate.

## ECS decision

Do not introduce a full ECS yet. The current game benefits more from explicit domain models and focused systems. Re-evaluate ECS when entity counts, chunk streaming and many shared component combinations make explicit orchestration measurably painful.

## Scope control

Before Early Access, do not prioritize:

- multiplayer;
- accounts/backend;
- live-service economy;
- DLC architecture;
- large NPC populations;
- advanced horse genetics before the core care/build/explore loop is fun.

The production loop is:

```text
explore -> gather -> care -> bond -> build -> improve ranch -> explore farther
```

Every feature should strengthen at least one part of that loop.
