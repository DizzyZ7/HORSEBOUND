# HORSEBOUND

**HORSEBOUND is created by Dimash Dzhanibekov (DizZyZ7).**  
Copyright © 2026 Dimash Dzhanibekov (DizZyZ7). All rights reserved.

A cozy stylized 3D Java sandbox about horses, exploration, taming, riding, gathering,
and building a small ranch. The world is intentionally **not voxel/block based**:
terrain is a continuous procedural mesh and characters are simple low-poly 3D forms.

## Current playable vertical slice

- third-person 3D movement and mouse camera;
- smooth procedural terrain with hills, a lake, trees and resource gathering;
- several wild horses with wandering/fleeing behavior;
- trust-based taming using apples;
- mounting, riding, gallop stamina and jumping;
- simple fence building from gathered wood;
- time-of-day lighting;
- **Pushik / Пушик**, a completely black fluffy cat with fluffy paws who follows the player quietly;
- title screen, HUD and creator attribution;
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
| Esc | Return to menu |

## Run from source

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
```

The project uses libGDX 1.14.2.

## Windows build

A Windows build is produced by GitHub Actions on every push to `main`.
Download the `HORSEBOUND-Windows-x64` artifact and extract it. Start the game with:

```text
HORSEBOUND/HORSEBOUND.exe
```

No system Java installation is required for that packaged build because `jpackage`
includes a runtime image next to the executable.

To package locally on Windows with JDK 21:

```powershell
gradle clean windowsImage
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
- procedural runtime-generated prototype visuals
- `jpackage` Windows app-image

## Ownership

HORSEBOUND is an original project created by **Dimash Dzhanibekov (DizZyZ7)**.
See [LICENSE](LICENSE) and [NOTICE.md](NOTICE.md). The repository is public for
visibility and development history; public access does not grant reuse rights.
