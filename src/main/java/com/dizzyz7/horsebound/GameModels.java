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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

final class GameModels implements Disposable {
    private static final long ATTRS = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    final Model player;
    final Model horse;
    final Model pushik;
    final Model tree;
    final Model rock;
    final Model fence;
    final Model water;

    GameModels() {
        player = createPlayer();
        horse = createHorse();
        pushik = createPushik();
        tree = createTree();
        rock = createRock();
        fence = createFence();
        water = createWater();
    }

    private static Material material(float r, float g, float b) {
        return new Material(ColorAttribute.createDiffuse(new Color(r, g, b, 1f)));
    }

    private static MeshPartBuilder part(ModelBuilder builder, String name, Material material, Matrix4 transform) {
        MeshPartBuilder part = builder.part(name, GL20.GL_TRIANGLES, ATTRS, material);
        part.setVertexTransform(transform);
        return part;
    }

    private static Matrix4 transform(float x, float y, float z, float sx, float sy, float sz) {
        return new Matrix4().setToTranslation(x, y, z).scale(sx, sy, sz);
    }

    private static Model createPlayer() {
        ModelBuilder builder = new ModelBuilder();
        Material coat = material(0.15f, 0.25f, 0.32f);
        Material skin = material(0.76f, 0.58f, 0.42f);
        Material boots = material(0.08f, 0.07f, 0.06f);
        builder.begin();
        part(builder, "body", coat, transform(0f, 1.10f, 0f, 0.68f, 1.15f, 0.42f)).box(1f, 1f, 1f);
        part(builder, "head", skin, transform(0f, 2.02f, 0f, 0.54f, 0.60f, 0.54f)).sphere(1f, 1f, 1f, 10, 8);
        part(builder, "leg_l", boots, transform(-0.21f, 0.45f, 0f, 0.22f, 0.90f, 0.25f)).box(1f, 1f, 1f);
        part(builder, "leg_r", boots, transform(0.21f, 0.45f, 0f, 0.22f, 0.90f, 0.25f)).box(1f, 1f, 1f);
        return builder.end();
    }

    private static Model createHorse() {
        ModelBuilder builder = new ModelBuilder();
        Material coat = material(0.42f, 0.22f, 0.10f);
        Material dark = material(0.08f, 0.045f, 0.03f);
        Material muzzle = material(0.30f, 0.16f, 0.10f);
        builder.begin();

        part(builder, "horse_body", coat, transform(0f, 1.45f, 0f, 1.12f, 0.90f, 2.15f)).sphere(1f, 1f, 1f, 12, 8);

        Matrix4 neck = transform(0f, 2.05f, 0.88f, 0.52f, 1.28f, 0.52f).rotate(Vector3.X, -22f);
        part(builder, "horse_neck", coat, neck).box(1f, 1f, 1f);
        part(builder, "horse_head", coat, transform(0f, 2.66f, 1.35f, 0.58f, 0.56f, 0.90f)).sphere(1f, 1f, 1f, 10, 8);
        part(builder, "horse_muzzle", muzzle, transform(0f, 2.56f, 1.78f, 0.48f, 0.38f, 0.48f)).sphere(1f, 1f, 1f, 9, 7);

        float[] xs = {-0.38f, 0.38f};
        float[] zs = {-0.66f, 0.66f};
        int leg = 0;
        for (float x : xs) {
            for (float z : zs) {
                part(builder, "horse_leg_" + leg++, coat, transform(x, 0.67f, z, 0.24f, 1.34f, 0.27f)).box(1f, 1f, 1f);
                part(builder, "horse_hoof_" + leg, dark, transform(x, 0.12f, z + 0.02f, 0.28f, 0.22f, 0.34f)).box(1f, 1f, 1f);
            }
        }

        part(builder, "mane", dark, transform(0f, 2.20f, 0.52f, 0.14f, 1.22f, 0.90f).rotate(Vector3.X, -18f)).box(1f, 1f, 1f);
        part(builder, "tail", dark, transform(0f, 1.45f, -1.26f, 0.23f, 1.20f, 0.25f).rotate(Vector3.X, 28f)).box(1f, 1f, 1f);
        part(builder, "ear_l", coat, transform(-0.19f, 3.02f, 1.32f, 0.15f, 0.45f, 0.16f)).cone(1f, 1f, 1f, 6);
        part(builder, "ear_r", coat, transform(0.19f, 3.02f, 1.32f, 0.15f, 0.45f, 0.16f)).cone(1f, 1f, 1f, 6);
        return builder.end();
    }

    private static Model createPushik() {
        ModelBuilder builder = new ModelBuilder();
        Material black = material(0.012f, 0.012f, 0.016f);
        Material eye = material(0.93f, 0.64f, 0.16f);
        builder.begin();

        part(builder, "pushik_fluffy_body", black, transform(0f, 0.42f, 0f, 0.72f, 0.58f, 1.02f)).sphere(1f, 1f, 1f, 12, 9);
        part(builder, "pushik_chest_fluff", black, transform(0f, 0.58f, 0.38f, 0.62f, 0.68f, 0.60f)).sphere(1f, 1f, 1f, 11, 8);
        part(builder, "pushik_head", black, transform(0f, 0.87f, 0.58f, 0.64f, 0.60f, 0.62f)).sphere(1f, 1f, 1f, 12, 9);

        float[] pawX = {-0.28f, 0.28f};
        float[] pawZ = {-0.34f, 0.42f};
        int paw = 0;
        for (float x : pawX) {
            for (float z : pawZ) {
                part(builder, "pushik_fluffy_paw_" + paw++, black, transform(x, 0.17f, z, 0.34f, 0.30f, 0.40f)).sphere(1f, 1f, 1f, 9, 7);
            }
        }

        part(builder, "pushik_ear_l", black, transform(-0.23f, 1.25f, 0.56f, 0.28f, 0.48f, 0.30f)).cone(1f, 1f, 1f, 7);
        part(builder, "pushik_ear_r", black, transform(0.23f, 1.25f, 0.56f, 0.28f, 0.48f, 0.30f)).cone(1f, 1f, 1f, 7);

        Matrix4 tail = transform(0.48f, 0.54f, -0.58f, 0.24f, 0.24f, 1.28f).rotate(Vector3.Y, -28f).rotate(Vector3.X, 20f);
        part(builder, "pushik_fluffy_tail", black, tail).cylinder(1f, 1f, 1f, 9);

        part(builder, "pushik_eye_l", eye, transform(-0.18f, 0.94f, 0.86f, 0.10f, 0.10f, 0.07f)).sphere(1f, 1f, 1f, 7, 6);
        part(builder, "pushik_eye_r", eye, transform(0.18f, 0.94f, 0.86f, 0.10f, 0.10f, 0.07f)).sphere(1f, 1f, 1f, 7, 6);
        return builder.end();
    }

    private static Model createTree() {
        ModelBuilder builder = new ModelBuilder();
        Material trunk = material(0.27f, 0.15f, 0.07f);
        Material leaves = material(0.18f, 0.42f, 0.18f);
        builder.begin();
        part(builder, "tree_trunk", trunk, transform(0f, 1.55f, 0f, 0.56f, 3.10f, 0.56f)).cylinder(1f, 1f, 1f, 8);
        part(builder, "tree_crown_low", leaves, transform(0f, 3.35f, 0f, 2.7f, 2.6f, 2.7f)).sphere(1f, 1f, 1f, 10, 7);
        part(builder, "tree_crown_high", leaves, transform(0.3f, 4.45f, -0.15f, 2.15f, 2.1f, 2.15f)).sphere(1f, 1f, 1f, 10, 7);
        return builder.end();
    }

    private static Model createRock() {
        ModelBuilder builder = new ModelBuilder();
        return builder.createSphere(1.5f, 1.0f, 1.25f, 8, 6, material(0.34f, 0.36f, 0.34f), ATTRS);
    }

    private static Model createFence() {
        ModelBuilder builder = new ModelBuilder();
        Material wood = material(0.47f, 0.28f, 0.12f);
        builder.begin();
        part(builder, "post_l", wood, transform(-1.75f, 0.80f, 0f, 0.24f, 1.60f, 0.24f)).box(1f, 1f, 1f);
        part(builder, "post_r", wood, transform(1.75f, 0.80f, 0f, 0.24f, 1.60f, 0.24f)).box(1f, 1f, 1f);
        part(builder, "rail_low", wood, transform(0f, 0.60f, 0f, 3.55f, 0.18f, 0.18f)).box(1f, 1f, 1f);
        part(builder, "rail_high", wood, transform(0f, 1.15f, 0f, 3.55f, 0.18f, 0.18f)).box(1f, 1f, 1f);
        return builder.end();
    }

    private static Model createWater() {
        ModelBuilder builder = new ModelBuilder();
        Material water = new Material(
            ColorAttribute.createDiffuse(new Color(0.22f, 0.54f, 0.72f, 0.72f)),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.72f)
        );
        return builder.createCylinder(Terrain.LAKE_RADIUS * 2f, 0.08f, Terrain.LAKE_RADIUS * 2f, 40, water, ATTRS);
    }

    @Override
    public void dispose() {
        player.dispose();
        horse.dispose();
        pushik.dispose();
        tree.dispose();
        rock.dispose();
        fence.dispose();
        water.dispose();
    }
}
