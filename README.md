# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. The world uses continuous terrain and is intentionally not voxel/block based.

## 0.4.6 — Display & Deck UX

HORSEBOUND now has device-local display configuration, scalable 1280×800-oriented UI and measurable performance diagnostics.

### Display and graphics settings

- Deck-safe 1280×800 default window;
- windowed and fullscreen modes;
- runtime resolution changes with safe fallback;
- 1280×720, 1280×800, 1600×900 and 1920×1080 windowed profiles;
- VSync;
- UI/text scale from 100% to 150%;
- optional performance overlay;
- display settings remain separate from ranch saves and Steam Cloud.

Graphics presets are meaningful desktop startup profiles:

| Preset | MSAA | Foreground cap |
|---|---:|---:|
| Low | Off | 60 FPS |
| Medium | 2× | 90 FPS |
| High | 4× | 144 FPS |

The FPS cap changes immediately. MSAA changes on the next launch because the OpenGL backbuffer must be recreated.

### Performance diagnostics

The optional global overlay is available in menus and gameplay and reports:

- rolling average FPS;
- average frame time;
- worst frame time in the latest 120 samples;
- current resolution and graphics preset;
- an explicit 800p/30 target status.

The overlay is a development and player-support aid. It is not a substitute for physical Steam Deck profiling.

### Deck-safe UI direction

- centralized scaling from a 1280×800 design surface;
- controller-accessible display settings;
- scaled main menu with centered labels;
- keyboard/controller prompt catalog that never mixes both device families in one hint;
- tests guarantee a readable minimum effective scale on smaller windows.

This is not a Steam Deck Verified claim. Final graphical glyph assets, physical-device testing, Proton validation and Valve compatibility review remain required.

## Support diagnostics

HORSEBOUND carries an exact build identity and can produce privacy-conscious local crash reports.

Crash reports are stored only on the player's device:

```text
%APPDATA%\HORSEBOUND\logs\
```

- no automatic telemetry or uploads;
- build, OS, architecture, Java runtime, memory summary, thread and stack trace;
- home and AppData path prefixes are redacted;
- only the 10 newest reports are retained;
- logs are excluded from the Steam install image and Steam Cloud policy.

See [`docs/SUPPORT.md`](docs/SUPPORT.md).

## Controller foundation

HORSEBOUND has a standardized controller path across live gameplay and the core menu flow using the official `gdx-controllers` desktop backend.

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

D-pad or Left Stick navigates the main menu, ranch slots and Settings. A confirms and B returns. Keyboard and mouse remain supported.

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

Pushik is the completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player. His affection and state survive restart.

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
    logs\
        crash-*.log
```

Ranch saves are prepared for Steam Auto-Cloud. Display/input settings and crash logs remain local.

## Steam readiness

HORSEBOUND is developed against SteamPipe, Steam Cloud, offline single-player and Steam Deck requirements from the architecture stage.

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/SUPPORT.md`](docs/SUPPORT.md)
- [`docs/releases/0.4.6.md`](docs/releases/0.4.6.md)
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

- executable, application JAR and bundled runtime;
- exact packaged version and source commit;
- standardized controller classes and `jamepad64.dll`;
- third-party notices and exact license texts;
- absence of saves, settings, crash logs and local Steam data in the install image;
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
