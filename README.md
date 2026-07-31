# HORSEBOUND

**HORSEBOUND is created by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov (DizZyZ7). All rights reserved.

A cozy stylized 3D Java sandbox about horses, exploration, taming, riding, gathering and building a small ranch. The world is intentionally **not voxel/block based**: terrain is a continuous procedural mesh and characters use stylized low-poly 3D forms.

## HORSEBOUND 0.4 Living Ranch Foundation

0.4 starts separating the actual game domain from the renderer/input layer so HORSEBOUND can grow without turning one screen class into the entire game.

### Runtime architecture

- `GameSession` owns world seed, world clock, ranch inventory and Pushik's companion mind;
- `Inventory` is a pure Java typed item model instead of raw `wood` / `apples` fields in the renderer;
- `ItemId` defines current item identities and stack limits;
- `PushikMind` is a pure Java companion state machine;
- `LivingRanchScreen` is now the active 3D gameplay presentation/input layer;
- the previous `WorldScreen` remains temporarily as a reference implementation until the new screen is fully validated and can be removed in a cleanup release.

### Pushik / Пушик

Pushik is still the required completely black fluffy cat with fluffy black paws and near-silent footsteps. He now has behavior states:

- `FOLLOW`;
- `SIT`;
- `EXPLORE`;
- `SLEEP`;
- `GREET`.

Petting increases affection and triggers a greeting state. Pushik follows when the player moves away, can quietly explore nearby, settles when idle and sleeps near the player at night. His current state and affection are visible in the HUD.

### Inventory and ranch gameplay

Wood and apples now flow through `Inventory`. Gathering adds typed wood items, horse feeding consumes apples, and fence construction consumes two wood only after a valid placement is found. This is the foundation for later item definitions, crafting, equipment, storage, hay, tack and stable construction.

### Horses

Every persistent horse keeps:

- UUID identity;
- personality: `CALM`, `CURIOUS`, `SHY`, `BRAVE`, `STUBBORN`, or `ENERGETIC`;
- trust;
- bond;
- fear;
- stamina and taming state.

Personality affects relationship progression and fear response. Taming requires complete trust and a sufficiently calm horse.

## Current game foundation

- third-person 3D movement and mouse camera;
- continuous procedural terrain, hills, lake, trees and rocks;
- gathering and persistent fence building;
- wild horse wandering/fleeing behavior;
- horse personality + trust/bond/fear;
- mounting, gallop stamina and jumping;
- day/night lighting;
- Pushik companion AI states;
- Steam-like Continue / New Game / Load Game / Settings / Exit flow;
- three independent ranch save slots;
- manual save with `F5`, configurable autosave, save on pause/exit;
- safe binary `.hbs` saves with atomic replacement and backup recovery;
- v1-to-v2 world migration;
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

## Run and test

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
gradle test
```

## Windows build

GitHub Actions runs tests before packaging and verifies that `HORSEBOUND.exe` exists.

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
