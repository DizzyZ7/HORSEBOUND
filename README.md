# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.1 — Live Homestead Integration

The persistent Homestead foundation is now connected to the playable ranch.

### Hotbar and building

- visible eight-slot hotbar;
- keyboard 1–8 / numpad 1–8 and controller D-pad selection;
- remappable Build action enters and confirms placement;
- keyboard brackets or D-pad Up/Down cycle blueprints;
- R or D-pad Left/Right rotates in 15-degree steps;
- placement snaps to a 0.5 metre grid;
- lake, world-boundary, steep-ground, material and structure-overlap validation;
- green valid preview and red invalid preview;
- build cancel through Escape / controller Back without leaving the ranch.

Playable provisional structures:

- Fence;
- Gate;
- Feeder;
- Water Trough;
- Hay Storage;
- Chest;
- Stable Stall.

Feeder, Water Trough and Hay Storage accept matching resources through the normal Interact action when the player stands nearby and selects the correct hotbar slot.

### Horse care

- live hunger, thirst and energy;
- stronger drain during gallop;
- tamed horses automatically consume nearby stored hay and water;
- Stable Stall proximity improves rest recovery;
- nearest-horse needs are visible in the HUD;
- automatic care produces player feedback;
- forgiving rates keep infrastructure useful without turning the game into harsh survival management.

### Persistence

Save format v4 stores:

- 24-slot aggregate inventory and multi-stack totals;
- eight-slot hotbar and selected slot;
- typed structures, transforms and stored resource units;
- horse hunger, thirst and energy;
- all previous player, world, Pushik and horse relationship state.

Manual save, autosave, pause save, backgrounding, Save & Main Menu and clean disposal all enrich the active ranch snapshot with current Homestead state. Versions 1–3 continue to migrate safely, including real binary v3 coverage and backup recovery.

### New-ranch materials

New ranches start with Hay, Water Buckets and enough Stone for the first Water Trough after gathering additional Wood. Existing saves are never silently stripped or granted items.

### Honest current limitations

- Chest has no inventory window yet;
- Gate does not open or close yet;
- new structures do not yet block player/horse movement;
- models are provisional stylized primitives rather than final GLB assets.

These are explicit 0.5.2 tasks, not hidden promises in the current build.

See [`docs/releases/0.5.1.md`](docs/releases/0.5.1.md) and [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md).

## Current playable gameplay

- third-person movement and camera;
- procedural continuous terrain, lake, trees and rocks;
- resource gathering and persistent construction;
- horse personalities and trust/bond/fear;
- relationship-based taming;
- riding, gallop stamina and jumping;
- horse care infrastructure;
- day/night lighting;
- Pushik companion AI;
- Continue / New Game / Load Game / Settings / Exit;
- three ranch save slots with structure metadata;
- manual save, autosave and backup recovery.

## Pushik / Пушик

Pushik is the completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player. His affection and state survive restart.

## Input and accessibility

HORSEBOUND has a true in-memory pause lifecycle and device-local input profile.

- configurable keyboard bindings and conflict-safe key swapping;
- invert vertical camera;
- movement and camera stick dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible pause, settings and binding screens;
- dynamic Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- Homestead D-pad actions update the active prompt family correctly.

Input settings are stored in `%APPDATA%\HORSEBOUND\input.properties` and remain separate from ranch saves, Steam Cloud and the install depot.

## Display and Deck-oriented UX

- 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 profiles;
- UI scale from 100% to 150%;
- Low / Medium / High startup presets;
- optional rolling FPS/frame-time overlay;
- global scalable action-prompt strip;
- controller-readable menus, save slots, hotbar and build feedback.

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family and Valve compatibility testing remain required.

## Architecture

The 0.5.1 integration keeps the validated legacy ranch renderer while moving new responsibility into a dedicated wrapper:

```text
HomesteadRanchScreen
├── LivingRanchScreen presentation delegate
├── captured pure-Java GameSession
├── HomesteadRenderer / provisional models
├── hotbar + build input adapter
├── HorseCareSystem
└── owner-scoped save transformer
```

Temporary access to legacy camera/actor data is isolated in `LivingRanchTelemetryAdapter`; reflection does not enter domain code. This bridge will be removed as the legacy screen is split into explicit controllers and renderers.

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
- controller-only and Homestead packaged smoke-test contracts;
- honest store-feature discipline;
- third-party notices and exact license texts.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.1.md`](docs/releases/0.5.1.md)
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

CI verifies the complete controller, accessibility, save migration, Homestead domain and live integration runtime inside the packaged JAR.

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3
- gdx-controllers 2.2.4 / Jamepad
- Gradle
- JDK binary persistence
- JDK Properties settings
- Windows `jpackage`

## Licensing and ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md).

Third-party runtime components retain their own licenses. See [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) and the [`licenses/`](licenses/) directory.
