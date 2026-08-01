# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.4 — Ranch Interaction Polish

This release removes the remaining compatibility facade and improves editing, camera behavior and procedural-audio control without changing save format v5.

### Direct ranch access

- `HomesteadRanchScreen` uses `RanchWorldAccess` directly;
- `LivingRanchTelemetryAdapter` is deleted from source and packaged runtime;
- immutable actor and horse telemetry remains the only information exposed by the base renderer;
- typed camera-obstacle updates connect the Homestead layer to third-person camera collision;
- CI rejects both source and packaged regressions of the obsolete adapter.

### Editing and undo

- nearby interactive structures receive a non-persistent translucent highlight;
- edit mode highlights the selected origin while showing a separate destination ghost;
- HUD feedback shows origin-to-destination coordinates;
- `U`, `Ctrl+Z` or the controller-accessible Pause menu undo the latest safe ranch edit;
- unchanged new placement returns its full recipe;
- relocation undo restores the previous transform only after current placement validation;
- changed, opened or filled structures cannot be incorrectly reverted;
- undo remains session-local and is not persisted into save v5.

### Camera and ranch audio

- third-person camera collision samples terrain and blocking Homestead structures;
- open Gates stop blocking both actors and the camera;
- camera pull-in is fast while return is smoother and slower;
- ranch interaction SFX volume is adjustable from 0% to 100%;
- old `settings.properties` files receive an 80% default;
- procedural waveforms are cached unchanged and scaled only during playback.

### Persistence

Save format remains **v5**. Existing ranches load without migration changes, preserving Gate state, Chest contents, moved structures, storage, inventory, hotbar, horse needs, Pushik and world progress.

See [`docs/releases/0.5.4.md`](docs/releases/0.5.4.md) and [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md).

## Current playable gameplay

- third-person movement and terrain/structure-aware camera;
- procedural continuous terrain, lake, trees and rocks;
- resource gathering and persistent construction;
- visible hotbar and placement preview;
- feeders, troughs, hay storage, Chests, Gates and Stalls;
- persistent one-item, stack and full-type Chest transfers;
- highlighted selection and origin-to-ghost move feedback;
- movable, undoable and safely dismantled structures;
- player and horse structure collisions;
- animated opening and closing Gates;
- horse personalities and trust/bond/fear;
- relationship-based taming;
- riding, gallop stamina and jumping;
- hunger, thirst, energy and automatic ranch care;
- day/night lighting;
- Pushik companion AI;
- procedural interaction sounds with device-local volume;
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
- controller-accessible pause, settings, bindings, inventory, build/edit and undo UX;
- dynamic Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- context-aware D-pad actions avoid Inventory/blueprint conflicts;
- held directional navigation repeats while Confirm/Back remain edge-only.

Input settings are stored in `%APPDATA%\HORSEBOUND\input.properties` and remain separate from ranch saves, Steam Cloud and the install depot.

## Display, audio and Deck-oriented UX

- 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 profiles;
- UI scale from 100% to 150%;
- ranch effects volume from 0% to 100%;
- Low / Medium / High startup presets;
- optional rolling FPS/frame-time overlay;
- global scalable action-prompt strip;
- controller-readable menus, save slots, hotbar, inventory and build feedback.

Display and ranch-audio settings are stored in device-local `%APPDATA%\HORSEBOUND\settings.properties` and remain excluded from ranch saves and Steam Cloud.

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family and Valve compatibility testing remain required.

## Architecture

```text
HomesteadRanchScreen
├── LivingRanchScreen : RanchWorldAccess
├── captured pure-Java GameSession
├── InventoryOverlay + InventoryTransferService
├── HomesteadCollisionSystem
├── RanchUndoManager
├── HomesteadRenderer
│   ├── selected-structure overlay
│   ├── GateAnimationState
│   ├── RanchPresentationObserver
│   └── shared procedural RanchAudio
├── RanchCameraCollisionSystem
├── hotbar + build/edit input adapter
├── HorseCareSystem
└── owner-scoped save transformer
```

Domain and persistence code do not use reflection or libGDX rendering. `RanchWorldAccess` is the single narrow presentation-only boundary; no telemetry compatibility facade remains.

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

Steam Auto-Cloud is planned for ranch `save.hbs` and `save.bak` only. Display, audio and input settings plus diagnostics remain device-local.

## Steam readiness

- self-contained Windows x64 `jpackage` image;
- direct `HORSEBOUND.exe` launch without a mandatory launcher;
- bundled Java runtime;
- exact packaged version/commit and SHA-256 manifest;
- no mutable user files inside the depot;
- controller, migration, storage, collision, camera and typed-ranch package gates;
- CI rejection of obsolete adapter regressions;
- third-party notices and exact license texts.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.4.md`](docs/releases/0.5.4.md)
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

CI verifies controller/accessibility, save migration, inventory/storage, typed ranch access, transactional undo, camera collision, procedural audio and live Homestead runtime inside the packaged JAR.

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
