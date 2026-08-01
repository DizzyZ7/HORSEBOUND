// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Provisional stylized primitives; production GLB assets replace these in the presentation pass. */
final class HomesteadModels implements Disposable {
    private static final long ATTRS = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    private final EnumMap<HomesteadStructureType, Model> normal = new EnumMap<>(HomesteadStructureType.class);
    private final EnumMap<HomesteadStructureType, Model> validPreview = new EnumMap<>(HomesteadStructureType.class);
    private final EnumMap<HomesteadStructureType, Model> invalidPreview = new EnumMap<>(HomesteadStructureType.class);

    HomesteadModels() {
        for (HomesteadStructureType type : HomesteadStructureType.values()) {
            normal.put(type, create(type, false, true));
            validPreview.put(type, create(type, true, true));
            invalidPreview.put(type, create(type, true, false));
        }
    }

    Model normal(HomesteadStructureType type) {
        return normal.get(type);
    }

    Model preview(HomesteadStructureType type, boolean valid) {
        return (valid ? validPreview : invalidPreview).get(type);
    }

    private static Model create(HomesteadStructureType type, boolean preview, boolean valid) {
        ModelBuilder builder = new ModelBuilder();
        Material wood = material(preview ? (valid ? 0.24f : 0.60f) : 0.42f, preview ? 0.72f : 0.23f, preview ? 0.38f : 0.10f, preview ? 0.46f : 1f);
        Material darkWood = material(preview ? (valid ? 0.20f : 0.55f) : 0.23f, preview ? 0.58f : 0.12f, preview ? 0.30f : 0.055f, preview ? 0.46f : 1f);
        Material stone = material(preview ? (valid ? 0.30f : 0.66f) : 0.38f, preview ? 0.76f : 0.40f, preview ? 0.43f : 0.42f, preview ? 0.46f : 1f);
        Material hay = material(preview ? (valid ? 0.42f : 0.72f) : 0.78f, preview ? 0.82f : 0.62f, preview ? 0.35f : 0.18f, preview ? 0.46f : 1f);
        Material water = material(0.18f, 0.55f, 0.78f, preview ? 0.42f : 0.74f);
        builder.begin();

        switch (type) {
            case FENCE -> {
                box(builder, "post_l", wood, -1.20f, 0.75f, 0f, 0.22f, 1.50f, 0.22f);
                box(builder, "post_r", wood, 1.20f, 0.75f, 0f, 0.22f, 1.50f, 0.22f);
                box(builder, "rail_a", darkWood, 0f, 0.48f, 0f, 2.45f, 0.18f, 0.18f);
                box(builder, "rail_b", darkWood, 0f, 1.02f, 0f, 2.45f, 0.18f, 0.18f);
            }
            case GATE -> {
                box(builder, "post_l", wood, -1.45f, 0.90f, 0f, 0.28f, 1.80f, 0.28f);
                box(builder, "post_r", wood, 1.45f, 0.90f, 0f, 0.28f, 1.80f, 0.28f);
                box(builder, "gate_a", darkWood, 0f, 0.55f, 0f, 2.65f, 0.20f, 0.20f);
                box(builder, "gate_b", darkWood, 0f, 1.15f, 0f, 2.65f, 0.20f, 0.20f);
                box(builder, "brace", darkWood, 0f, 0.85f, 0f, 2.10f, 0.14f, 0.14f, 18f);
            }
            case FEEDER -> {
                box(builder, "base", darkWood, 0f, 0.42f, 0f, 1.65f, 0.68f, 0.85f);
                box(builder, "hay", hay, 0f, 0.78f, 0f, 1.45f, 0.34f, 0.68f);
                box(builder, "leg_l", wood, -0.62f, 0.22f, 0f, 0.18f, 0.44f, 0.18f);
                box(builder, "leg_r", wood, 0.62f, 0.22f, 0f, 0.18f, 0.44f, 0.18f);
            }
            case WATER_TROUGH -> {
                box(builder, "trough", stone, 0f, 0.42f, 0f, 2.05f, 0.72f, 0.95f);
                box(builder, "water", water, 0f, 0.80f, 0f, 1.70f, 0.06f, 0.66f);
            }
            case HAY_STORAGE -> {
                box(builder, "frame", wood, 0f, 0.75f, 0f, 2.25f, 1.50f, 1.40f);
                box(builder, "hay", hay, 0f, 0.85f, 0f, 1.90f, 1.25f, 1.10f);
                box(builder, "roof", darkWood, 0f, 1.62f, 0f, 2.55f, 0.18f, 1.65f);
            }
            case CHEST -> {
                box(builder, "chest", wood, 0f, 0.48f, 0f, 1.25f, 0.76f, 0.78f);
                box(builder, "lid", darkWood, 0f, 0.91f, 0f, 1.34f, 0.16f, 0.86f);
                box(builder, "lock", stone, 0f, 0.56f, 0.42f, 0.16f, 0.25f, 0.08f);
            }
            case STALL -> {
                box(builder, "post_fl", wood, -1.45f, 1.20f, 1.10f, 0.28f, 2.40f, 0.28f);
                box(builder, "post_fr", wood, 1.45f, 1.20f, 1.10f, 0.28f, 2.40f, 0.28f);
                box(builder, "post_bl", wood, -1.45f, 1.20f, -1.10f, 0.28f, 2.40f, 0.28f);
                box(builder, "post_br", wood, 1.45f, 1.20f, -1.10f, 0.28f, 2.40f, 0.28f);
                box(builder, "back", darkWood, 0f, 1.05f, -1.16f, 3.00f, 1.65f, 0.18f);
                box(builder, "roof", darkWood, 0f, 2.48f, 0f, 3.35f, 0.20f, 2.70f);
            }
        }
        return builder.end();
    }

    private static Material material(float r, float g, float b, float alpha) {
        Material material = new Material(ColorAttribute.createDiffuse(new Color(r, g, b, alpha)));
        if (alpha < 1f) material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha));
        return material;
    }

    private static void box(
        ModelBuilder builder,
        String name,
        Material material,
        float x,
        float y,
        float z,
        float sx,
        float sy,
        float sz
    ) {
        box(builder, name, material, x, y, z, sx, sy, sz, 0f);
    }

    private static void box(
        ModelBuilder builder,
        String name,
        Material material,
        float x,
        float y,
        float z,
        float sx,
        float sy,
        float sz,
        float rotationZ
    ) {
        Matrix4 transform = new Matrix4().setToTranslation(x, y, z).rotate(0f, 0f, 1f, rotationZ).scale(sx, sy, sz);
        MeshPartBuilder part = builder.part(name, GL20.GL_TRIANGLES, ATTRS, material);
        part.setVertexTransform(transform);
        part.box(1f, 1f, 1f);
    }

    @Override
    public void dispose() {
        for (Map<HomesteadStructureType, Model> map : List.of(normal, validPreview, invalidPreview)) {
            for (Model model : map.values()) model.dispose();
        }
    }
}
