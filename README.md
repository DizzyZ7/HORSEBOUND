# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. The world uses continuous terrain and is intentionally not voxel/block based.

## 0.4.4 — Controller Foundation

HORSEBOUND now has a standardized controller path across live gameplay and the core menu flow.

### Controller gameplay

The official `gdx-controllers` desktop backend provides standardized mappings instead of device-specific numeric button guesses.

- radial movement and camera dead zones;
- hot-plug and disconnect recovery;
- mixed keyboard/mouse + controller input;
- active prompt device changes only after meaningful input;
- bounds checks for incomplete controller layouts;
- controller-native runtime verified inside the packaged JAR.

Default gameplay bindings:

| Controller input | Action |
|---|---|
| Left Stick | Move / steer |
| Right Stick | Camera |
| A | Jump |
| X | Interact |
| Y | Mount / dismount |
| L1 | Build |
| R1 | Sprint / gallop |
| Back / View | Manual save |
| Start / Menu or B | Pause / back |

### Controller menu flow

Controller navigation is available in:

- main menu;
- New Game ranch slots;
- Load Game ranch slots;
- overwrite confirmation;
- Settings.

D-pad or Left Stick navigates, A confirms and B returns. Keyboard and mouse remain fully supported.

This is not yet a claim of Steam Deck Verified status. Physical-device testing, platform-specific graphical glyphs, 1280×800 readability, performance validation and Valve compatibility review remain required.

## Fixed-step gameplay

At 60 deterministic simulation ticks per second HORSEBOUND updates:

- player movement and jumping;
- mounted horse acceleration, steering, stamina and jumping;
- wild horse personality, fear, wandering and fleeing;
- Pushik companion behavior;
- interaction, taming, mounting and building;
- world time.

Render stalls are capped, catch-up work is bounded and tests compare behavior at 30, 60 and 144 render FPS.

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

## Keyboard controls

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
- [`docs/releases/0.4.4.md`](docs/releases/0.4.4.md)
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

The packaged game includes its Java runtime. CI verifies:

- executable, application JAR and runtime;
- standardized controller classes and `jamepad64.dll`;
- third-party notices and exact license texts;
- absence of mutable user data inside the install image;
- SHA-256 package hashes.

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3
- gdx-controllers 2.2.4 / Jamepad desktop backend
- Gradle
- JDK binary persistence
- JDK Properties settings
- jpackage Windows app image

## Licensing and ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md).

Third-party runtime components retain their own licenses. See [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) and the [`licenses/`](licenses/) directory.
