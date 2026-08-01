# HORSEBOUND 0.5.6 — Evening Playtest Route

Use a newly extracted build. Keep the entire `HORSEBOUND` directory together and launch `HORSEBOUND.exe`.

## Before starting

Record:

- Windows version and GPU;
- keyboard/mouse or controller model;
- display mode, resolution, UI scale and graphics preset;
- whether an older `%APPDATA%\HORSEBOUND` folder already existed.

Do not delete existing saves before first launch. Compatibility is part of the test.

## 1. Boot and menu

Expected:

- application opens without a terminal or external Java installation;
- version reports 0.5.6;
- menus respond once per press;
- holding Confirm while a new screen opens does not auto-activate its first item;
- Back does not immediately close a screen entered while the button was held.

## 2. Settings and input

- change UI scale and both audio buses;
- change one keyboard binding;
- leave settings, reopen them and confirm persistence;
- test Hold and Toggle Sprint;
- with Toggle Sprint, enter gameplay while R1 is held: sprint must stay off until release and a new press.

## 3. Existing ranch compatibility

- load an existing 0.5.5 or older ranch;
- verify player, horses, Pushik, structures, Chest contents, Gate state and inventory;
- save, return to menu, reload and compare again.

## 4. New ranch and overwrite safety

- choose an empty slot and create a ranch;
- later select an occupied slot, arm overwrite, move to another slot, return and press Confirm once;
- expected: the occupied ranch is not overwritten until confirmation is performed again without leaving the slot.

## 5. Basic movement and camera

- walk, sprint, jump and rotate the camera;
- move behind trees, rocks, fences, Gates and Stalls;
- confirm camera pull-in and nature fading are readable rather than distracting;
- open a Gate and confirm it no longer blocks movement or camera.

## 6. Horse and lake safety

- mount a horse;
- approach the lake from several angles;
- dismount near the shoreline;
- expected: the player appears on dry bounded ground and remains controllable.

## 7. Pushik safety

- move far enough for Pushik to catch up;
- repeat near the lake and near the world boundary;
- expected: Pushik returns to valid dry ground and continues normal behavior.

## 8. Gathering and construction

- gather wood and other available resources;
- attempt to place a structure inside a tree, rock, horse and existing structure;
- expected: each invalid placement is rejected with an understandable reason;
- place a valid structure, rotate it, move it, undo the move and test two-step dismantling.

## 9. Inventory and ranch care

- open inventory and a Chest;
- transfer one item, one stack and all units of a type;
- fill or partially fill a feeder/trough;
- observe nearby horse hunger, thirst and energy;
- verify that the HUD does not describe a horse far across the world.

## 10. Pause, save and restart

- pause and resume several times with both tap and held buttons;
- save without leaving;
- use Save & Main Menu;
- close the application normally;
- relaunch and load the same slot;
- compare position, inventory, hotbar, structures, storage, horses and Pushik.

## Report format

For every problem, record:

```text
Severity: blocker / major / minor / polish
Build line:
Input device:
Starting state:
Exact steps:
Expected:
Actual:
Repeatable: always / sometimes / once
Screenshot or video:
Crash log path, when present: %APPDATA%\HORSEBOUND\logs\
Affected save slot:
```

A useful playtest report describes the first moment the result diverged from expectation. Avoid restarting immediately after a problem unless the game is blocked; first capture the visible state and exact input sequence.
