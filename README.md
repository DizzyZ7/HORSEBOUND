# HORSEBOUND

**HORSEBOUND is created by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov (DizZyZ7). All rights reserved.

A cozy stylized 3D Java sandbox about horses, exploration, taming, riding, gathering,
and building a small ranch. The world is intentionally **not voxel/block based**:
terrain is a continuous procedural mesh and characters are simple low-poly 3D forms.

## HORSEBOUND 0.2 Foundation

The current build is moving from a vertical slice toward a real persistent single-player game.

- third-person 3D movement and mouse camera;
- smooth procedural terrain with hills, a lake, trees and resource gathering;
- several wild horses with wandering/fleeing behavior;
- persistent UUID identity for horses;
- trust-based taming using apples;
- mounting, riding, gallop stamina and jumping;
- simple fence building from gathered wood;
- time-of-day lighting;
- **Pushik / Пушик**, a completely black fluffy cat with fluffy paws who follows the player quietly;
- Continue / New Game flow;
- manual save with `F5`;
- autosave every 60 seconds;
- save on pause and exit;
- player position, resources, world time, harvested trees, fences, horses and Pushik survive restart;
- versioned JDK-only `.hbs` save format;
- atomic save replacement plus `save.bak` recovery;
- Windows self-contained app-image packaging with `HORSEBOUND.exe` and bundled Java runtime.

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

## Saves

HORSEBOUND keeps save DTOs separate from runtime/rendering objects. The current save format is versioned independently so future updates can migrate old worlds instead of invalidating them.

On Windows the default save slot is stored under:

```text
%APPDATA%\HORSEBOUND\saves\slot-1\
    save.hbs
    save.bak
```

Writes go through a temporary file, are flushed to disk, and replace the primary save atomically where the filesystem supports it. If the primary save is unreadable, HORSEBOUND attempts to recover from `save.bak`.

## Run from source

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
```

The project uses libGDX 1.14.2. Gameplay, world logic, persistence and application code are Java 21.

## Tests

```bash
gradle test
```

Persistence tests cover complete save/load round trips and recovery from a deliberately corrupted primary save.

## Windows build

A Windows build is produced by GitHub Actions on every push to `main`, with tests running before packaging.
Download the `HORSEBOUND-Windows-x64` artifact and extract it. Start the game with:

```text
HORSEBOUND/HORSEBOUND.exe
```

No system Java installation is required for that packaged build because `jpackage`
includes a runtime image next to the executable.

To package locally on Windows with JDK 21:

```powershell
gradle clean test windowsImage
```

Output:

```text
build/jpackage/HORSEBOUND/HORSEBOUND.exe
```

## Tech

- Java 21
- libGDX 1.14.2
- LWJGL3 desktop backend
- Gradle
- JDK `java.nio` / binary persistence layer
- procedural runtime-generated prototype visuals
- `jpackage` Windows app-image

## Ownership

HORSEBOUND is an original project created by **Dimash Janibekov (DizZyZ7)**.
See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md). The repository is public for
visibility and development history; public access does not grant reuse rights.
