# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.7 — Controls, Installer & Visual Foundation

This release responds directly to the first successful manual 0.5.6 playtest. It fixes mirrored camera-relative keyboard strafing, adds a real Windows installer alongside the portable build and introduces the first restrained visual-foundation pass without changing save format v5.

### Correct camera-relative movement

- `A` now moves visibly left relative to the current camera;
- `D` now moves visibly right relative to the current camera;
- the correction is shared by keyboard and controller command processing rather than swapping user bindings;
- cardinal camera directions are locked by regression tests;
- existing device-local rebinding files remain compatible.

### Real Windows delivery

- CI now produces a normal per-user Windows `.exe` installer;
- the installer offers a destination chooser, Start Menu entry and desktop shortcut;
- a stable upgrade UUID allows later HORSEBOUND installers to update the same installation;
- installation does not require administrator privileges;
- the self-contained portable app-image remains available for Steam depot and no-install use;
- both packages include the Java runtime and are independently hashed.

The installer is not code-signed yet. Windows SmartScreen may therefore show an unknown-publisher warning until a production signing certificate is introduced.

### First visual-foundation pass

- terrain mesh resolution is increased from a 4 m to a 3 m grid;
- vertex colors create meadow, highland, shoreline and lake-bed variation;
- terrain slope receives restrained natural shading;
- player, horse, Pushik, trees, rocks, fences and water receive more detailed procedural geometry;
- materials now distinguish matte, satin and reflective surfaces;
- water receives smoother geometry and stronger specular response;
- Pushik remains completely black, fluffy and quiet.

This is still procedural placeholder art, not the final commercial asset set. Production visuals require authored GLB models, skeletal animation, PBR textures, shadows, atmosphere and a dedicated water/sky pipeline.

Save format remains **v5**. No migration is required.

See [`docs/releases/0.5.7.md`](docs/releases/0.5.7.md), [`docs/VISUAL_ASSET_ROADMAP.md`](docs/VISUAL_ASSET_ROADMAP.md) and [`docs/WINDOWS_INSTALLATION.md`](docs/WINDOWS_INSTALLATION.md).

## Current playable gameplay

- third-person movement with corrected camera-relative strafing;
- terrain, structure, tree and rock camera collision;
- soft camera-occluder fading for procedural trees and rocks;
- smoother color-varied continuous terrain, lake, trees and rocks;
- resource gathering and persistent construction with tree, rock and horse placement exclusion;
- visible hotbar and placement preview;
- feeders, troughs, hay storage, Chests, Gates and Stalls;
- persistent one-item, stack and full-type Chest transfers;
- highlighted selection and origin-to-ghost move feedback;
- visible session-local undo state;
- movable structures and confirmed safe dismantling;
- player and horse structure collisions;
- animated opening and closing Gates;
- horse personalities and trust/bond/fear;
- relationship-based taming;
- riding, gallop stamina, jumping and safe-ground dismounting;
- hunger, thirst, energy and automatic ranch care;
- day/night lighting;
- Pushik companion AI with safe catch-up placement;
- procedural interaction sounds plus day/night ambience on independent buses;
- Continue / New Game / Load Game / Settings / Exit;
- three ranch save slots with continuous-selection overwrite confirmation;
- manual save, autosave, ranch-replacement backup safety and recovery.

## Pushik / Пушик

Pushik is the completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player. His affection and state survive restart.

## Input and accessibility

HORSEBOUND has a true in-memory pause lifecycle and device-local input profile.

- configurable keyboard bindings and conflict-safe key swapping;
- corrected camera-relative left/right movement;
- dedicated Inventory binding with legacy-profile fallback;
- invert vertical camera;
- movement and camera stick dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible pause, settings, bindings, inventory, build/edit and undo UX;
- two-step controller dismantle confirmation;
- dynamic Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- context-aware D-pad actions avoid Inventory/blueprint conflicts;
- held directional navigation repeats while Confirm/Back remain edge-only;
- controller action edges are re-primed safely across menus, gameplay and reconnects.

Input settings are stored in `%APPDATA%\HORSEBOUND\input.properties` and remain separate from ranch saves, Steam Cloud and the install depot.

## Display, audio and Deck-oriented UX

- 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 profiles;
- UI scale from 100% to 150%;
- Low / Medium / High presets with 0× / 2× / 4× MSAA;
- smoother terrain geometry and vertex-color biome variation;
- refined procedural character and environment models;
- ranch interaction effects from 0% to 100%;
- meadow ambience from 0% to 100%;
- optional rolling FPS/frame-time overlay;
- global scalable action-prompt strip;
- controller-readable menus, save slots, hotbar, inventory and build feedback.

Display and audio settings are stored in device-local `%APPDATA%\HORSEBOUND\settings.properties` and remain excluded from ranch saves and Steam Cloud.

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family, transparency/GPU and Valve compatibility testing remain required.

## Architecture

```text
HomesteadRanchScreen
├── LivingRanchScreen : RanchWorldAccess
│   ├── CameraRelativeAxes
│   ├── RanchCameraCollisionSystem
│   ├── RanchCameraFadeSystem
│   └── typed tree / rock camera geometry
├── captured pure-Java GameSession
├── InventoryOverlay + InventoryTransferService
├── HomesteadCollisionSystem
├── RanchUndoManager
├── RanchDismantleConfirmation
├── HomesteadRenderer
│   ├── selected-structure overlay
│   ├── GateAnimationState
│   └── RanchPresentationObserver
├── RanchAudio
│   ├── SFX bus
│   └── ambience bus + RanchAmbience scheduler
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

## Windows distribution

Two Windows x64 packages are produced:

```text
HORSEBOUND-Installer-Windows-x64
    HORSEBOUND-0.5.7.exe

HORSEBOUND-Portable-Windows-x64
    HORSEBOUND.exe
    app/
    runtime/
```

Use the installer for an ordinary desktop installation. Use the portable package for Steam depot preparation, diagnostics or a no-install copy.

## Steam readiness

- self-contained Windows x64 `jpackage` app-image;
- separate per-user Windows installer;
- direct `HORSEBOUND.exe` launch without a mandatory launcher;
- bundled Java runtime;
- exact packaged version/commit and SHA-256 manifests;
- no mutable user files inside the depot;
- controller, migration, storage, collision, camera, audio-bus and typed-ranch package gates;
- CI rejection of obsolete adapter regressions;
- third-party notices and exact license texts.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.7.md`](docs/releases/0.5.7.md)
- [`docs/WINDOWS_INSTALLATION.md`](docs/WINDOWS_INSTALLATION.md)
- [`docs/VISUAL_ASSET_ROADMAP.md`](docs/VISUAL_ASSET_ROADMAP.md)
- [`steam/README.md`](steam/README.md)

## Build

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
gradle test
```

Windows portable image and installer:

```powershell
gradle clean test windowsImage windowsInstaller
```

Outputs:

```text
build/jpackage/HORSEBOUND/HORSEBOUND.exe
build/installer/HORSEBOUND-0.5.7.exe
```

CI verifies controller/accessibility, camera-relative movement, save migration, inventory/storage, typed ranch access, transactional undo, dismantle confirmation, nature-aware camera behavior, independent audio buses, portable packaging and installer generation.

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3
- gdx-controllers 2.2.4 / Jamepad
- Gradle
- JDK binary persistence
- JDK Properties settings
- procedural mono interaction and ambience audio
- Windows `jpackage` app-image and EXE installer

## Licensing and ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md).

Third-party runtime components retain their own licenses. See [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) and the [`licenses/`](licenses/) directory.
