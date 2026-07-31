# HORSEBOUND

**HORSEBOUND is created by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov (DizZyZ7). All rights reserved.

A cozy stylized 3D Java sandbox about horses, exploration, taming, riding, gathering,
and building a small ranch. The world is intentionally **not voxel/block based**:
terrain is a continuous procedural mesh and characters are simple low-poly 3D forms.

## HORSEBOUND 0.2.1 Application Foundation

HORSEBOUND is moving from a vertical slice toward a real persistent single-player game.

- third-person 3D movement and mouse camera;
- smooth procedural terrain with hills, a lake, trees and resource gathering;
- several wild horses with wandering/fleeing behavior;
- persistent UUID identity for horses;
- trust-based taming using apples;
- mounting, riding, gallop stamina and jumping;
- simple fence building from gathered wood;
- time-of-day lighting;
- **Pushik / Пушик**, a completely black fluffy cat with fluffy paws who follows the player quietly;
- Steam-like main menu flow: Continue / New Game / Load Game / Settings / Exit;
- three independent ranch save slots with metadata;
- Continue automatically opens the most recently saved valid ranch;
- overwrite protection for occupied ranch slots;
- manual save with `F5`;
- configurable autosave interval from 30 to 300 seconds;
- save on pause and exit;
- player position, resources, world time, harvested trees, fences, horses and Pushik survive restart;
- versioned JDK-only `.hbs` save format;
- atomic save replacement plus `save.bak` recovery;
- persistent VSync and mouse-sensitivity settings stored separately from ranch saves;
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

Main-menu shortcuts include `Enter`, `N`, `L`, `S`, and `Esc`. Save-slot screens also support keys `1`, `2`, and `3`.

## Saves

HORSEBOUND keeps save DTOs separate from runtime/rendering objects. The save format is versioned independently so future updates can migrate old worlds instead of invalidating them.

On Windows user data is stored under:

```text
%APPDATA%\HORSEBOUND\
    settings.properties
    saves\
        slot-1\
            save.hbs
            save.bak
        slot-2\
            save.hbs
            save.bak
        slot-3\
            save.hbs
            save.bak
```

Each ranch is independent. The Load Game screen shows its last-save time and current horse/tamed-horse/fence counts. Continue selects the newest readable slot.

Writes go through a temporary file, are flushed to disk, and replace the primary save atomically where the filesystem supports it. The previous valid state is retained as `save.bak`; if the primary save is unreadable, HORSEBOUND attempts to recover from the backup.

## Settings

Application settings are deliberately stored outside ranch saves. A player can change them without modifying world progress.

Current persistent settings:

- VSync;
- mouse sensitivity;
- autosave interval.

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

Tests cover save/load round trips, recovery from deliberately corrupted primary saves, first-save backups, save-slot metadata/selection and settings persistence/fallback behavior.

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
- JDK `java.nio` binary persistence layer
- JDK `Properties` application settings
- procedural runtime-generated prototype visuals
- `jpackage` Windows app-image

## Ownership

HORSEBOUND is an original project created by **Dimash Janibekov (DizZyZ7)**.
See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md). The repository is public for
visibility and development history; public access does not grant reuse rights.
