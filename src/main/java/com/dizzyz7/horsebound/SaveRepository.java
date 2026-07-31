// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class SaveRepository {
    private static final int MAGIC = 0x48425356; // HBSV
    private static final int MAX_HORSES = 10_000;
    private static final int MAX_FENCES = 100_000;
    private static final int MAX_HARVESTED_TREES = 1_000_000;

    private final Path root;

    SaveRepository() {
        this(defaultRoot());
    }

    SaveRepository(Path root) {
        this.root = root;
    }

    boolean exists(String slot) {
        return Files.isRegularFile(savePath(slot)) || Files.isRegularFile(backupPath(slot));
    }

    Optional<SaveGame> load(String slot) {
        Path primary = savePath(slot);
        Path backup = backupPath(slot);
        if (!Files.isRegularFile(primary) && !Files.isRegularFile(backup)) {
            return Optional.empty();
        }

        IOException primaryFailure = null;
        if (Files.isRegularFile(primary)) {
            try {
                return Optional.of(read(primary));
            } catch (IOException ex) {
                primaryFailure = ex;
            }
        }

        if (Files.isRegularFile(backup)) {
            try {
                return Optional.of(read(backup));
            } catch (IOException backupFailure) {
                if (primaryFailure != null) {
                    backupFailure.addSuppressed(primaryFailure);
                }
                throw new SaveException("Both primary and backup HORSEBOUND saves are unreadable.", backupFailure);
            }
        }

        throw new SaveException("HORSEBOUND save is unreadable.", primaryFailure);
    }

    void save(String slot, SaveGame saveGame) {
        Path directory = slotDirectory(slot);
        Path primary = savePath(slot);
        Path backup = backupPath(slot);
        Path temporary = directory.resolve("save.tmp");

        try {
            Files.createDirectories(directory);
            writeAndSync(temporary, saveGame);

            if (Files.isRegularFile(primary)) {
                Files.copy(primary, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }

            moveAtomically(temporary, primary);
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                // Keep the original exception as the meaningful failure.
            }
            throw new SaveException("Could not save HORSEBOUND world to " + primary, ex);
        }
    }

    Path root() {
        return root;
    }

    private SaveGame read(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            int magic = in.readInt();
            if (magic != MAGIC) {
                throw new IOException("Not a HORSEBOUND save file: " + path);
            }

            int version = in.readInt();
            if (version < 1 || version > SaveGame.CURRENT_VERSION) {
                throw new IOException("Unsupported HORSEBOUND save version: " + version);
            }

            long worldSeed = in.readLong();
            long savedAt = in.readLong();
            float worldTime = in.readFloat();

            SaveGame.PlayerData player = new SaveGame.PlayerData(
                in.readFloat(),
                in.readFloat(),
                in.readFloat(),
                in.readInt(),
                in.readInt()
            );

            SaveGame.PushikData pushik = new SaveGame.PushikData(
                in.readFloat(),
                in.readFloat(),
                in.readFloat()
            );

            int horseCount = checkedCount(in.readInt(), MAX_HORSES, "horse");
            List<SaveGame.HorseData> horses = new ArrayList<>(horseCount);
            for (int i = 0; i < horseCount; i++) {
                UUID id = new UUID(in.readLong(), in.readLong());
                String name = in.readUTF();
                horses.add(new SaveGame.HorseData(
                    id,
                    name,
                    in.readFloat(),
                    in.readFloat(),
                    in.readFloat(),
                    in.readFloat(),
                    in.readFloat(),
                    in.readBoolean()
                ));
            }

            int fenceCount = checkedCount(in.readInt(), MAX_FENCES, "fence");
            List<SaveGame.FenceData> fences = new ArrayList<>(fenceCount);
            for (int i = 0; i < fenceCount; i++) {
                fences.add(new SaveGame.FenceData(in.readFloat(), in.readFloat(), in.readFloat()));
            }

            int treeCount = checkedCount(in.readInt(), MAX_HARVESTED_TREES, "harvested tree");
            List<Integer> harvestedTrees = new ArrayList<>(treeCount);
            for (int i = 0; i < treeCount; i++) {
                harvestedTrees.add(in.readInt());
            }

            return new SaveGame(
                version,
                worldSeed,
                savedAt,
                worldTime,
                player,
                pushik,
                horses,
                fences,
                harvestedTrees
            );
        } catch (EOFException ex) {
            throw new IOException("HORSEBOUND save ended unexpectedly: " + path, ex);
        }
    }

    private void writeAndSync(Path path, SaveGame saveGame) throws IOException {
        try (
            FileChannel channel = FileChannel.open(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Channels.newOutputStream(channel)))
        ) {
            out.writeInt(MAGIC);
            out.writeInt(SaveGame.CURRENT_VERSION);
            out.writeLong(saveGame.worldSeed());
            out.writeLong(saveGame.savedAtEpochMillis());
            out.writeFloat(saveGame.worldTime());

            SaveGame.PlayerData player = saveGame.player();
            out.writeFloat(player.x());
            out.writeFloat(player.z());
            out.writeFloat(player.facing());
            out.writeInt(player.wood());
            out.writeInt(player.apples());

            SaveGame.PushikData pushik = saveGame.pushik();
            out.writeFloat(pushik.x());
            out.writeFloat(pushik.z());
            out.writeFloat(pushik.heading());

            out.writeInt(saveGame.horses().size());
            for (SaveGame.HorseData horse : saveGame.horses()) {
                out.writeLong(horse.id().getMostSignificantBits());
                out.writeLong(horse.id().getLeastSignificantBits());
                out.writeUTF(horse.name());
                out.writeFloat(horse.x());
                out.writeFloat(horse.z());
                out.writeFloat(horse.heading());
                out.writeFloat(horse.trust());
                out.writeFloat(horse.stamina());
                out.writeBoolean(horse.tamed());
            }

            out.writeInt(saveGame.fences().size());
            for (SaveGame.FenceData fence : saveGame.fences()) {
                out.writeFloat(fence.x());
                out.writeFloat(fence.z());
                out.writeFloat(fence.heading());
            }

            out.writeInt(saveGame.harvestedTreeIds().size());
            for (int treeId : saveGame.harvestedTreeIds()) {
                out.writeInt(treeId);
            }

            out.flush();
            channel.force(true);
        }
    }

    private static int checkedCount(int value, int max, String label) throws IOException {
        if (value < 0 || value > max) {
            throw new IOException("Invalid " + label + " count: " + value);
        }
        return value;
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path slotDirectory(String slot) {
        if (!slot.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Invalid save slot: " + slot);
        }
        return root.resolve("saves").resolve(slot);
    }

    private Path savePath(String slot) {
        return slotDirectory(slot).resolve("save.hbs");
    }

    private Path backupPath(String slot) {
        return slotDirectory(slot).resolve("save.bak");
    }

    private static Path defaultRoot() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "HORSEBOUND");
        }
        return Path.of(System.getProperty("user.home"), ".horsebound");
    }

    static final class SaveException extends RuntimeException {
        SaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
