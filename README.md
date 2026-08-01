# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.5 — Ranch Workflow & Camera Hardening

This release makes destructive ranch actions clearer, expands the third-person camera to procedural nature and separates interaction effects from meadow ambience without changing save format v5.

### Safer editing workflow

- the live HUD shows whether the latest build or move can still be undone;
- Pause visibly enables or disables **Undo Last Ranch Edit**;
- dismantling now requires two Mount/Y presses within a four-second confirmation window;
- the confirmation is bound to the selected structure UUID and operational revision;
- selection changes, expiry or any later structure mutation invalidate stale confirmation;
- no world or inventory mutation occurs on the first dismantle press.

### Nature-aware camera

- camera collision now includes unharvested trees and rocks as well as terrain and blocking Homestead structures;
- nature collision geometry follows each procedural prop's visible scale;
- harvested trees immediately leave the camera-obstacle set;
- trees and rocks directly between the actor and camera fade softly instead of hiding the player;
- per-instance material isolation prevents one faded prop from changing every tree or rock;
- open Gates remain non-blocking for movement and camera collision.

### Independent ranch audio buses

- ranch interaction SFX and meadow ambience have separate 0–100% controls;
- old `settings.properties` files receive safe defaults of 80% SFX and 45% ambience;
- day phases use a procedural Meadow Breeze profile;
- night phases use a procedural Night Crickets profile;
- deterministic bounded scheduling avoids constant audio spam;
- all audio settings remain device-local and outside save v5 and Steam Cloud.

### Persistence

Save format remains **v5**. Existing ranches load without migration changes, preserving Gate state, Chest contents, moved structures, storage, inventory, hotbar, horse needs, Pushik and world progress.

See [`docs/releases/0.5.5.md`](docs/releases/0.5.5.md) and [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md).

## Current playable gameplay

- third-person movement with terrain, structure, tree and rock camera collision;
- soft camera-occluder fading for procedural trees and rocks;
- procedural continuous terrain, lake, trees and rocks;
- resource gathering and persistent construction;
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
- riding, gallop stamina and jumping;
- hunger, thirst, energy and automatic ranch care;
- day/night lighting;
- Pushik companion AI;
- procedural interaction sounds plus day/night ambience on independent buses;
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
- two-step controller dismantle confirmation;
- dynamic Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- context-aware D-pad actions avoid Inventory/blueprint conflicts;
- held directional navigation repeats while Confirm/Back remain edge-only.

Input settings are stored in `%APPDATA%\HORSEBOUND\input.properties` and remain separate from ranch saves, Steam Cloud and the install depot.

## Display, audio and Deck-oriented UX

- 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 profiles;
- UI scale from 100% to 150%;
- ranch interaction effects from 0% to 100%;
- meadow ambience from 0% to 100%;
- Low / Medium / High startup presets;
- optional rolling FPS/frame-time overlay;
- global scalable action-prompt strip;
- controller-readable menus, save slots, hotbar, inventory and build feedback.

Display and audio settings are stored in device-local `%APPDATA%\HORSEBOUND\settings.properties` and remain excluded from ranch saves and Steam Cloud.

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family, transparency/GPU and Valve compatibility testing remain required.

## Architecture

```text
HomesteadRanchScreen
├── LivingRanchScreen : RanchWorldAccess
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

## Steam readiness

- self-contained Windows x64 `jpackage` image;
- direct `HORSEBOUND.exe` launch without a mandatory launcher;
- bundled Java runtime;
- exact packaged version/commit and SHA-256 manifest;
- no mutable user files inside the depot;
- controller, migration, storage, collision, camera, audio-bus and typed-ranch package gates;
- CI rejection of obsolete adapter regressions;
- third-party notices and exact license texts.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.5.md`](docs/releases/0.5.5.md)
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

CI verifies controller/accessibility, save migration, inventory/storage, typed ranch access, transactional undo, dismantle confirmation, nature-aware camera behavior, independent audio buses and live Homestead runtime inside the packaged JAR.

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3
- gdx-controllers 2.2.4 / Jamepad
- Gradle
- JDK binary persistence
- JDK Properties settings
- procedural mono interaction and ambience audio
- Windows `jpackage`

## Licensing and ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md).

Third-party runtime components retain their own licenses. See [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) and the [`licenses/`](licenses/) directory.
