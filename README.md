# HORSEBOUND

**HORSEBOUND is created by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

A cozy stylized 3D Java sandbox about horses, exploration, taming, riding, gathering and building a small ranch. The world is intentionally **not voxel/block based**: terrain is a continuous procedural mesh and characters use stylized low-poly 3D forms.

## HORSEBOUND 0.4.1 Stabilization

0.4.1 hardens the Living Ranch foundation before Homestead content expands.

### Runtime architecture

- `GameSession` owns world seed, world clock, typed ranch inventory and Pushik's companion mind;
- `Inventory` and `ItemId` are pure Java domain models;
- `HorsePersonality` and `HorseRelationship` keep horse identity, trust, bond and fear outside rendering;
- `PushikMind` owns companion behavior and affection;
- `LivingRanchScreen` is the active libGDX presentation/input layer;
- the superseded legacy `WorldScreen` has been removed;
- a documented fixed-step simulation/system split is the next architecture milestone.

### Persistence v3

The JDK-only `.hbs` save format is now version 3.

Persistent state includes:

- player position and facing;
- typed inventory stacks;
- world seed and time;
- harvested trees and built fences;
- every horse UUID, position, personality, trust, bond, fear, stamina and taming state;
- Pushik position, heading, affection and current companion state.

Version 1 and version 2 ranches remain readable. Compatibility migrations are covered by explicit binary fixture tests. Successful future saves rewrite migrated ranches as v3.

Writes use a temporary file, disk flush, atomic replacement where supported and a recoverable `save.bak`. A corrupted primary save is never copied over a valid backup.

### Pushik / Пушик

Pushik remains the completely black fluffy cat with fluffy black paws and near-silent footsteps. His current states are:

- `FOLLOW`;
- `SIT`;
- `EXPLORE`;
- `SLEEP`;
- `GREET`.

Petting raises affection, following and reunions affect behavior, and his persistent companion state survives restart.

### Current gameplay foundation

- third-person movement and mouse camera;
- continuous procedural terrain, hills, lake, trees and rocks;
- gathering and persistent fence building;
- wild horse wandering/fleeing behavior;
- horse personality + trust/bond/fear;
- relationship-based taming;
- mounting, gallop stamina and jumping;
- day/night lighting;
- Pushik companion AI;
- Continue / New Game / Load Game / Settings / Exit flow;
- three independent ranch save slots;
- manual save with `F5`, configurable autosave, save on pause/exit;
- persistent VSync and mouse sensitivity settings;
- self-contained Windows packaging with `HORSEBOUND.exe` and bundled Java runtime.

## Controls

| Key | Action |
|---|---|
| W A S D | Move / steer horse |
| Mouse | Third-person camera |
| Shift | Sprint / gallop |
| Space | Jump |
| E | Interact, feed horse, pet Pushik, gather wood |
| F | Mount / dismount a tamed horse |
| B | Build a fence segment (2 wood) |
| F5 | Save game |
| Esc | Save and return to menu |

Controller-first input abstraction is scheduled before Homestead grows so all gameplay and menus can target Steam Deck/Steam Controller requirements without duplicating logic.

## User data

On Windows:

```text
%APPDATA%\HORSEBOUND\
    settings.properties
    saves\
        slot-1\save.hbs + save.bak
        slot-2\save.hbs + save.bak
        slot-3\save.hbs + save.bak
```

World saves are designed for Steam Auto-Cloud. Device-specific settings remain local and must not be cloud-synced between desktop and Steam Deck.

## Steam readiness

HORSEBOUND is being developed against Steam release, SteamPipe, Steam Cloud and Steam Deck expectations from the architecture stage.

See:

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`steam/README.md`](steam/README.md)
- SteamPipe templates in [`steam/`](steam/)

The planned Steam launch target is `HORSEBOUND.exe` directly, without a required external launcher. Single-player gameplay must remain available offline.

## Run and test

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
gradle test
```

## Windows build

GitHub Actions runs all tests before packaging and verifies that `HORSEBOUND.exe` exists.

```powershell
gradle clean test windowsImage
```

Output:

```text
build/jpackage/HORSEBOUND/HORSEBOUND.exe
```

No system Java installation is required by the packaged build because `jpackage` includes its own runtime image.

## Tech

- Java 21 gameplay/domain/application code
- libGDX 1.14.2
- LWJGL3 desktop backend
- Gradle
- JDK `java.nio` binary persistence
- JDK `Properties` application settings
- `jpackage` Windows app-image

## Ownership

HORSEBOUND is an original project created by **Dimash Janibekov (DizZyZ7)**. See [LICENSE](LICENSE), [NOTICE.md](NOTICE.md) and repository history. Public repository access does not grant reuse rights.
