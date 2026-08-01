// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.Map;

/** Optional presentation audio. Missing or unsupported audio becomes a safe no-op. */
final class RanchAudio implements Disposable {
    private final Map<Cue, Sound> sounds = new EnumMap<>(Cue.class);

    RanchAudio() {
        for (Cue cue : Cue.values()) {
            try {
                sounds.put(cue, Gdx.audio.newSound(Gdx.files.internal(cue.resourcePath())));
            } catch (RuntimeException | LinkageError ex) {
                if (Gdx.app != null) {
                    Gdx.app.debug("HORSEBOUND", "Optional ranch sound unavailable: " + cue.resourcePath());
                }
            }
        }
    }

    void play(Cue cue) {
        if (cue == null) return;
        Sound sound = sounds.get(cue);
        if (sound == null) return;
        try {
            sound.play(cue.volume(), cue.pitch(), 0f);
        } catch (RuntimeException ex) {
            if (Gdx.app != null) Gdx.app.debug("HORSEBOUND", "Ranch sound playback skipped: " + cue);
        }
    }

    int loadedCount() {
        return sounds.size();
    }

    @Override
    public void dispose() {
        for (Sound sound : sounds.values()) {
            try {
                sound.dispose();
            } catch (RuntimeException ignored) {
                // Audio teardown must never block clean game shutdown.
            }
        }
        sounds.clear();
    }

    enum Cue {
        BUILD("audio/build.wav", 0.34f, 0.96f),
        MOVE("audio/move.wav", 0.28f, 1.02f),
        DISMANTLE("audio/dismantle.wav", 0.34f, 0.92f),
        GATE_OPEN("audio/gate-open.wav", 0.30f, 1.00f),
        GATE_CLOSE("audio/gate-close.wav", 0.32f, 0.94f),
        INVENTORY_TRANSFER("audio/inventory-transfer.wav", 0.25f, 1.05f);

        private final String resourcePath;
        private final float volume;
        private final float pitch;

        Cue(String resourcePath, float volume, float pitch) {
            this.resourcePath = resourcePath;
            this.volume = volume;
            this.pitch = pitch;
        }

        String resourcePath() {
            return resourcePath;
        }

        float volume() {
            return volume;
        }

        float pitch() {
            return pitch;
        }
    }
}
