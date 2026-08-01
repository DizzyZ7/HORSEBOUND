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

The domain must not depend on libGDX input, rendering, controller, audio or window classes.

## Current layers

### Domain

- `GameSession` and fixed world clock;
- `Inventory`, `InventoryStack`, `InventoryTransferService` and `Hotbar`;
- horse identity, personality, relationship and `HorseNeeds`;
- `HorseCareSystem`;
- Pushik companion mind;
- `HomesteadStructureType`, `PlacedStructure` and `HomesteadState`;
- item definitions, recipes and operational structure state;
- `HomesteadCollisionSystem` and `GateAnimationState` pure calculation contracts.

### Application

- device-neutral `PlayerCommand` and menu commands;
- fixed-step `GameSimulationLoop`;
- generic `RanchSessionScreen` pause/save lifecycle;
- owner-scoped save enrichment;
- semantic Homestead action routing;
- delayed directional `NavigationRepeater` with edge-only actions.

### Presentation

- `HomesteadRanchScreen` live integration wrapper;
- `LivingRanchScreen` base world renderer/controller implementing `RanchWorldAccess`;
- `LivingRanchTelemetryAdapter` typed compatibility facade;
- provisional `HomesteadModels` and `HomesteadRenderer`;
- `RanchPresentationObserver` and procedural `RanchAudio`;
- keyboard/mouse/controller adapters;
- HUD, scalable prompts, menus and performance/support overlays.

### Persistence

- `SaveGame` v5 DTO contract;
- v1–v4 migration;
- typed inventory, hotbar, structures, Gate state, Chest contents and horse needs;
- validation and bounded collection sizes;
- temporary writes, disk flush and atomic replacement;
- backup recovery;
- device-local display/input settings.

## Fixed-step simulation

Simulation uses a bounded accumulator at 60 ticks per second. Rendering may run at a different frame rate and later interpolate visual transforms. Domain systems receive commands/context rather than reading `Gdx.input` directly.

Horse needs and Homestead care use fixed-step clocks. Gate visual interpolation is presentation-only; persistent collision truth changes immediately.

## Typed ranch access

The Homestead wrapper no longer reads private fields through reflection.

```text
HomesteadRanchScreen
        ↓
LivingRanchTelemetryAdapter (compatibility facade)
        ↓
RanchWorldAccess
        ↓
LivingRanchScreen
```

`RanchWorldAccess` exposes only:

- camera reference required by the renderer;
- immutable actor pose;
- immutable horse telemetry list;
- safe active-actor correction;
- safe horse correction by UUID.

Mutable `Vector3`, HorseActor and renderer-owned collections never cross the boundary. Tests and CI reject `java.lang.reflect`, `setAccessible` or `getDeclaredField` returning to the facade.

The compatibility facade may be removed later when `HomesteadRanchScreen` directly consumes `RanchWorldAccess`; this is cleanup rather than a safety blocker.

## Building and interaction flow

```text
remappable command / controller edge
        ↓
HomesteadActionBus
        ↓
placement, edit, Gate or inventory use case
        ↓
pure-Java validation / exact Inventory transaction
        ↓
HomesteadState mutation
        ↓
HomesteadRenderer + presentation observer
        ↓
Save v5 enrichment
```

Placement validates world bounds, lake, slope, recipe and structure overlap. Swept collision protects player, mounted horse and autonomous horses, including actors initially embedded by a newly placed or closed structure.

## Inventory transfer flow

```text
one / selected stack / all of item type
        ↓
InventoryTransferService exact request
        ↓
destination capacity preflight
        ↓
source.transferTo(destination, exactAmount)
        ↓
procedural success cue
```

Insufficient capacity produces zero mutation. Confirm, Build and Mount transfer actions remain edge-only. Only directional navigation repeats after a delay.

## Horse care flow

```text
fixed care tick
        ↓
typed RanchWorldAccess telemetry
        ↓
HorseNeeds.tick(...)
        ↓
HorseCareSystem
        ↓
nearby feeder / trough / stall query
        ↓
resource consumption + HUD feedback
        ↓
Save v5 enrichment
```

Per-stack item limits are presentation limits. `Inventory` owns aggregate totals and exposes stack views, preventing legacy overflow from being truncated.

## Save ownership

`SaveService` accepts an owner-scoped transformer. Every manual save, autosave, pause save and disposal save produced by the delegate passes through the active Homestead owner.

A stale screen cannot clear a transformer installed by a newer ranch session. The final snapshot combines:

- live positions and relationships from the typed delegate;
- inventory/hotbar from the captured session;
- structures, Gate state, Chest contents and resource storage;
- current horse needs;
- deterministic legacy-fence compatibility structures.

## Procedural audio boundary

`RanchPresentationObserver` compares immutable structure snapshots and emits semantic cues. `RanchAudio` generates short mono waveforms at runtime and streams them through one optional shared libGDX AudioDevice.

No audio asset is required for boot or packaging. Unsupported audio becomes a no-op. Domain and save code never reference audio classes.

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
- resource deposits and live horse care;
- complete v4 save enrichment.

### 0.5.2 — Inventory, Physics & Building UX

- save v5 operational state;
- inventory and persistent Chest storage;
- opening Gates;
- player/horse structure collision;
- relocation and safe dismantling.

### 0.5.3 — Ranch Architecture & Production Interaction

- explicit typed ranch-access interface;
- removal of reflection telemetry;
- one/stack/all exact transfers;
- held directional navigation repeat;
- Gate visual interpolation;
- procedural interaction audio;
- stronger package assertions.

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
- authored spatial audio and ambient layers;
- grass, lighting, shaders and LOD.

## ECS decision

Do not introduce a full ECS yet. Explicit domain models and focused systems are easier to test and ship at the current entity count. Re-evaluate only when chunk streaming and many shared component combinations create measured pain.

## Scope control

Before Early Access, do not prioritize multiplayer/backend, live-service economy, DLC architecture, large NPC populations or advanced genetics ahead of the care/build/explore loop.

```text
explore -> gather -> care -> bond -> build -> improve ranch -> explore farther
```

Every feature must strengthen that loop and survive save/load before it is considered complete.
