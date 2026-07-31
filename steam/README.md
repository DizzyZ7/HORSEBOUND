# HORSEBOUND SteamPipe Templates

Created by **Dimash Janibekov (DizZyZ7)**.

These files prepare HORSEBOUND for Steam delivery without committing Steam credentials or SDK binaries.

## Before use

1. Obtain the HORSEBOUND AppID and Windows DepotID in Steamworks.
2. Copy the template VDF files outside the repository or rename them to private local files.
3. Replace `APP_ID_HERE`, `DEPOT_ID_HERE` and the content/build-output paths.
4. Build the self-contained Windows app image:

```powershell
gradle clean test windowsImage
```

5. Copy the complete contents of:

```text
build/jpackage/HORSEBOUND/
```

into the SteamPipe content root. `HORSEBOUND.exe`, `app/` and `runtime/` must remain together.
6. Upload first as a preview or to a private Steam branch.
7. Install through the Steam client and run the full checklist in `docs/STEAM_RELEASE_READINESS.md`.

## Security

Never commit:

- Steam account names or passwords;
- Steam Guard codes;
- Steamworks SDK binaries;
- generated SteamPipe output/cache;
- private AppID/DepotID scripts if the repository should remain generic;
- local `steam_dev.cfg` files.

The game must launch directly through `HORSEBOUND.exe`. No second mandatory launcher is planned.
