# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. The world uses continuous terrain and is intentionally not voxel/block based.

## 0.4.3 — Gameplay Loop Integration

The active Living Ranch gameplay path now runs through a device-neutral fixed-step simulation loop.

### Live fixed-step gameplay

At 60 deterministic simulation ticks per second HORSEBOUND now updates:

- player movement and jumping;
- mounted horse acceleration, steering, stamina and jumping;
- wild horse personality, fear, wandering and fleeing;
- Pushik companion behavior;
- interaction, taming, mounting and building;
- world time.

Render stalls are capped, catch-up work is bounded and tests compare behavior at 30, 60 and 144 render FPS.

### Device-neutral input

- `PlayerCommand` models intent instead of key codes;
- `InputMapper` converts physical devices into commands;
- `GameSimulationLoop` bridges render-rate sampling and fixed simulation;
- continuous movement persists between ticks;
- jump/interact/mount/build/save/pause edges execute exactly once;
- active device identity reaches the HUD/prompt layer;
- keyboard/mouse is the current adapter;
- gamepad and Steam Input will reuse the same gameplay code.

Controller glyph mapping is still in progress and is not claimed as complete controller support yet.

## Persistent worlds

Save format v3 stores:

- player position and facing;
- typed inventory stacks;
- world seed and time;
- harvested resources and constructed fences;
- horse UUID, position, personality, trust, bond, fear, stamina and taming state;
- Pushik position, heading, affection and companion state.

Version 1 and 2 ranches migrate to v3. Saves use temporary writes, disk flush, atomic replacement where supported and `save.bak` recovery.

## Pushik / Пушик

Pushik is the required completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player. His affection and state survive restart.

## Current gameplay

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

## Controls

| Input | Action |
|---|---|
| W A S D | Move / steer |
| Mouse | Camera |
| Shift | Sprint / gallop |
| Space | Jump |
| E | Interact |
| F | Mount / dismount |
| B | Build fence |
| F5 | Save |
| Esc | Save and menu |

Controller-complete menus and gameplay remain a Steam Deck target.

## User data

```text
%APPDATA%\HORSEBOUND\
    settings.properties
    saves\
        slot-1\save.hbs + save.bak
        slot-2\save.hbs + save.bak
        slot-3\save.hbs + save.bak
```

Ranch saves are prepared for Steam Auto-Cloud. Device-specific settings remain local.

## Steam readiness

HORSEBOUND is developed against SteamPipe, Steam Cloud, offline single-player and Steam Deck requirements from the architecture stage.

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/releases/0.4.2.md`](docs/releases/0.4.2.md)
- [`docs/releases/0.4.3.md`](docs/releases/0.4.3.md)
- [`steam/README.md`](steam/README.md)

The planned Steam launch target is `HORSEBOUND.exe` directly, without a mandatory external launcher.

## Build

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
gradle test
```

Windows self-contained build:

```powershell
gradle clean test windowsImage
```

```text
build/jpackage/HORSEBOUND/HORSEBOUND.exe
```

The packaged game includes its Java runtime. CI verifies the executable, app JAR, runtime, absence of mutable user data and SHA-256 hashes.

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3
- Gradle
- JDK binary persistence
- JDK Properties settings
- jpackage Windows app image

## Ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md).
