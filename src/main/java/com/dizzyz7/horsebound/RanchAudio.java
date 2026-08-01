// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

/**
 * Shared procedural interaction audio. It requires no external asset files and degrades to no-op
 * when the platform has no usable audio device.
 */
final class RanchAudio {
    static final int SAMPLE_RATE = 22_050;
    private static final Object LOCK = new Object();
    private static volatile float masterVolume = GameSettings.DEFAULT_SFX_VOLUME;
    private static Engine shared;
    private static boolean unavailable;

    private RanchAudio() {
    }

    static void setMasterVolume(float value) {
        if (!Float.isFinite(value)) value = GameSettings.DEFAULT_SFX_VOLUME;
        masterVolume = Math.max(GameSettings.MIN_SFX_VOLUME, Math.min(GameSettings.MAX_SFX_VOLUME, value));
    }

    static float masterVolume() {
        return masterVolume;
    }

    static void play(Cue cue) {
        if (cue == null || masterVolume <= 0f) return;
        Engine engine = engine();
        if (engine != null) engine.play(cue);
    }

    static void shutdown() {
        Engine engine;
        synchronized (LOCK) {
            engine = shared;
            shared = null;
            unavailable = false;
        }
        if (engine != null) engine.dispose();
    }

    static float[] synthesize(Cue cue) {
        if (cue == null) return new float[0];
        int sampleCount = Math.max(1, Math.round(cue.durationSeconds() * SAMPLE_RATE));
        float[] samples = new float[sampleCount];
        double phase = 0d;
        long noiseState = 0x9E3779B97F4A7C15L ^ cue.ordinal();
        for (int i = 0; i < sampleCount; i++) {
            float progress = i / (float) Math.max(1, sampleCount - 1);
            float frequency = cue.startFrequency()
                + (cue.endFrequency() - cue.startFrequency()) * progress;
            phase += Math.PI * 2d * frequency / SAMPLE_RATE;
            noiseState ^= noiseState << 13;
            noiseState ^= noiseState >>> 7;
            noiseState ^= noiseState << 17;
            float noise = ((noiseState >>> 40) / (float) (1 << 23)) * 2f - 1f;
            float envelope = attackRelease(progress, cue.attackFraction());
            float tonal = (float) Math.sin(phase);
            float sample = (tonal * (1f - cue.noiseMix()) + noise * cue.noiseMix())
                * envelope
                * cue.volume();
            samples[i] = Math.max(-1f, Math.min(1f, sample));
        }
        return samples;
    }

    static float[] applyMasterVolume(float[] samples, float volume) {
        if (samples == null || samples.length == 0) return new float[0];
        float safeVolume = Float.isFinite(volume)
            ? Math.max(GameSettings.MIN_SFX_VOLUME, Math.min(GameSettings.MAX_SFX_VOLUME, volume))
            : GameSettings.DEFAULT_SFX_VOLUME;
        if (safeVolume == 1f) return samples;
        float[] scaled = new float[samples.length];
        for (int i = 0; i < samples.length; i++) scaled[i] = samples[i] * safeVolume;
        return scaled;
    }

    private static Engine engine() {
        synchronized (LOCK) {
            if (shared != null) return shared;
            if (unavailable || Gdx.audio == null) return null;
            try {
                shared = new Engine(Gdx.audio.newAudioDevice(SAMPLE_RATE, true));
                return shared;
            } catch (RuntimeException | LinkageError ex) {
                unavailable = true;
                if (Gdx.app != null) Gdx.app.debug("HORSEBOUND", "Procedural ranch audio unavailable.");
                return null;
            }
        }
    }

    private static float attackRelease(float progress, float attackFraction) {
        float safeAttack = Math.max(0.01f, Math.min(0.40f, attackFraction));
        float attack = Math.min(1f, progress / safeAttack);
        float releaseProgress = (progress - safeAttack) / (1f - safeAttack);
        float release = 1f - Math.max(0f, Math.min(1f, releaseProgress));
        return attack * release * release;
    }

    enum Cue {
        BUILD(170f, 285f, 0.18f, 0.30f, 0.22f, 0.15f),
        MOVE(235f, 180f, 0.12f, 0.23f, 0.18f, 0.10f),
        DISMANTLE(210f, 85f, 0.20f, 0.31f, 0.42f, 0.08f),
        GATE_OPEN(125f, 205f, 0.22f, 0.27f, 0.34f, 0.12f),
        GATE_CLOSE(195f, 105f, 0.20f, 0.29f, 0.38f, 0.10f),
        INVENTORY_TRANSFER(440f, 540f, 0.08f, 0.20f, 0.08f, 0.20f),
        UNDO(310f, 190f, 0.14f, 0.25f, 0.14f, 0.12f);

        private final float startFrequency;
        private final float endFrequency;
        private final float durationSeconds;
        private final float volume;
        private final float noiseMix;
        private final float attackFraction;

        Cue(
            float startFrequency,
            float endFrequency,
            float durationSeconds,
            float volume,
            float noiseMix,
            float attackFraction
        ) {
            this.startFrequency = startFrequency;
            this.endFrequency = endFrequency;
            this.durationSeconds = durationSeconds;
            this.volume = volume;
            this.noiseMix = noiseMix;
            this.attackFraction = attackFraction;
        }

        float startFrequency() {
            return startFrequency;
        }

        float endFrequency() {
            return endFrequency;
        }

        float durationSeconds() {
            return durationSeconds;
        }

        float volume() {
            return volume;
        }

        float noiseMix() {
            return noiseMix;
        }

        float attackFraction() {
            return attackFraction;
        }
    }

    private static final class Engine {
        private final AudioDevice device;
        private final ExecutorService executor;
        private final Map<Cue, float[]> samples = new EnumMap<>(Cue.class);

        private Engine(AudioDevice device) {
            this.device = device;
            for (Cue cue : Cue.values()) samples.put(cue, synthesize(cue));
            ThreadFactory factory = runnable -> {
                Thread thread = new Thread(runnable, "horsebound-ranch-audio");
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((ignored, error) -> {
                    if (Gdx.app != null) Gdx.app.debug("HORSEBOUND", "Ranch audio task skipped.");
                });
                return thread;
            };
            executor = Executors.newSingleThreadExecutor(factory);
        }

        private void play(Cue cue) {
            float[] cueSamples = samples.get(cue);
            if (cueSamples == null || cueSamples.length == 0) return;
            float volumeAtDispatch = masterVolume;
            if (volumeAtDispatch <= 0f) return;
            try {
                executor.execute(() -> {
                    try {
                        float[] output = applyMasterVolume(cueSamples, volumeAtDispatch);
                        if (output.length > 0) device.writeSamples(output, 0, output.length);
                    } catch (RuntimeException ex) {
                        if (Gdx.app != null) Gdx.app.debug("HORSEBOUND", "Ranch audio playback skipped.");
                    }
                });
            } catch (RejectedExecutionException ignored) {
                // Teardown won the race; gameplay must continue silently.
            }
        }

        private void dispose() {
            executor.shutdownNow();
            try {
                device.dispose();
            } catch (RuntimeException ignored) {
                // Audio teardown must never block clean game shutdown.
            }
            samples.clear();
        }
    }
}
