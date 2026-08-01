# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch. The world uses continuous terrain and is intentionally not voxel/block based.

## 0.4.8 — Input & Accessibility

HORSEBOUND now has a true in-memory pause lifecycle and a device-local input profile independent of ranch progress.

### True pause

- Pause opens a dedicated screen instead of immediately returning to the main menu;
- ranch simulation stops while the current world remains alive in memory;
- Resume returns to the same session without regeneration or reload;
- Save Game persists without leaving the session;
- Save & Main Menu persists and disposes the world cleanly;
- backgrounding from pause or input screens saves the suspended ranch.

### Input and accessibility settings

- invert vertical camera;
- configurable movement and camera stick dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible Settings Hub, pause and input screens;
- changes apply to live input adapters without recreating the ranch.

### Keyboard rebinding

The player can rebind movement, jump, interact, mount, build, sprint, manual save and pause. Assigning an already-used key swaps the two actions instead of creating an invisible conflict. Dynamic HUD prompts immediately show the active bindings.

Input settings are stored separately:

```text
%APPDATA%\HORSEBOUND\input.properties
```

The file uses temporary writes and atomic replacement where supported. Invalid fields fall back safely without resetting valid fields.

## HUD & controller glyphs

HORSEBOUND has a centralized scalable action-prompt layer across gameplay and the core menu flow.

- prompts follow the last meaningfully used device;
- small stick drift does not switch the prompt family;
- keyboard prompts use active rebound keys;
- stable labels such as `SHIFT`, `CTRL`, `ESC` and `SPACE` are independent of backend naming;
- Xbox, PlayStation, Steam Deck, Nintendo and generic controller families;
- vector-rendered prompt chips scale with the 1280×800 UI system;
- save-slot cards remain readable at 150% UI scale.

Every future Steam candidate must pass the packaged controller-only route in [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md).

This is not a Steam Deck Verified claim. Physical Deck testing, Proton validation and Valve compatibility review remain required.

## Display & Deck UX

HORSEBOUND has device-local display configuration, scalable 1280×800-oriented UI and measurable performance diagnostics.

- Deck-safe 1280×800 default window;
- windowed and fullscreen modes;
- 1280×720, 1280×800, 1600×900 and 1920×1080 windowed profiles;
- runtime resolution changes with safe fallback;
- VSync;
- UI/text scale from 100% to 150%;
- optional global performance overlay.

| Preset | MSAA | Foreground cap |
|---|---:|---:|
| Low | Off | 60 FPS |
| Medium | 2× | 90 FPS |
| High | 4× | 144 FPS |

The FPS cap changes immediately. MSAA changes on the next launch because the OpenGL backbuffer must be recreated.

The performance overlay reports rolling FPS, average and worst frame time, current resolution/preset and an explicit 800p/30 target status.

## Support diagnostics

Privacy-conscious crash reports are stored only on the player's device:

```text
%APPDATA%\HORSEBOUND\logs\
```

- no automatic telemetry or uploads;
- build, OS, architecture, Java runtime, memory, thread and stack trace;
- home and AppData path prefixes are redacted;
- only the 10 newest reports remain;
- logs are excluded from the Steam install image and Steam Cloud policy.

See [`docs/SUPPORT.md`](docs/SUPPORT.md).

## Controller foundation

HORSEBOUND uses the official `gdx-controllers` desktop backend and standardized mappings.

| Controller input | Action |
|---|---|
| Left Stick | Move / steer |
| Right Stick | Camera |
| A / Cross | Jump |
| X / Square | Interact |
| Y / Triangle | Mount / dismount |
| L1 / LB | Build |
| R1 / RB | Sprint / gallop |
| Back / View / Share | Manual save |
| Start / Menu or B / Circle | Pause / back |

D-pad or Left Stick navigates menus. Confirm and Back labels follow the active controller family. Unsupported rumble devices fail safely without affecting gameplay.

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
- true pause and resume;
- three ranch save slots;
- manual save, autosave and backup recovery.

## Default keyboard controls

All gameplay keys may be rebound.

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
| Esc | Pause |

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

Ranch saves are prepared for Steam Auto-Cloud. Display settings, input profiles and crash logs remain device-local.

## Steam readiness

HORSEBOUND is developed against SteamPipe, Steam Cloud, offline single-player and Steam Deck requirements from the architecture stage.

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- [`docs/STEAM_RELEASE_READINESS.md`](docs/STEAM_RELEASE_READINESS.md)
- [`docs/STEAM_CONTROLLER_SMOKE_TEST.md`](docs/STEAM_CONTROLLER_SMOKE_TEST.md)
- [`docs/SUPPORT.md`](docs/SUPPORT.md)
- [`docs/releases/0.4.8.md`](docs/releases/0.4.8.md)
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
- prompt, pause, input-profile and rebinding runtime classes;
- third-party notices and exact license texts;
- absence of saves, display/input settings, crash logs and local Steam data in the install image;
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
