# HORSEBOUND Localization

## Supported languages

- English (`Language.ENGLISH`)
- Russian (`Language.RUSSIAN`)

All player-facing text must be retrieved through `I18n`. Internal save IDs, enum names, diagnostics intended only for developers and serialized values remain stable and language-neutral.

## Runtime selection

The active language is stored in device-local `settings.properties` under `language`.

On a settings file without that property:

- a Russian operating-system locale selects Russian;
- every other locale selects English.

The setting is not part of ranch save v5 and is not intended for Steam Auto-Cloud.

## Catalogs

```text
src/main/resources/i18n/messages_en.properties
src/main/resources/i18n/messages_ru.properties
```

Rules:

1. Every key must exist in both catalogs.
2. Formatting placeholders must match by index and type.
3. English is the deterministic runtime fallback.
4. Keys and resource files use UTF-8.
5. Player-facing Java code should reference keys rather than embed translated sentences.
6. Saved domain values must never depend on translated labels.

## Font pipeline

```text
tools/generate_ui_font.py
src/main/resources/fonts/horsebound-ui.fnt
src/main/resources/fonts/horsebound-ui.png
```

The generator creates a reproducible BMFont atlas from DejaVu Sans and includes Latin, Cyrillic and the interface symbols used by both catalogs. The font license is stored at `licenses/BITSTREAM-VERA-FONT.txt`.

Player-facing screens obtain fonts through `GameFonts.create()` rather than constructing libGDX's legacy default `BitmapFont`.

## Layout expectations

Russian copy is often longer than English. New screens must:

- derive row and button positions from available resolution;
- avoid fixed widths sized only for English;
- use the existing UI scale setting;
- keep controller and keyboard prompts readable at 1280×800;
- prefer concise action-oriented labels in both languages.

## Domain-safe localization

Display adapters localize stable values:

- `ItemId.displayName()`;
- `HomesteadStructureType.displayName()`;
- `BindableAction.displayName()`;
- `HorsePersonality.displayName()`;
- `PushikState.displayName()`;
- default horse names through `HorseNames`.

The underlying enum constants, item IDs, UUIDs and serialized horse names remain unchanged.

## Guidance

`RanchGuidance` derives the next objective from current inventory, structures, storage and horse state. It does not persist quest flags. This prevents old saves and non-linear player actions from desynchronizing onboarding copy.

## Required tests

- catalog key parity;
- placeholder parity;
- fallback behavior;
- Unicode font coverage;
- selected-source raw-copy scan;
- language persistence and legacy settings loading;
- derived guidance progression;
- packaged localization resources inside `HORSEBOUND.jar`.
