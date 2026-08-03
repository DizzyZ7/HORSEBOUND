# HORSEBOUND

**Created and owned by Dimash Janibekov (DizZyZ7).**  
Copyright © 2026 Dimash Janibekov. All rights reserved.

HORSEBOUND is a cozy stylized 3D Java sandbox about horses, exploration, taming, riding and building a ranch on continuous non-voxel terrain.

## 0.5.8 — Bilingual UX & Mechanics Clarity

The complete player-facing ranch experience is now available in **English and Russian** without changing save format v5.

### Bilingual player experience

- main menu, pause, settings, save slots, HUD, prompts, inventory and building feedback are localized;
- item, structure, action, personality, horse-name and Pushik-state labels are localized at display time;
- first launch follows a Russian operating-system locale and otherwise defaults to English;
- language can be changed in settings without reinstalling the game;
- a paused ranch refreshes its HUD and status copy after returning from language settings;
- English is the deterministic fallback for a missing Russian entry;
- serialized IDs, enum constants, UUIDs and save DTOs remain language-neutral.

### Unicode interface font

Player-facing screens use a reproducibly generated DejaVu Sans-derived BMFont atlas with Latin, Cyrillic, punctuation, arrows and required interface symbols. CI verifies the resources and corresponding font license inside the portable build and installer.

### Clear first-ranch guidance

Outside build or edit mode the HUD shows **Next objective / Следующая цель**, derived from actual ranch state:

1. gather wood;
2. build and stock a feeder;
3. build and fill a water trough;
4. earn a horse's trust;
5. mount a horse;
6. continue ranch development.

No separate quest flag is persisted, so old saves and non-linear actions cannot permanently desynchronize the objective.

See [`docs/releases/0.5.8.md`](docs/releases/0.5.8.md), [`docs/LOCALIZATION.md`](docs/LOCALIZATION.md), [`docs/VISUAL_ASSET_ROADMAP.md`](docs/VISUAL_ASSET_ROADMAP.md) and [`docs/WINDOWS_INSTALLATION.md`](docs/WINDOWS_INSTALLATION.md).

## Current gameplay

- English and Russian player-facing UX;
- state-derived onboarding objectives;
- corrected camera-relative keyboard/controller movement;
- resource gathering and persistent construction;
- feeders, troughs, hay storage, Chests, Gates and Stalls;
- structure placement, relocation, undo and confirmed dismantling;
- collision with terrain, structures, trees and rocks;
- horse personalities, trust, bond, fear, hunger, thirst and energy;
- relationship-based taming, riding, gallop stamina and jumping;
- safe dismounting near water and automatic ranch care;
- Pushik companion AI with persistent affection and safe catch-up placement;
- inventory, hotbar and one/stack/all Chest transfers;
- day/night lighting, ranch SFX and meadow ambience;
- three save slots, manual save, autosave and backup recovery.

## Localization architecture

```text
src/main/resources/i18n/messages_en.properties
src/main/resources/i18n/messages_ru.properties
src/main/resources/fonts/horsebound-ui.fnt
src/main/resources/fonts/horsebound-ui.png
```

`I18n`, `Language` and `GameFonts` own presentation copy. `RanchGuidance` derives onboarding from inventory, structures, storage and horse state. Domain and persistence code do not serialize translated values.

Tests enforce catalog key parity, formatting-placeholder parity, English fallback, Unicode glyph coverage and selected-source raw-copy rules.

## Input and accessibility

- configurable keyboard bindings with conflict-safe swapping;
- invert vertical camera;
- movement and camera dead zones;
- Hold or Toggle sprint/gallop;
- optional controller rumble and strength;
- controller-accessible menus, inventory, build/edit and undo;
- Xbox, PlayStation, Steam Deck, Nintendo and generic controller prompts;
- safe controller action priming across screen transitions and reconnects.

Input settings are stored in `%APPDATA%\HORSEBOUND\input.properties`.

## Display, audio and language

- 1280×800 default window;
- windowed/fullscreen modes and common resolution profiles;
- UI scale from 100% to 150%;
- Low / Medium / High graphics presets;
- independent effects and ambience volumes;
- English/Russian language selection with system-locale default;
- optional FPS/frame-time overlay.

Display, audio and language settings are stored in `%APPDATA%\HORSEBOUND\settings.properties`. They remain outside ranch saves and Steam Cloud planning.

## Pushik / Пушик

Pushik is the completely black fluffy cat with fluffy black paws and almost silent footsteps. He can follow, sit, explore, sleep and greet the player.

## Windows distribution

```text
HORSEBOUND-Installer-Windows-x64/HORSEBOUND-0.5.8.exe
HORSEBOUND-Portable-Windows-x64/HORSEBOUND.exe
```

The installer provides a normal per-user desktop installation. The portable package is intended for Steam depot preparation, diagnostics and no-install use. The development installer is not code-signed yet, so Windows SmartScreen may show an unknown-publisher warning.

## User data

```text
%APPDATA%\HORSEBOUND\
    settings.properties
    input.properties
    saves\slot-1..3\save.hbs + save.bak
    logs\crash-*.log
```

Steam Auto-Cloud is planned for ranch `save.hbs` and `save.bak` only.

## Build

Requirements: JDK 21 and Gradle 8.x+.

```bash
gradle run
gradle test
gradle clean test windowsImage windowsInstaller
```

CI verifies localization, Unicode resources, guidance progression, controller/accessibility, save migrations, inventory/storage, construction, camera behavior, portable packaging, installer generation, exact build identity and depot hygiene.

## Tech

- Java 21
- libGDX 1.14.2 / LWJGL3
- gdx-controllers / Jamepad
- Gradle
- JDK binary persistence
- UTF-8 English/Russian catalogs
- reproducible Unicode BMFont atlas
- Windows `jpackage` app-image and EXE installer

This is **not** a Steam Deck Verified claim. Physical Deck, Proton, controller-family, font/readability and GPU testing remain required.

## Licensing and ownership

HORSEBOUND is an original project by **Dimash Janibekov (DizZyZ7)**. Public repository access does not grant reuse rights. See [LICENSE](LICENSE), [NOTICE.md](NOTICE.md), [THIRD_PARTY_NOTICES.txt](THIRD_PARTY_NOTICES.txt) and [`licenses/`](licenses/).
