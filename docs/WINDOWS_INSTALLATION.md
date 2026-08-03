# HORSEBOUND — Windows Installation

Created by **Dimash Janibekov (DizZyZ7)**.

## Recommended: installer

Run:

```text
HORSEBOUND-0.5.7.exe
```

The installer:

- installs only for the current Windows user;
- does not require administrator rights;
- lets the user choose the destination;
- creates a Start Menu entry;
- can create a desktop shortcut;
- bundles the required Java runtime;
- uses a stable upgrade identity for later HORSEBOUND versions.

Because the current development installer is not code-signed, Windows SmartScreen may display an unknown-publisher warning. A production release should be signed with an Authenticode certificate before public commercial distribution.

## Portable package

Extract the complete archive and keep its directory structure unchanged:

```text
HORSEBOUND.exe
app/
runtime/
licenses/
THIRD_PARTY_NOTICES.txt
```

Launch `HORSEBOUND.exe`. Do not move only the executable away from the `app` and `runtime` directories.

The portable package is intended for:

- Steam depot preparation;
- no-install testing;
- diagnostics;
- development snapshots.

## User data

Both delivery methods use the same device-local data directory:

```text
%APPDATA%\HORSEBOUND\
```

Installing, updating or deleting the application does not intentionally delete ranch saves. Save backups should still be kept before destructive testing.

## Build outputs

```text
build/jpackage/HORSEBOUND/
build/installer/HORSEBOUND-0.5.7.exe
```

Both outputs are generated and validated by the Windows CI pipeline.
