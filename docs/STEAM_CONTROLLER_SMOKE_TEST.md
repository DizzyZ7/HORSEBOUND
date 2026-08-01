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

Unavailable physical devices must remain marked **not tested**, never assumed passing.

## Clean-start route

1. Remove or temporarily move `%APPDATA%\HORSEBOUND\settings.properties`.
2. Keep ranch saves backed up outside the test location.
3. Connect the controller before launch.
4. Start the packaged `HORSEBOUND.exe` directly.
5. Confirm the default window is readable at 1280×800.
6. Use only D-pad/Left Stick and controller buttons from this point.
7. Verify the prompt strip switches to the correct controller family after the first meaningful input.
8. Confirm no keyboard glyphs remain in the active bottom prompt strip.
9. On Nintendo devices, compare every A/B/X/Y prompt against the physical button that actually triggers the action; do not assume an Xbox-style positional swap.

## Main menu and settings

1. Navigate every main-menu item in both directions.
2. Open Settings.
3. Change Window Mode, Windowed Resolution, VSync, Graphics Preset, UI Scale, Performance Overlay, Sensitivity and Autosave.
4. Verify Left/Right changes values and Confirm activates the selected row.
5. Set UI scale to 150% and verify every Settings row remains visible at 1280×800.
6. Enable the performance overlay and confirm its text does not overlap the bottom prompt strip.
7. Return to the main menu using the controller Back action.
8. Close and reopen the game; verify device-local settings persist.

## Ranch slot flow

1. Open New Game.
2. Navigate all three ranch cards and Back.
3. Create a ranch in an empty slot.
4. Return to the menu and open New Game again.
5. Select the occupied slot once and verify overwrite warning text.
6. Select it a second time and verify overwrite confirmation works.
7. Open Load Game and load each valid slot.
8. Select an empty or damaged slot and verify the error message is readable without trapping focus.
9. Verify all slot metadata remains readable at 1280×800 and 150% UI scale.

## Gameplay route

1. Move and rotate the camera simultaneously.
2. Confirm small stick drift does not move the player or switch prompts.
3. Jump.
4. Sprint.
5. Approach and interact with Pushik.
6. Interact with a horse.
7. Feed and tame a horse when resources allow.
8. Mount and dismount.
9. Steer, gallop and jump while mounted.
10. Gather wood.
11. Build a fence.
12. Trigger manual save and verify the visible View / Share / Minus prompt matches the controller family.
13. Return to the menu using Pause/Back.
14. Verify the prompt family remains correct after every screen transition.

## Persistence and restart

1. Exit the game through the controller-accessible menu.
2. Relaunch from the packaged executable or Steam client.
3. Select Continue using only the controller.
4. Verify player position, inventory, fences, horse relationship data and Pushik state survived.
5. Verify controller prompts appear after the first controller input without requiring a Settings toggle.
6. Disconnect the controller during gameplay.
7. Reconnect it and verify input resumes without restarting the game.

## Display and performance checks

Test these configurations:

- 1280×800 windowed, Medium preset;
- 1280×800 fullscreen or borderless-equivalent platform mode;
- 1920×1080 windowed/fullscreen on desktop;
- 150% UI scale at 1280×800;
- Low preset with performance overlay enabled.

Record:

- average FPS;
- average frame time;
- worst frame time;
- whether the 800p/30 indicator passes;
- any prompt overlap, clipping or unreadable text.

## Pass criteria

A candidate passes controller-only QA only when:

- launch-to-exit is possible without keyboard or mouse;
- no screen traps focus;
- button labels match the active controller family;
- prompts change only after meaningful device input;
- save, restart and Continue work;
- 1280×800 UI remains readable;
- no essential text is hidden by the prompt or performance overlays;
- disconnect/reconnect does not crash or permanently disable input.

Steam Deck Verified status must not be claimed until this route passes on physical Deck hardware and Valve completes compatibility review.
