# HORSEBOUND — Steam Release Readiness

Created by **Dimash Janibekov (DizZyZ7)**.  
Copyright © 2026 Dimash Janibekov. All rights reserved.

This document is the release contract for HORSEBOUND. A feature is not shippable merely because it works from an IDE. It must survive installation, restart, Steam delivery, offline use, controller-oriented play and device changes.

## Current target

- Product: paid single-player cozy horse sandbox.
- Runtime: Java 21 application/domain code with libGDX + LWJGL3.
- Initial supported OS: Windows x64.
- Delivery: self-contained `jpackage` app image; players do not install Java separately.
- Steam launch target: `HORSEBOUND.exe` directly, without a required external launcher.
- Ranch saves: `%APPDATA%\HORSEBOUND\saves\...`.
- Display settings: `%APPDATA%\HORSEBOUND\settings.properties`.
- Input/accessibility profile: `%APPDATA%\HORSEBOUND\input.properties`.
- Diagnostics: `%APPDATA%\HORSEBOUND\logs\...`.
- Network: all single-player gameplay must work offline.

## Non-negotiable build rules

1. The default Steam launch option starts the game directly.
2. The game must not require administrator privileges.
3. The game must never write mutable saves, settings or logs into the Steam install directory.
4. A clean Steam install must launch on Windows x64 without a separately installed JRE.
5. The packaged build must start, create user data, save, exit cleanly and reopen the same ranch.
6. Store-page features must exist in the submitted build; planned features are clearly marked or omitted.
7. Steam credentials, SDK binaries, private VDF files and build-account details never enter Git.
8. Every candidate passes unit tests, save migrations, Windows packaging, runtime-class assertions and executable verification.
9. A pause screen must stop simulation and resume the same in-memory ranch rather than silently creating a new session.
10. Required gameplay actions must remain reachable after rebinding and controller disconnect/reconnect.

## Steam Cloud design

Use Steam Auto-Cloud first so the gameplay/domain layer remains independent from Steamworks.

Cloud only ranch data:

```text
%APPDATA%\HORSEBOUND\saves\**\save.hbs
%APPDATA%\HORSEBOUND\saves\**\save.bak
```

Do **not** cloud:

```text
%APPDATA%\HORSEBOUND\settings.properties
%APPDATA%\HORSEBOUND\input.properties
%APPDATA%\HORSEBOUND\logs\**
crash dumps
save.tmp
*.tmp
```

Display and input tuning are device-specific. A desktop, laptop and Steam Deck must not overwrite one another's resolution, dead zones, rumble preference or local bindings.

Recommended initial Auto-Cloud quota:

- 100 MB per user;
- 200 files per user;
- recursive sync under the HORSEBOUND `saves` directory;
- Windows root first, with platform overrides only if native Linux/macOS builds are added later.

## Input architecture

Current implemented direction:

```text
Raw keyboard / mouse / standardized gamepad input
                ↓
InputProfile + device adapters
                ↓
InputMapper
                ↓
PlayerCommand / MenuCommand
                ↓
fixed-step GameSimulation
```

- gameplay receives semantic commands rather than physical keys;
- keyboard bindings are device-local and conflict-safe;
- movement/camera dead zones are configurable;
- camera inversion and Hold/Toggle sprint work in the live adapters;
- pause is intercepted at the application layer before world simulation sees it;
- unsupported rumble devices fail safely;
- dynamic prompts use the active input profile and controller family.

The gameplay domain must not query physical input directly.

## Steam Deck / controller target

HORSEBOUND should target **Deck Verified**, not merely “it launches through Proton”.

Required design direction:

- all gameplay, menus, save slots, pause and essential settings reachable with a controller;
- controller enabled by default without an in-game enable toggle;
- mixed input for mouse/right-stick camera use;
- active-device prompts instead of permanent keyboard text;
- no required launcher before gameplay;
- no required manual on-screen keyboard invocation;
- readable HUD and menus at 1280×800;
- playable default configuration at 30 FPS on Deck-class hardware;
- 16:10 and non-16:9 aspect ratios supported;
- single-player gameplay available offline;
- controller disconnect/reconnect does not crash or trap focus;
- accessibility settings persist locally and do not corrupt ranch saves.

Passing automated tests is not a Verified claim. Physical Deck profiling, Proton validation and Valve compatibility review remain mandatory.

## SteamPipe / depot policy

Start with one Windows x64 content depot containing the complete self-contained app image:

```text
HORSEBOUND/
    HORSEBOUND.exe
    app/
    runtime/
    licenses/
    THIRD_PARTY_NOTICES.txt
    SHA256SUMS.txt
```

Never place saves, settings, input profiles, logs or generated user files in the depot. CI explicitly rejects them.

Branch policy once an AppID exists:

- `default`: public approved release;
- `playtest`: external QA / Steam Playtest build;
- `internal`: private developer validation;
- `previous`: rollback candidate.

Upload to a non-public branch first. Promote only after installation and smoke testing through the Steam client.

## Release-candidate smoke test

Test the packaged/Steam-installed build, not Gradle:

1. Install into a path containing spaces and non-ASCII characters.
2. Launch directly from Steam.
3. Create a ranch in each save slot.
4. Gather/spend resources, feed/tame a horse and change relationship data.
5. Pet Pushik and change affection/state.
6. Build persistent objects.
7. Trigger manual save and autosave.
8. Pause, change accessibility settings and resume the same in-memory ranch.
9. Rebind multiple keys, including a conflict swap, and verify HUD prompts update.
10. Test Hold and Toggle sprint/gallop.
11. Test camera inversion and several dead-zone values.
12. Test rumble enabled/disabled on supported hardware.
13. Exit through menu and close-window paths.
14. Relaunch and verify all ranch state.
15. Verify display/input settings persist locally but do not alter ranch data.
16. Corrupt the primary save and verify backup recovery.
17. Run without network access.
18. Run controller-only from startup to exit.
19. Disconnect and reconnect the controller in gameplay and menus.
20. Test 1920×1080, 1280×800 and an ultrawide aspect ratio.
21. Verify no user data appears inside the install depot.
22. Verify update/install validation does not erase saves.

The detailed controller route is in [`STEAM_CONTROLLER_SMOKE_TEST.md`](STEAM_CONTROLLER_SMOKE_TEST.md).

## Store/build review discipline

Before submitting to Valve:

- upload a near-final build to a candidate branch;
- ensure every selected store feature is implemented;
- use only real gameplay screenshots;
- do not advertise unimplemented features as current content;
- submit store presence early enough for review feedback;
- keep the Coming Soon page live for the required period;
- allow review time for both store presence and product build.

## Early Access decision gate

HORSEBOUND may enter Early Access only when the current build is already worth its current asking price and provides a stable repeatable loop:

```text
explore → meet horse → build trust → tame → ride → gather → build ranch → save → return
```

Required before Early Access:

- reliable saves and migrations;
- controller-complete menus/gameplay;
- true pause and accessible input settings;
- meaningful horse personalities;
- stable ranch-building loop;
- sufficient content for honest repeat play;
- no roadmap promises phrased as guarantees;
- a transparent Early Access questionnaire describing what exists now.

## Store asset production list

Current project asset targets:

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
- SteamPipe: https://partner.steamgames.com/doc/sdk/uploading
- Builds: https://partner.steamgames.com/doc/store/application/builds
- Depots: https://partner.steamgames.com/doc/store/application/depots
- Steam Cloud: https://partner.steamgames.com/doc/features/cloud
- Steam hardware recommendations: https://partner.steamgames.com/doc/steamhardware/recommendations
- Compatibility review: https://partner.steamgames.com/doc/steamhardware/compat
- Early Access: https://partner.steamgames.com/doc/store/earlyaccess
- Graphical assets: https://partner.steamgames.com/doc/store/assets
- Graphical asset rules: https://partner.steamgames.com/doc/store/assets/rules
