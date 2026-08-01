# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. Its world uses continuous terrain and is intentionally not voxel/block based.

## 0.5.0 — Homestead Domain Foundation

0.5.0 establishes the final persistent contract for the playable Homestead loop before its large rendering/input integration.

### Inventory and hotbar

- the player inventory has 24 stack slots;
- partial stacks fill before another slot is consumed;
- stack limits no longer cap the total amount owned;
- 150 wood, 45 apples and other multi-stack totals survive save/load intact;
- legacy inventories larger than 24 slots are preserved without deletion and temporarily block new stacks until space is freed;
- aggregate inventory can be split into immutable UI stack views;
- resources now include Wood, Stone, Apple, Carrot, Hay and Water Bucket;
- persistent eight-slot hotbar with selected slot, empty slots and safe unknown-ID handling;
- fresh ranches receive starter hay and water.

### Homestead structures

The typed structure catalog includes:

- Fence;
- Gate;
- Feeder;
- Water Trough;
- Hay Storage;
- Chest;
- Stable Stall.

Structures have persistent UUIDs, transforms, build costs, optional storage capacity and stored resource units. Placement checks the full recipe before consuming materials, so failed builds cannot partially charge the player. Duplicate persistent UUIDs are ignored during restoration to prevent ambiguous world objects.

### Horse needs and automatic care

Each horse has persistent hunger, thirst and energy. The pure-Java care system supports passive/activity drain, stronger gallop drain, autonomous feeding from nearby hay storage/feeders, autonomous watering from troughs and energy recovery near a stable stall.

The design is deliberately forgiving: building a good ranch automates routine care instead of creating a punishing survival chore loop.

### Save format v4

Save v4 persists:

- typed inventory totals;
- hotbar slots and selection;
- horse hunger, thirst and energy;
- typed placed structures and stored resources;
- all previous world, player, Pushik and horse relationship data.

Versions 1–3 migrate to v4. Legacy fences receive deterministic UUIDs, old horses receive healthy need defaults and old worlds receive the default hotbar. A real binary v3 fixture is migrated, rewritten and loaded again in tests.

### Honest scope boundary

The new Homestead domain is packaged and validated, but the current live ranch renderer still presents the 0.4.8 gameplay set. **0.5.1** connects visible hotbar selection, placement preview/snapping, feeders, troughs, gates, stalls, resource deposits and live horse-need feedback without another save-format rewrite.

See [`docs/releases/0.5.0.md`](docs/releases/0.5.0.md) and [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md).

## Current playable gameplay

- third-person movement and camera;
- procedural continuous terrain, lake, trees and rocks;
- gathering and persistent fence building;
- horse personalities and trust/bond/fear;
- relationship-based taming;
- riding, gallop stamina and jumping;
- day/night lighting;
- Pushik companion AI;
- Continue / New Game / Load Game / Settings / Exit;
- three ranch save slots;
- manual save, autosave and backup recovery.

## Pushik / Пушик

Pushik is the completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player. His affection and state survive restart.

## Input and accessibility

HORSEBOUND has a true in-memory pause lifecycle and a device-local input profile.

- configurable keyboard bindings;
- conflict-safe key swapping;
- invert vertical camera;
- movement and camera stick dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible pause, Settings Hub and input screens;
- dynamic keyboard/controller prompts;
- Xbox, PlayStation, Steam Deck, Nintendo and generic controller label families.

Input settings are stored separately:

```text
%APPDATA%\HORSEBOUND\input.properties
```

They are intentionally excluded from ranch saves, Steam Cloud and the install depot.

## Display and Deck-oriented UX

- 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 profiles;
- UI scale from 100% to 150%;
- Low / Medium / High startup presets;
- optional rolling FPS/frame-time overlay;
- global scalable action-prompt strip;
- controller-readable save slots and menus.

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family and Valve compatibility testing remain required.

## Fixed-step simulation

Gameplay runs at 60 deterministic simulation ticks per second. Render stalls and catch-up work are bounded, and tests compare behavior at 30, 60 and 144 render FPS.

The domain layer does not depend on render cadence and increasingly avoids direct physical-input queries.

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

Steam Auto-Cloud is planned for ranch `save.hbs` and `save.bak` files only. Display/input settings and diagnostics remain device-local.

## Support diagnostics

Privacy-conscious crash reports are stored locally under `%APPDATA%\HORSEBOUND\logs`.

- no automatic telemetry or upload;
- exact version and commit;
- OS, architecture, Java runtime, memory, thread and stack trace;
- user home/AppData path redaction;
- only the ten newest reports retained.

See [`docs/SUPPORT.md`](docs/SUPPORT.md).

## Steam readiness

HORSEBOUND is developed against SteamPipe, Steam Cloud, offline single-player and controller/Steam Deck requirements from the architecture stage.

- self-contained Windows x64 `jpackage` image;
- direct `HORSEBOUND.exe` launch with no mandatory external launcher;
- bundled Java runtime;
- no administrator requirement;
- user data outside the install depot;
- exact packaged build identity and SHA-256 manifest;
- third-party license bundle;
- controller-only packaged smoke-test contract;
- separate Homestead persistence/gameplay release contract;
- honest store-feature discipline.

Key documents:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/HOMESTEAD_RELEASE_CONTRACT.md`](docs/HOMESTEAD_RELEASE_CONTRACT.md)
- [`docs/releases/0.5.0.md`](docs/releases/0.5.0.md)
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

CI verifies tests, executable, JAR, bundled runtime, controller native runtime, Homestead domain runtime, licenses, exact version/commit and absence of mutable user data in the install image.

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
