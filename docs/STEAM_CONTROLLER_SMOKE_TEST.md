# HORSEBOUND — Controller-Only Steam Smoke Test

Created by **Dimash Janibekov (DizZyZ7)**.  
Copyright © 2026 Dimash Janibekov. All rights reserved.

This checklist is the minimum controller-only acceptance path for every Steam candidate. Passing unit tests is not enough: the packaged build must be operated from launch to exit without touching a keyboard or mouse.

## Required devices

Run the complete route with each available family:

- Xbox / XInput controller;
- PlayStation DualShock or DualSense controller;
- Steam Deck built-in controls or Steam Virtual Gamepad;
- Nintendo Switch Pro / Joy-Con controller;
- one generic SDL-compatible controller when available.

Unavailable physical devices remain marked **not tested**, never assumed passing.

## Clean-start route

1. Temporarily move `%APPDATA%\HORSEBOUND\settings.properties` and `input.properties`.
2. Keep ranch saves backed up outside the test location.
3. Connect the controller before launch.
4. Start packaged `HORSEBOUND.exe` directly.
5. Confirm the default window is readable at 1280×800.
6. Use only D-pad/Left Stick and controller buttons from this point.
7. Verify prompts switch to the correct controller family after meaningful input.
8. Confirm keyboard glyphs do not remain in the active bottom strip.
9. On Nintendo devices, compare A/B/X/Y prompts with the physical button that triggers each action; never assume an Xbox positional swap.

## Settings hub

1. Navigate every main-menu item in both directions.
2. Open Settings and verify the hub exposes Display & Graphics and Input & Accessibility.
3. Open Display & Graphics.
4. Change Window Mode, Resolution, VSync, Graphics Preset, UI Scale, Performance Overlay, Sensitivity and Autosave.
5. Set UI scale to 150% and confirm every row remains visible at 1280×800.
6. Enable the performance overlay and verify it does not overlap the prompt strip.
7. Return to the Settings Hub using Back.

## Input and accessibility

1. Open Input & Accessibility using only the controller.
2. Toggle vertical camera inversion.
3. Change movement and camera dead zones through their full allowed range.
4. Switch sprint/gallop between Hold and Toggle.
5. Toggle rumble and change its strength.
6. On supported hardware, confirm a short feedback pulse occurs without repeating continuously.
7. On unsupported hardware, verify no error, freeze or input loss occurs.
8. Open Keyboard Bindings using the controller.
9. Navigate every binding and the defaults/back rows.
10. Enter keyboard-capture mode, then cancel with controller Back; verify focus is not trapped.
11. Return to Input & Accessibility and then the Settings Hub.
12. Close and reopen the game; verify display and input profiles persist independently.

## Ranch slot flow

1. Open New Game.
2. Navigate all three ranch cards and Back.
3. Create a ranch in an empty slot.
4. Return to menu and open New Game again.
5. Select the occupied slot once and verify overwrite warning text.
6. Select it a second time and verify overwrite confirmation.
7. Open Load Game and load each valid slot.
8. Select an empty/damaged slot and verify the message is readable without trapping focus.
9. Verify metadata remains readable at 1280×800 and 150% UI scale.

## Gameplay route

1. Move and rotate the camera simultaneously.
2. Confirm small stick drift does not move the player or switch prompts.
3. Test normal and inverted vertical camera movement.
4. Test Hold sprint while walking and galloping.
5. Test Toggle sprint: press once, release, remain sprinting, press again to stop.
6. Jump.
7. Approach and interact with Pushik.
8. Interact with a horse.
9. Feed and tame a horse when resources allow.
10. Mount and dismount.
11. Steer, gallop and jump while mounted.
12. Gather wood and build a fence.
13. Trigger manual save and verify View / Share / Minus matches the family.
14. Change dead zones while the ranch is suspended, resume and verify changes apply without reloading the world.
15. Verify dynamic prompts remain correct through every transition.

## True pause route

1. Pause during active gameplay.
2. Confirm the dedicated Pause screen appears instead of the main menu.
3. Wait and verify world time, horse AI and Pushik do not advance.
4. Select Resume and verify the same ranch state returns without reload/regeneration.
5. Pause again and choose Save Game; verify the session remains paused and a save is written.
6. Open Input & Accessibility from Pause, change an option and return to Pause.
7. Resume and verify the live session uses the new option.
8. Pause and choose Save & Main Menu; verify clean return to the main menu.
9. Relaunch/Continue and verify the saved state.

## Persistence and restart

1. Exit through controller-accessible menus.
2. Relaunch from packaged executable or Steam client.
3. Select Continue using only the controller.
4. Verify player position, inventory, fences, horse relationships and Pushik state.
5. Verify input/display profiles persisted without changing ranch content.
6. Verify controller prompts appear after first input without a Settings toggle.
7. Disconnect controller during gameplay, pause and menus.
8. Reconnect and verify input resumes without restart.

## Display and performance checks

Test:

- 1280×800 windowed, Medium preset;
- 1280×800 fullscreen or platform equivalent;
- 1920×1080 desktop;
- 150% UI scale at 1280×800;
- Low preset with performance overlay;
- performance overlay together with pause and prompt strips.

Record:

- average FPS;
- average frame time;
- worst frame time;
- 800p/30 target status;
- prompt/pause clipping or unreadable text;
- dead-zone and rumble device/model observations.

## Pass criteria

A candidate passes controller-only QA only when:

- launch-to-exit is possible without keyboard/mouse;
- no screen traps focus;
- pause resumes the same in-memory ranch;
- button labels match the active family;
- prompts change only after meaningful device input;
- Hold/Toggle, inversion and dead zones behave predictably;
- unsupported rumble fails safely;
- save, restart and Continue work;
- 1280×800 UI remains readable;
- essential text is not hidden by prompt/performance overlays;
- disconnect/reconnect does not crash or permanently disable input.

Steam Deck Verified status must not be claimed until this route passes on physical Deck hardware and Valve completes compatibility review.
