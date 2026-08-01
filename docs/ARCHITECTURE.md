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
- pause/session lifecycle;
- interaction and save orchestration;
- future placement validator/snapshot assembler.

### Presentation

- libGDX screens;
- keyboard/mouse/controller adapters;
- cameras, models and lighting;
- HUD, scalable prompts and menus;
- performance/support overlays.

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

This keeps movement, horse AI, needs, weather and save tests independent from 30/60/144 Hz rendering.

## Homestead architecture

0.5 uses explicit domain models rather than embedding more state into `LivingRanchScreen`.

```text
Input / build selection
        ↓
Placement request + validator
        ↓
HomesteadState.place(...)
        ↓
Inventory cost transaction
        ↓
PlacedStructure + renderer actor
        ↓
Save v4 snapshot
```

Horse care follows:

```text
fixed simulation tick
        ↓
HorseNeeds.tick(...)
        ↓
HorseCareSystem
        ↓
nearest feeder / trough / stall query
        ↓
care result + visual/audio feedback
```

Per-stack item limits are presentation limits. `Inventory` owns aggregate totals and exposes stack views, preventing old saves with more than one stack from being truncated.

## Version sequence

### 0.5.0 — Homestead Domain Foundation

- inventory totals and stack views;
- persistent hotbar;
- typed structure catalog/storage;
- persistent horse needs;
- save format v4 and v3 binary migration coverage.

### 0.5.1 — Live Homestead Integration

- hotbar input and HUD;
- placement ghost, snapping and collision validation;
- gates, feeders, troughs and stalls rendered in-world;
- deposit interactions;
- live HorseCareSystem updates;
- active-screen v4 snapshot assembly.

### 0.5.2 — Inventory and Building UX

- inventory screen;
- chest/storage UI;
- placement rotation/cancel/undo;
- accessible controller building flow;
- construction feedback and audio.

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

Before Early Access, do not prioritize:

- multiplayer or accounts/backend;
- live-service economy;
- DLC architecture;
- large NPC populations;
- advanced genetics before care/build/explore is fun.

The production loop is:

```text
explore -> gather -> care -> bond -> build -> improve ranch -> explore farther
```

Every feature must strengthen at least one part of that loop and survive save/load before it is considered complete.
