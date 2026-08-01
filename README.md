# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.3 — Ranch Architecture & Production Interaction

This release removes the temporary reflection bridge and strengthens the playable inventory, Gate and feedback loop without changing save format v5.

### Typed ranch access

- `LivingRanchScreen` implements the explicit `RanchWorldAccess` contract;
- Homestead receives immutable player/mounted-horse and horse telemetry snapshots;
- collision correction uses typed methods rather than private-field lookup;
- internal mutable actors and `Vector3` instances are never exposed;
- `LivingRanchTelemetryAdapter` remains only as a compatibility facade;
- `java.lang.reflect`, `Field`, `setAccessible` and string-based field lookup are removed;
- tests and the Windows package gate prevent the reflection bridge from returning.

### Inventory and Chest interaction

- Confirm/A transfers one item;
- Build/L1 transfers the selected visual stack;
- Mount/Y transfers every item of the selected type;
- destination capacity is checked before mutation;
- insufficient space never causes a partial move or deleted item;
- held controller directions repeat after a deliberate delay;
- action buttons remain edge-only and cannot duplicate transfers.

### Gates and feedback

- persistent Gate collision changes immediately;
- the visible Gate now animates smoothly between closed and 90 degrees;
- loaded Gates begin at their saved target instead of replaying an animation;
- construction, relocation, dismantling, Gate and successful transfer cues are synthesized procedurally;
- audio requires no external WAV assets;
- unsupported audio hardware degrades safely to silence.

### Persistence

Save format remains **v5**. Existing 0.5.2 ranches load without migration changes, preserving Gate state, Chest contents, moved structures, storage, inventory, hotbar, horse needs, Pushik and world progress.

See [`docs/releases/0.5.3.md`](docs/releases/0.5.3.md) and [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md).

## Current playable gameplay

- third-person movement and camera;
- procedural continuous terrain, lake, trees and rocks;
- resource gathering and persistent construction;
- visible hotbar and placement preview;
- feeders, troughs, hay storage, Chests, Gates and Stalls;
- persistent one-item, stack and full-type Chest transfers;
- movable and safely dismantled structures;
- player and horse structure collisions;
- animated opening and closing Gates;
- horse personalities and trust/bond/fear;
- relationship-based taming;
- riding, gallop stamina and jumping;
- hunger, thirst, energy and automatic ranch care;
- day/night lighting;
- Pushik companion AI;
- procedural interaction sounds;
- Continue / New Game / Load Game / Settings / Exit;
- three ranch save slots with structure metadata;
- manual save, autosave and backup recovery.

## Pushik / Пушик

Pushik is the completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player. His affection and state survive restart.

## Input and accessibility

HORSEBOUND has a true in-memory pause lifecycle and device-local input profile.

- configurable keyboard bindings and conflict-safe key swapping;
- dedicated Inventory binding with legacy-profile fallback;
- invert vertical camera;
- movement and camera stick dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible pause, settings, bindings, inventory and build/edit UX;
- dynamic Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- context-aware D-pad actions avoid Inventory/blueprint conflicts;
- held directional navigation repeats while Confirm/Back remain edge-only.

Input settings are stored in `%APPDATA%\HORSEBOUND\input.properties` and remain separate from ranch saves, Steam Cloud and the install depot.

## Display and Deck-oriented UX

- 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 profiles;
- UI scale from 100% to 150%;
- Low / Medium / High startup presets;
- optional rolling FPS/frame-time overlay;
- global scalable action-prompt strip;
- controller-readable menus, save slots, hotbar, inventory and build feedback.

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family and Valve compatibility testing remain required.

## Architecture

```text
HomesteadRanchScreen
├── LivingRanchScreen : RanchWorldAccess
├── captured pure-Java GameSession
├── InventoryOverlay + InventoryTransferService
├── HomesteadCollisionSystem
├── HomesteadRenderer
│   ├── GateAnimationState
│   ├── RanchPresentationObserver
│   └── shared procedural RanchAudio
├── hotbar + build/edit input adapter
├── HorseCareSystem
└── owner-scoped save transformer
```

Domain and persistence code do not use reflection or libGDX rendering. `RanchWorldAccess` is a narrow presentation-only boundary. `LivingRanchTelemetryAdapter` delegates to that interface solely to preserve the existing wrapper API while later renderer/controller decomposition continues.

## User data

```text
%APPDATA%\HORSEBOUND\
    settings.properties
    input.properties
    saves\
        slot-1\save.hbs + save.bak
        slot-2\save.hbs + save.bak
        slot-3\save.hbs + save.bak
    logs\
        crash-*.log
```

Steam Auto-Cloud is planned for ranch `save.hbs` and `save.bak` only. Display/input settings and diagnostics remain device-local.

## Steam readiness

- self-contained Windows x64 `jpackage` image;
- direct `HORSEBOUND.exe` launch without a mandatory launcher;
- bundled Java runtime;
- exact packaged version/commit and SHA-256 manifest;
- no mutable user files inside the depot;
- controller, migration, storage, collision and typed-ranch package gates;
- CI rejection of reflection bridge regressions;
- third-party notices and exact license texts.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.3.md`](docs/releases/0.5.3.md)
- [`steam/README.md`](steam/README.md)

## Build

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
gradle test
```

Windows self-contained image:

```powershell
gradle clean test windowsImage
```

Output:

```text
build/jpackage/HORSEBOUND/HORSEBOUND.exe
```

CI verifies the controller, accessibility, save migration, inventory/storage, typed ranch access, procedural audio, Gate animation and live Homestead runtime inside the packaged JAR.

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3
- gdx-controllers 2.2.4 / Jamepad
- Gradle
- JDK binary persistence
- JDK Properties settings
- procedural mono interaction audio
- Windows `jpackage`

## Licensing and ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md).

Third-party runtime components retain their own licenses. See [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) and the [`licenses/`](licenses/) directory.
