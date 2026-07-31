# HORSEBOUND — Player Support

Created by **Dimash Janibekov (DizZyZ7)**.

HORSEBOUND does not automatically transmit telemetry or crash reports. Diagnostic files remain on the player's device until the player chooses to share them.

## Crash reports

On Windows, local crash reports are stored under:

```text
%APPDATA%\HORSEBOUND\logs\
```

Files are named like:

```text
crash-20260801-001234-567.log
```

HORSEBOUND keeps the 10 most recent reports and removes older reports automatically.

A report contains:

- HORSEBOUND version and build commit;
- UTC timestamp;
- operating system and CPU architecture;
- Java runtime information;
- processor and memory summary;
- failing thread;
- exception stack trace.

Home-directory and AppData path prefixes are replaced with privacy placeholders before the report is written.

## How to report a problem

When asking for support, provide:

1. what you were doing before the problem;
2. whether keyboard/mouse or controller was active;
3. the affected save slot;
4. the most recent crash report, if one exists;
5. the build string printed by:

```text
HORSEBOUND.exe --version
```

Do not send account credentials, Steam Guard codes or unrelated private files.

Do not send a ranch save unless support specifically requests it. Make a copy before sharing any save file.

## Save recovery

Each ranch slot normally contains:

```text
save.hbs
save.bak
```

If the primary save is unreadable, HORSEBOUND attempts to load the backup automatically. Do not manually delete either file before making a copy for support.

## Steam notes

Crash logs and device settings are local support data. They must not be synchronized through Steam Cloud. Only persistent ranch save files are intended for cloud synchronization.
