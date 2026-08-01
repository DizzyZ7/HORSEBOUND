# HORSEBOUND — Homestead Release Contract

Created by **Dimash Janibekov (DizZyZ7)**.  
Copyright © 2026 Dimash Janibekov. All rights reserved.

This contract applies to every 0.5.x candidate. A Homestead feature is not complete until its domain state, packaged interaction path and save/restart behavior all pass.

## Persistence gate

Test with packaged builds, never only from the IDE:

1. Keep clean copies of v1, v2 and v3 ranch files.
2. Launch 0.5.x and load each file.
3. Verify inventory totals are unchanged, including totals larger than one visual stack.
4. Verify every legacy fence still appears exactly once.
5. Verify migrated fences have stable IDs across repeated loads.
6. Verify each old horse receives safe hunger/thirst/energy defaults.
7. Verify the default hotbar is created.
8. Save, exit and relaunch.
9. Verify the primary file is v4 and the previous valid file remains recoverable as `save.bak`.
10. Corrupt `save.hbs` and confirm backup recovery.

Migration must never silently delete excess inventory. When a legacy inventory exceeds the 24-slot player capacity, all items remain owned and the inventory is marked over capacity. New stacks remain blocked until enough space is freed.

## Inventory and hotbar gate

- 24 inventory slots;
- per-item stack limits remain stable;
- partial stacks fill before a new slot is consumed;
- adding to a full inventory returns the exact unaccepted amount through the existing accepted-count contract;
- build recipes never consume partial materials when the full recipe is unavailable;
- hotbar has eight slots and one selected index;
- empty and unknown hotbar IDs do not crash load;
- keyboard/controller selection must show the same selected slot;
- remapped input and controller prompts must remain correct.

## Structure gate

For Fence, Gate, Feeder, Water Trough, Hay Storage, Chest and Stable Stall:

1. Preview placement without spending materials.
2. Reject lake, out-of-world, collision and invalid-slope placement.
3. Confirm placement once.
4. Consume the complete recipe exactly once.
5. Save and restart.
6. Verify UUID, type, transform and stored units.
7. Remove/undo according to the current refund policy.
8. Confirm duplicate persistent UUIDs cannot create ambiguous objects.

## Resource service gate

- Feeder accepts Hay only.
- Water Trough accepts Water Bucket only.
- Storage never exceeds its declared capacity.
- A rejected deposit does not consume an inventory item.
- Horse service queries choose a valid nearby structure with available units.
- One care event consumes one stored unit.
- Distant or empty structures do not fake care.

## Horse-needs gate

Test idle, walking and galloping states:

- hunger, thirst and energy remain within 0–100;
- gallop drains faster than walking;
- idle recovery is gentle;
- nearby feed and water raise the correct need;
- resting near a Stable Stall accelerates energy recovery;
- infrastructure reduces repetitive chores rather than creating rapid failure timers;
- all need values survive save/restart exactly within float serialization tolerance.

## Controller and Deck path

The 0.5.1+ packaged build must support, without mouse/keyboard:

```text
select hotbar item
→ open build mode
→ rotate / snap preview
→ confirm or cancel
→ deposit feed/water
→ inspect horse needs
→ pause
→ save
→ restart
→ Continue
```

Run at 1280×800 and 150% UI scale. No essential need, cost, storage or selected-slot text may be hidden by the prompt/performance overlays.

## Store discipline

Do not select or advertise playable ranch construction, automatic horse care, feeders, troughs, stalls, inventory UI or crafting on the Steam store page until the corresponding packaged interaction route passes this contract. Domain-only foundations may be described in development notes, not as current player-facing content.
