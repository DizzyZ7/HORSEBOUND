# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.2 — Inventory, Physics & Building UX

The Homestead loop now includes persistent storage, editable construction and structure physics.

### Inventory and Chest

- the player backpack supports 24 visual stack slots;
- Inventory is a rebindable keyboard action stored in device-local `input.properties`;
- controller D-pad Up opens the backpack outside placement mode;
- Chest provides 12 persistent item-stack slots;
- the two-panel overlay keeps the ranch alive in memory;
- Left/Right changes panel and Confirm transfers one item;
- transfers are atomic, so a full destination never deletes or partially moves items;
- old input profiles receive the default Inventory binding without losing prior settings.

### Gates and structure physics

- Interact opens and closes nearby Gates;
- Gate state survives save/load;
- open Gates stop blocking movement;
- closed Gates, fences and other structures block the player, mounted horse and autonomous horses;
- swept-circle collision prevents tunnelling during large frame steps;
- structure collision radii are owned by the typed structure catalog.

### Building edit mode

- `M` or controller D-pad Down selects the nearest editable structure;
- existing rotation controls remain active;
- Build confirms relocation without charging the recipe again;
- Mount/Y dismantles the selected structure;
- dismantling refunds half the construction materials;
- non-empty storage cannot be dismantled;
- dismantling is refused when the backpack cannot accept the refund;
- legacy fences remain stable collision participants but are not movable until their old renderer is retired.

### Save format v5

Save v5 persists:

- Gate open/closed state;
- Chest item contents;
- moved structure transforms;
- feeder/trough storage;
- inventory, hotbar, horse needs and all earlier ranch state.

Versions 1–4 continue to migrate. Real binary v4 coverage proves that old Gates load closed and old Chests load empty before being rewritten as v5. Manual save, autosave, pause save, application backgrounding and clean exit all capture current operational state.

### Current presentation boundary

Models remain provisional stylized primitives. The validated `LivingRanchScreen` still renders and controls the base world, while `HomesteadRanchScreen` owns the 0.5.2 inventory, edit and physics layer. Temporary actor-transform access remains isolated in `LivingRanchTelemetryAdapter`; it has not been falsely presented as already removed.

See [`docs/releases/0.5.2.md`](docs/releases/0.5.2.md) and [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md).

## Current playable gameplay

- third-person movement and camera;
- procedural continuous terrain, lake, trees and rocks;
- resource gathering and persistent construction;
- visible hotbar and placement preview;
- feeders, troughs, hay storage, Chests, Gates and Stalls;
- persistent inventory and chest transfers;
- movable and safely dismantled structures;
- player and horse structure collisions;
- horse personalities and trust/bond/fear;
- relationship-based taming;
- riding, gallop stamina and jumping;
- hunger, thirst, energy and automatic ranch care;
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
- dedicated Inventory binding with legacy-profile fallback;
- invert vertical camera;
- movement and camera stick dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible pause, settings, bindings, inventory and build/edit UX;
- dynamic Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- context-aware D-pad actions avoid Inventory/blueprint conflicts.

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

The 0.5.x integration keeps the validated legacy ranch renderer while new responsibility lives in a dedicated wrapper:

```text
HomesteadRanchScreen
├── LivingRanchScreen presentation delegate
├── captured pure-Java GameSession
├── InventoryOverlay
├── HomesteadCollisionSystem
├── HomesteadRenderer / provisional models
├── hotbar + build/edit input adapter
├── HorseCareSystem
└── owner-scoped save transformer
```

Domain code does not use reflection or libGDX rendering. Temporary access to legacy camera/actor data is isolated in `LivingRanchTelemetryAdapter` and remains scheduled for replacement by explicit ranch-access interfaces.

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
- controller-only, migration, storage and Homestead packaged gates;
- honest store-feature discipline;
- third-party notices and exact license texts.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.2.md`](docs/releases/0.5.2.md)
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

CI verifies the controller, accessibility, save migration, inventory/storage, collision and live Homestead runtime inside the packaged JAR.

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
