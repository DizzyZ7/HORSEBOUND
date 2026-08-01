# HORSEBOUND — Production Architecture

Created by **Dimash Janibekov (DizZyZ7)**. Copyright © 2026. All rights reserved.

## Goal

HORSEBOUND is a paid single-player cozy horse sandbox built in Java 21. libGDX/LWJGL3 provide presentation and platform access; game truth, simulation, persistence contracts and content rules remain pure Java wherever practical.

## Dependency rule

Presentation must not own game truth.

```text
presentation.gdx -> application -> domain
persistence      -> domain DTO contracts
platform         -> application
```

The domain must not depend on libGDX input, rendering, controller or window classes.

## Current layers

### Domain

- `GameSession` and fixed world clock;
- `Inventory`, `InventoryStack` and `Hotbar`;
- horse identity, personality, relationship and `HorseNeeds`;
- `HorseCareSystem`;
- Pushik companion mind;
- `HomesteadStructureType`, `PlacedStructure` and `HomesteadState`;
- item definitions and structure recipes.

### Application

- device-neutral `PlayerCommand` and menu commands;
- fixed-step `GameSimulationLoop`;
- generic `RanchSessionScreen` pause/save lifecycle;
- owner-scoped save enrichment;
- semantic Homestead build/deposit/cancel action routing.

### Presentation

- `HomesteadRanchScreen` live integration wrapper;
- legacy `LivingRanchScreen` presentation delegate;
- isolated `LivingRanchTelemetryAdapter` compatibility bridge;
- provisional `HomesteadModels` and `HomesteadRenderer`;
- keyboard/mouse/controller adapters;
- HUD, scalable prompts, menus and performance/support overlays.

### Persistence

- `SaveGame` v4 DTO contract;
- v1–v3 migration;
- typed inventory, hotbar, structures and horse needs;
- validation and bounded collection sizes;
- temporary writes, disk flush and atomic replacement;
- backup recovery;
- device-local display/input settings.

## Fixed-step simulation

Simulation uses a bounded accumulator at 60 ticks per second. Rendering may run at a different frame rate and later interpolate visual transforms. Domain systems receive commands/context rather than reading `Gdx.input` directly.

Horse needs and Homestead care use a fixed-step clock. This keeps behavior independent from 30/60/144 Hz rendering.

## Live Homestead integration

0.5.1 avoids placing more responsibility inside the legacy gameplay screen.

```text
HorseboundGame
        ↓
HomesteadRanchScreen (session/presentation owner)
        ├── LivingRanchScreen delegate
        ├── captured GameSession
        ├── Homestead input/action layer
        ├── HomesteadRenderer
        ├── HorseCareSystem
        └── SaveService transformer
```

The wrapper obtains the actual `GameSession` through a scoped construction bridge, not reflection. Temporary reflection exists only in `LivingRanchTelemetryAdapter` to read camera and actor telemetry from the old presentation class. Domain and persistence code never depend on it.

This is a transition architecture, not a permanent excuse to preserve the legacy screen forever.

## Building flow

```text
remappable Build command
        ↓
HomesteadActionBus
        ↓
build selection + snapped preview
        ↓
placement validation
        ↓
HomesteadState.place(...)
        ↓
atomic Inventory transaction
        ↓
HomesteadRenderer actor
        ↓
owner-scoped Save v4 enrichment
```

Validation currently checks world bounds, lake, slope, recipe and structure overlap. Player/horse physical collision and structure navigation are explicit 0.5.2 work.

## Horse care flow

```text
fixed care tick
        ↓
legacy actor telemetry
        ↓
HorseNeeds.tick(...)
        ↓
HorseCareSystem
        ↓
nearby feeder / trough / stall query
        ↓
resource consumption + HUD feedback
        ↓
Save v4 enrichment
```

Per-stack item limits are presentation limits. `Inventory` owns aggregate totals and exposes stack views, preventing old saves with more than one stack from being truncated.

## Save ownership

`SaveService` accepts an owner-scoped transformer. Every manual save, autosave, pause save and disposal save produced by the delegate passes through the current Homestead owner.

A stale screen cannot clear a transformer installed by a newer ranch session. The final snapshot combines:

- live positions and relationships from the delegate;
- inventory/hotbar from the captured session;
- placed structures and stored units;
- current horse needs;
- deterministic legacy-fence compatibility structures.

## Version sequence

### 0.5.0 — Homestead Domain Foundation

- inventory capacity and stack views;
- persistent hotbar;
- typed structure catalog/storage;
- persistent horse needs;
- save format v4 and binary v3 migration coverage.

### 0.5.1 — Live Homestead Integration

- live hotbar and controller selection;
- placement ghost, snapping and validation;
- structures rendered in-world;
- resource deposits;
- live horse-care updates;
- complete v4 save enrichment.

### 0.5.2 — Inventory, Physics and Building UX

- inventory/chest/storage screens;
- opening gates;
- player/horse structure collision;
- tree/rock placement collision;
- build undo/removal mode;
- construction feedback and audio;
- replace reflection telemetry with explicit delegate interfaces/controllers;
- begin production asset replacement.

### 0.6 — Living World

- region/chunk streaming;
- deterministic biomes and POIs;
- resource respawn policy;
- weather and shelter behavior;
- wildlife/NPC scheduling only after streaming is stable.

### 0.7 — Production Presentation

- GLTF/GLB asset pipeline;
- horse/player/Pushik animation state machines;
- camera collision and gait feel;
- spatial audio and ambient layers;
- grass, lighting, shaders and LOD.

## ECS decision

Do not introduce a full ECS yet. Explicit domain models and focused systems are easier to test and ship at the current entity count. Re-evaluate only when chunk streaming and many shared component combinations create measured pain.

## Scope control

Before Early Access, do not prioritize multiplayer/backend, live-service economy, DLC architecture, large NPC populations or advanced genetics ahead of the care/build/explore loop.

```text
explore -> gather -> care -> bond -> build -> improve ranch -> explore farther
```

Every feature must strengthen that loop and survive save/load before it is considered complete.
