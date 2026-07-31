# HORSEBOUND — Steam Release Readiness

Created by **Dimash Janibekov (DizZyZ7)**.  
Copyright © 2026 Dimash Janibekov. All rights reserved.

This document is the release contract for HORSEBOUND. A feature is not considered shippable merely because it works from an IDE. It must survive installation, restart, Steam delivery, offline use and controller-oriented play.

## Current target

- Product: paid single-player cozy horse sandbox.
- Runtime: Java 21 game/application/domain code with libGDX + LWJGL3.
- Initial supported OS: Windows x64.
- Delivery: self-contained `jpackage` app image; players do not install Java separately.
- Steam launch target: `HORSEBOUND.exe` directly, without a required external launcher.
- Saves: `%APPDATA%\HORSEBOUND\saves\...`.
- Device-local settings: `%APPDATA%\HORSEBOUND\settings.properties`.
- Network: all single-player gameplay must work offline.

## Non-negotiable build rules

1. The default Steam launch option starts the game directly.
2. The game must not require administrator privileges.
3. The game must never write mutable saves/settings into the Steam install directory.
4. A clean Steam install must launch on a supported Windows x64 machine without a separately installed JRE.
5. The packaged build must start, create user data, save, exit cleanly and reopen the same ranch.
6. Store-page features must exist in the submitted build; planned features must be clearly marked as planned or omitted.
7. Steam credentials, SDK binaries, private VDF files and build-account details must never enter Git.
8. Every release candidate passes unit tests, save migration tests, Windows packaging and executable verification.

## Steam Cloud design

Use Steam Auto-Cloud first. It is simpler and does not couple the domain layer to Steamworks.

Cloud these files:

```text
%APPDATA%\HORSEBOUND\saves\**\save.hbs
%APPDATA%\HORSEBOUND\saves\**\save.bak
```

Do **not** cloud:

```text
%APPDATA%\HORSEBOUND\settings.properties
logs
crash dumps
temporary files
save.tmp
```

Reason: graphics, resolution, render distance and input tuning are device-specific. A desktop and Steam Deck must not overwrite each other's display configuration.

Recommended initial Auto-Cloud quota:

- 100 MB per user;
- 200 files per user;
- recursive sync under the HORSEBOUND `saves` directory;
- Windows root first, with platform overrides only if native Linux/macOS builds are added later.

## Steam Deck / controller target

HORSEBOUND should target **Deck Verified**, not merely “it launches through Proton”.

Required design direction:

- all gameplay, menus, save slots and settings reachable with a controller;
- controller enabled by default, without requiring an in-game toggle;
- mixed input: mouse and right stick may both rotate the camera cleanly;
- active-device prompts/glyphs instead of permanently showing keyboard text;
- no required launcher before gameplay;
- no manual on-screen-keyboard invocation for required text input;
- readable HUD and menus at 1280×800;
- playable default graphics configuration at 30 FPS on Deck-class hardware;
- 16:10 and non-16:9 aspect ratios supported;
- single-player gameplay available offline.

Controller abstraction planned for 0.4.2:

```text
Raw keyboard/mouse/gamepad input
        ↓
InputMapper
        ↓
PlayerCommand / MenuCommand
        ↓
Fixed-step GameSimulation
```

The gameplay domain must not query `Gdx.input` directly after this migration.

## SteamPipe / depot policy

Start with one Windows x64 content depot containing the complete self-contained app image:

```text
HORSEBOUND/
    HORSEBOUND.exe
    app/
    runtime/
```

Do not place saves, settings or generated user files in the depot.

Branch policy once an AppID exists:

- `default`: public approved release;
- `playtest`: external QA / Steam Playtest build;
- `internal`: private developer validation;
- `previous`: rollback candidate when needed.

A build is uploaded to a non-public branch first. The default branch is changed only after installation and smoke testing through the Steam client.

## Release-candidate smoke test

Test from the packaged/Steam-installed build, not from Gradle:

1. Install into a path containing spaces and non-ASCII characters.
2. Launch directly from Steam.
3. Create a new ranch in each save slot.
4. Gather wood and spend it.
5. Feed and tame a horse.
6. Change horse trust/bond/fear.
7. Pet Pushik and change his affection/state.
8. Build multiple persistent objects.
9. Trigger manual save and autosave.
10. Exit through menu and by closing the window.
11. Relaunch and verify all state.
12. Corrupt the primary save and verify backup recovery.
13. Run without network access.
14. Run with a controller from startup to exit.
15. Test 1920×1080, 1280×800 and at least one ultrawide aspect ratio.
16. Verify no user data is created inside the install depot.
17. Verify update/install validation does not erase saves.

## Store/build review discipline

Before submitting to Valve:

- upload a near-final build to the default branch candidate;
- ensure every feature selected on the store page is implemented;
- use only real gameplay screenshots;
- do not advertise unimplemented features as current content;
- make the product description coherent and specific;
- submit store presence early enough for review feedback;
- keep the Coming Soon page live for the required minimum period before release;
- allow review time for both the store page and the product build.

## Early Access decision gate

Do not enter Early Access merely to finance development. HORSEBOUND may enter Early Access only when the current build is already worth its current asking price and provides a stable repeatable loop:

```text
explore → meet horse → build trust → tame → ride → gather → build ranch → save → return
```

Required before Early Access:

- reliable saves and migrations;
- controller-complete menus/gameplay;
- meaningful horse personalities;
- stable ranch-building loop;
- sufficient content for honest repeat play;
- no roadmap promises phrased as guarantees;
- transparent Early Access questionnaire describing what exists now.

## Store asset production list

Current required Steam asset targets:

- Header Capsule: 920×430;
- Small Capsule: 462×174;
- Main Capsule: 1232×706;
- Vertical Capsule: 748×896;
- screenshots: 1920×1080 or larger, 16:9;
- shortcut icon: 256×256;
- app icon: 184×184;
- Library Capsule: 600×900;
- Library Hero: 3840×1240;
- Library Logo: up to 1280 px wide and/or 720 px tall, transparent PNG;
- Library Header Capsule: 920×430.

Base capsules contain only game artwork, the readable HORSEBOUND name/logo and an official subtitle if used. No review scores, awards, discount text or unrelated marketing copy.

## Official references

- Release process: https://partner.steamgames.com/doc/store/releasing
- Review process: https://partner.steamgames.com/doc/store/Review_Process
- Uploading through SteamPipe: https://partner.steamgames.com/doc/sdk/uploading
- Builds: https://partner.steamgames.com/doc/store/application/builds
- Depots: https://partner.steamgames.com/doc/store/application/depots
- Steam Cloud: https://partner.steamgames.com/doc/features/cloud
- Steam Deck / Steam Machine recommendations: https://partner.steamgames.com/doc/steamhardware/recommendations
- Compatibility review: https://partner.steamgames.com/doc/steamhardware/compat
- Early Access: https://partner.steamgames.com/doc/store/earlyaccess
- Graphical assets: https://partner.steamgames.com/doc/store/assets
- Graphical asset rules: https://partner.steamgames.com/doc/store/assets/rules
