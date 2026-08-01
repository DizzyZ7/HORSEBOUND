// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Temporary presentation adapter for 0.5.x. It isolates access to legacy private actor fields
 * until LivingRanchScreen is split into explicit renderer/controllers.
 */
final class LivingRanchTelemetryAdapter {
    private final LivingRanchScreen screen;
    private final Field cameraField;
    private final Field playerPositionField;
    private final Field playerFacingField;
    private final Field mountedHorseField;
    private final Field horsesField;
    private Field horseIdField;
    private Field horsePositionField;
    private Field horseHeadingField;
    private Field horseSpeedField;
    private Field horseTamedField;

    LivingRanchTelemetryAdapter(LivingRanchScreen screen) {
        this.screen = screen;
        cameraField = field(LivingRanchScreen.class, "camera");
        playerPositionField = field(LivingRanchScreen.class, "playerPosition");
        playerFacingField = field(LivingRanchScreen.class, "playerFacing");
        mountedHorseField = field(LivingRanchScreen.class, "mountedHorse");
        horsesField = field(LivingRanchScreen.class, "horses");
    }

    PerspectiveCamera camera() {
        return (PerspectiveCamera) get(cameraField, screen);
    }

    ActorPose actorPose() {
        Object mounted = get(mountedHorseField, screen);
        if (mounted != null) {
            prepareHorseFields(mounted.getClass());
            Vector3 position = (Vector3) get(horsePositionField, mounted);
            return new ActorPose(position.x, position.z, getFloat(horseHeadingField, mounted), true);
        }
        Vector3 player = (Vector3) get(playerPositionField, screen);
        return new ActorPose(player.x, player.z, getFloat(playerFacingField, screen), false);
    }

    void setActorPosition(float x, float z) {
        Object mounted = get(mountedHorseField, screen);
        if (mounted != null) {
            prepareHorseFields(mounted.getClass());
            setXZ((Vector3) get(horsePositionField, mounted), x, z);
            return;
        }
        setXZ((Vector3) get(playerPositionField, screen), x, z);
    }

    boolean setHorsePosition(UUID id, float x, float z) {
        if (id == null) return false;
        List<?> actors = (List<?>) get(horsesField, screen);
        for (Object actor : actors) {
            prepareHorseFields(actor.getClass());
            if (!id.equals(get(horseIdField, actor))) continue;
            setXZ((Vector3) get(horsePositionField, actor), x, z);
            return true;
        }
        return false;
    }

    List<HorseTelemetry> horses() {
        List<?> actors = (List<?>) get(horsesField, screen);
        List<HorseTelemetry> result = new ArrayList<>(actors.size());
        Object mounted = get(mountedHorseField, screen);
        for (Object actor : actors) {
            prepareHorseFields(actor.getClass());
            Vector3 position = (Vector3) get(horsePositionField, actor);
            result.add(new HorseTelemetry(
                (UUID) get(horseIdField, actor),
                position.x,
                position.z,
                getFloat(horseSpeedField, actor),
                actor == mounted,
                getBoolean(horseTamedField, actor)
            ));
        }
        return List.copyOf(result);
    }

    private void prepareHorseFields(Class<?> type) {
        if (horseIdField != null) return;
        horseIdField = field(type, "id");
        horsePositionField = field(type, "position");
        horseHeadingField = field(type, "heading");
        horseSpeedField = field(type, "speed");
        horseTamedField = field(type, "tamed");
    }

    private static void setXZ(Vector3 position, float x, float z) {
        if (!Float.isFinite(x) || !Float.isFinite(z)) return;
        position.x = x;
        position.z = z;
    }

    private static Field field(Class<?> type, String name) {
        try {
            Field value = type.getDeclaredField(name);
            value.setAccessible(true);
            return value;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("HORSEBOUND legacy telemetry field is unavailable: " + name, ex);
        }
    }

    private static Object get(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("HORSEBOUND could not read legacy telemetry: " + field.getName(), ex);
        }
    }

    private static float getFloat(Field field, Object target) {
        try {
            return field.getFloat(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("HORSEBOUND could not read legacy float: " + field.getName(), ex);
        }
    }

    private static boolean getBoolean(Field field, Object target) {
        try {
            return field.getBoolean(target);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("HORSEBOUND could not read legacy boolean: " + field.getName(), ex);
        }
    }

    record ActorPose(float x, float z, float heading, boolean mounted) {
    }

    record HorseTelemetry(UUID id, float x, float z, float speed, boolean mounted, boolean tamed) {
    }
}
