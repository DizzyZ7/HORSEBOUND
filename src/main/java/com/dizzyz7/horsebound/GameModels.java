// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
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

    private static Material matte(float r, float g, float b) {
        return new Material(
            ColorAttribute.createDiffuse(new Color(r, g, b, 1f)),
            ColorAttribute.createSpecular(new Color(0.10f, 0.10f, 0.10f, 1f)),
            FloatAttribute.createShininess(5f)
        );
    }

    private static Material satin(float r, float g, float b, float shininess) {
        return new Material(
            ColorAttribute.createDiffuse(new Color(r, g, b, 1f)),
            ColorAttribute.createSpecular(new Color(0.28f, 0.28f, 0.25f, 1f)),
            FloatAttribute.createShininess(shininess)
        );
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
        Material coat = matte(0.12f, 0.25f, 0.34f);
        Material shirt = matte(0.72f, 0.67f, 0.52f);
        Material skin = satin(0.76f, 0.58f, 0.42f, 7f);
        Material hair = matte(0.10f, 0.065f, 0.045f);
        Material trousers = matte(0.12f, 0.13f, 0.14f);
        Material boots = satin(0.07f, 0.055f, 0.04f, 14f);
        builder.begin();

        part(builder, "body", coat, transform(0f, 1.28f, 0f, 0.72f, 1.05f, 0.48f)).sphere(1f, 1f, 1f, 14, 10);
        part(builder, "shirt_front", shirt, transform(0f, 1.38f, 0.42f, 0.44f, 0.62f, 0.08f)).box(1f, 1f, 1f);
        part(builder, "head", skin, transform(0f, 2.16f, 0f, 0.54f, 0.61f, 0.54f)).sphere(1f, 1f, 1f, 14, 10);
        part(builder, "hair", hair, transform(0f, 2.47f, -0.04f, 0.56f, 0.25f, 0.56f)).sphere(1f, 1f, 1f, 12, 8);

        part(builder, "arm_l", coat, transform(-0.53f, 1.27f, 0f, 0.20f, 0.88f, 0.24f).rotate(Vector3.Z, -7f)).cylinder(1f, 1f, 1f, 9);
        part(builder, "arm_r", coat, transform(0.53f, 1.27f, 0f, 0.20f, 0.88f, 0.24f).rotate(Vector3.Z, 7f)).cylinder(1f, 1f, 1f, 9);
        part(builder, "hand_l", skin, transform(-0.58f, 0.85f, 0f, 0.22f, 0.24f, 0.22f)).sphere(1f, 1f, 1f, 9, 7);
        part(builder, "hand_r", skin, transform(0.58f, 0.85f, 0f, 0.22f, 0.24f, 0.22f)).sphere(1f, 1f, 1f, 9, 7);

        part(builder, "leg_l", trousers, transform(-0.22f, 0.55f, 0f, 0.23f, 0.92f, 0.28f)).cylinder(1f, 1f, 1f, 9);
        part(builder, "leg_r", trousers, transform(0.22f, 0.55f, 0f, 0.23f, 0.92f, 0.28f)).cylinder(1f, 1f, 1f, 9);
        part(builder, "boot_l", boots, transform(-0.22f, 0.14f, 0.10f, 0.29f, 0.28f, 0.48f)).box(1f, 1f, 1f);
        part(builder, "boot_r", boots, transform(0.22f, 0.14f, 0.10f, 0.29f, 0.28f, 0.48f)).box(1f, 1f, 1f);
        return builder.end();
    }

    private static Model createHorse() {
        ModelBuilder builder = new ModelBuilder();
        Material coat = satin(0.43f, 0.22f, 0.095f, 12f);
        Material coatLight = satin(0.53f, 0.30f, 0.14f, 10f);
        Material dark = satin(0.065f, 0.038f, 0.025f, 16f);
        Material muzzle = satin(0.30f, 0.16f, 0.10f, 9f);
        Material eye = satin(0.025f, 0.018f, 0.012f, 28f);
        Material blaze = matte(0.80f, 0.73f, 0.58f);
        builder.begin();

        part(builder, "horse_body", coat, transform(0f, 1.48f, 0f, 1.18f, 0.94f, 2.18f)).sphere(1f, 1f, 1f, 18, 12);
        part(builder, "horse_chest", coatLight, transform(0f, 1.60f, 0.82f, 0.98f, 0.96f, 0.86f)).sphere(1f, 1f, 1f, 15, 10);
        part(builder, "horse_haunch", coat, transform(0f, 1.55f, -0.88f, 1.02f, 0.98f, 0.92f)).sphere(1f, 1f, 1f, 15, 10);

        Matrix4 neck = transform(0f, 2.10f, 0.88f, 0.56f, 1.34f, 0.58f).rotate(Vector3.X, -22f);
        part(builder, "horse_neck", coat, neck).sphere(1f, 1f, 1f, 14, 10);
        part(builder, "horse_head", coat, transform(0f, 2.72f, 1.38f, 0.60f, 0.58f, 0.92f)).sphere(1f, 1f, 1f, 14, 10);
        part(builder, "horse_muzzle", muzzle, transform(0f, 2.58f, 1.82f, 0.50f, 0.39f, 0.52f)).sphere(1f, 1f, 1f, 12, 9);
        part(builder, "horse_blaze", blaze, transform(0f, 2.78f, 1.84f, 0.14f, 0.43f, 0.055f)).sphere(1f, 1f, 1f, 8, 6);

        part(builder, "horse_eye_l", eye, transform(-0.33f, 2.83f, 1.68f, 0.095f, 0.095f, 0.065f)).sphere(1f, 1f, 1f, 8, 6);
        part(builder, "horse_eye_r", eye, transform(0.33f, 2.83f, 1.68f, 0.095f, 0.095f, 0.065f)).sphere(1f, 1f, 1f, 8, 6);
        part(builder, "nostril_l", dark, transform(-0.20f, 2.58f, 2.06f, 0.075f, 0.050f, 0.035f)).sphere(1f, 1f, 1f, 7, 5);
        part(builder, "nostril_r", dark, transform(0.20f, 2.58f, 2.06f, 0.075f, 0.050f, 0.035f)).sphere(1f, 1f, 1f, 7, 5);

        float[] xs = {-0.40f, 0.40f};
        float[] zs = {-0.70f, 0.70f};
        int leg = 0;
        for (float x : xs) {
            for (float z : zs) {
                String id = Integer.toString(leg++);
                part(builder, "horse_upper_leg_" + id, coat, transform(x, 0.84f, z, 0.29f, 1.12f, 0.32f)).cylinder(1f, 1f, 1f, 9);
                part(builder, "horse_knee_" + id, coatLight, transform(x, 0.40f, z, 0.33f, 0.34f, 0.35f)).sphere(1f, 1f, 1f, 9, 7);
                part(builder, "horse_lower_leg_" + id, coatLight, transform(x, 0.24f, z, 0.22f, 0.50f, 0.25f)).cylinder(1f, 1f, 1f, 8);
                part(builder, "horse_hoof_" + id, dark, transform(x, 0.09f, z + 0.03f, 0.31f, 0.20f, 0.39f)).box(1f, 1f, 1f);
            }
        }

        part(builder, "mane", dark, transform(0f, 2.25f, 0.48f, 0.16f, 1.28f, 0.98f).rotate(Vector3.X, -18f)).box(1f, 1f, 1f);
        part(builder, "forelock", dark, transform(0f, 3.00f, 1.50f, 0.22f, 0.52f, 0.18f).rotate(Vector3.X, 15f)).cone(1f, 1f, 1f, 7);
        part(builder, "tail", dark, transform(0f, 1.46f, -1.35f, 0.28f, 1.34f, 0.30f).rotate(Vector3.X, 28f)).cylinder(1f, 1f, 1f, 10);
        part(builder, "ear_l", coat, transform(-0.19f, 3.08f, 1.32f, 0.16f, 0.48f, 0.17f)).cone(1f, 1f, 1f, 7);
        part(builder, "ear_r", coat, transform(0.19f, 3.08f, 1.32f, 0.16f, 0.48f, 0.17f)).cone(1f, 1f, 1f, 7);
        return builder.end();
    }

    private static Model createPushik() {
        ModelBuilder builder = new ModelBuilder();
        Material black = satin(0.010f, 0.010f, 0.014f, 18f);
        Material eye = satin(0.93f, 0.64f, 0.16f, 34f);
        Material pupil = satin(0.015f, 0.012f, 0.010f, 28f);
        builder.begin();

        part(builder, "pushik_fluffy_body", black, transform(0f, 0.42f, 0f, 0.74f, 0.60f, 1.04f)).sphere(1f, 1f, 1f, 16, 11);
        part(builder, "pushik_chest_fluff", black, transform(0f, 0.60f, 0.38f, 0.64f, 0.72f, 0.62f)).sphere(1f, 1f, 1f, 15, 10);
        part(builder, "pushik_head", black, transform(0f, 0.89f, 0.60f, 0.66f, 0.62f, 0.64f)).sphere(1f, 1f, 1f, 16, 11);
        part(builder, "pushik_cheek_l", black, transform(-0.27f, 0.77f, 0.79f, 0.34f, 0.32f, 0.28f)).sphere(1f, 1f, 1f, 11, 8);
        part(builder, "pushik_cheek_r", black, transform(0.27f, 0.77f, 0.79f, 0.34f, 0.32f, 0.28f)).sphere(1f, 1f, 1f, 11, 8);

        float[] pawX = {-0.28f, 0.28f};
        float[] pawZ = {-0.34f, 0.42f};
        int paw = 0;
        for (float x : pawX) {
            for (float z : pawZ) {
                part(builder, "pushik_fluffy_paw_" + paw++, black, transform(x, 0.17f, z, 0.35f, 0.31f, 0.42f)).sphere(1f, 1f, 1f, 11, 8);
            }
        }

        part(builder, "pushik_ear_l", black, transform(-0.23f, 1.29f, 0.57f, 0.29f, 0.50f, 0.31f)).cone(1f, 1f, 1f, 8);
        part(builder, "pushik_ear_r", black, transform(0.23f, 1.29f, 0.57f, 0.29f, 0.50f, 0.31f)).cone(1f, 1f, 1f, 8);

        Matrix4 tail = transform(0.48f, 0.56f, -0.60f, 0.25f, 0.25f, 1.34f).rotate(Vector3.Y, -28f).rotate(Vector3.X, 20f);
        part(builder, "pushik_fluffy_tail", black, tail).cylinder(1f, 1f, 1f, 11);

        part(builder, "pushik_eye_l", eye, transform(-0.18f, 0.96f, 0.89f, 0.115f, 0.115f, 0.072f)).sphere(1f, 1f, 1f, 9, 7);
        part(builder, "pushik_eye_r", eye, transform(0.18f, 0.96f, 0.89f, 0.115f, 0.115f, 0.072f)).sphere(1f, 1f, 1f, 9, 7);
        part(builder, "pushik_pupil_l", pupil, transform(-0.18f, 0.96f, 0.953f, 0.045f, 0.085f, 0.024f)).sphere(1f, 1f, 1f, 7, 6);
        part(builder, "pushik_pupil_r", pupil, transform(0.18f, 0.96f, 0.953f, 0.045f, 0.085f, 0.024f)).sphere(1f, 1f, 1f, 7, 6);
        return builder.end();
    }

    private static Model createTree() {
        ModelBuilder builder = new ModelBuilder();
        Material trunk = matte(0.25f, 0.135f, 0.060f);
        Material barkLight = matte(0.34f, 0.20f, 0.09f);
        Material leavesLow = matte(0.13f, 0.34f, 0.14f);
        Material leavesMid = matte(0.19f, 0.45f, 0.18f);
        Material leavesHigh = matte(0.27f, 0.53f, 0.22f);
        builder.begin();
        part(builder, "tree_trunk", trunk, transform(0f, 1.55f, 0f, 0.62f, 3.10f, 0.62f)).cylinder(1f, 1f, 1f, 10);
        part(builder, "tree_trunk_highlight", barkLight, transform(0.25f, 1.56f, 0.35f, 0.12f, 2.65f, 0.10f)).box(1f, 1f, 1f);
        part(builder, "tree_branch_l", trunk, transform(-0.63f, 2.63f, 0f, 0.28f, 1.48f, 0.28f).rotate(Vector3.Z, 56f)).cylinder(1f, 1f, 1f, 9);
        part(builder, "tree_branch_r", trunk, transform(0.66f, 2.82f, -0.12f, 0.25f, 1.35f, 0.25f).rotate(Vector3.Z, -52f)).cylinder(1f, 1f, 1f, 9);
        part(builder, "tree_crown_low", leavesLow, transform(-0.35f, 3.42f, 0.05f, 2.85f, 2.50f, 2.75f)).sphere(1f, 1f, 1f, 14, 10);
        part(builder, "tree_crown_mid", leavesMid, transform(0.72f, 4.05f, -0.20f, 2.35f, 2.25f, 2.35f)).sphere(1f, 1f, 1f, 14, 10);
        part(builder, "tree_crown_high", leavesHigh, transform(-0.28f, 4.78f, 0.18f, 2.05f, 1.90f, 2.05f)).sphere(1f, 1f, 1f, 13, 9);
        return builder.end();
    }

    private static Model createRock() {
        ModelBuilder builder = new ModelBuilder();
        return builder.createSphere(1.5f, 1.0f, 1.25f, 12, 8, satin(0.31f, 0.34f, 0.33f, 7f), ATTRS);
    }

    private static Model createFence() {
        ModelBuilder builder = new ModelBuilder();
        Material wood = matte(0.44f, 0.25f, 0.10f);
        Material edge = matte(0.57f, 0.34f, 0.15f);
        builder.begin();
        part(builder, "post_l", wood, transform(-1.75f, 0.80f, 0f, 0.28f, 1.60f, 0.28f)).box(1f, 1f, 1f);
        part(builder, "post_r", wood, transform(1.75f, 0.80f, 0f, 0.28f, 1.60f, 0.28f)).box(1f, 1f, 1f);
        part(builder, "post_l_cap", edge, transform(-1.75f, 1.65f, 0f, 0.36f, 0.20f, 0.36f)).cone(1f, 1f, 1f, 4);
        part(builder, "post_r_cap", edge, transform(1.75f, 1.65f, 0f, 0.36f, 0.20f, 0.36f)).cone(1f, 1f, 1f, 4);
        part(builder, "rail_low", wood, transform(0f, 0.60f, 0f, 3.55f, 0.20f, 0.20f)).box(1f, 1f, 1f);
        part(builder, "rail_high", edge, transform(0f, 1.18f, 0f, 3.55f, 0.20f, 0.20f)).box(1f, 1f, 1f);
        return builder.end();
    }

    private static Model createWater() {
        ModelBuilder builder = new ModelBuilder();
        Material water = new Material(
            ColorAttribute.createDiffuse(new Color(0.16f, 0.48f, 0.69f, 0.70f)),
            ColorAttribute.createSpecular(new Color(0.72f, 0.86f, 0.94f, 1f)),
            FloatAttribute.createShininess(48f),
            new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.70f)
        );
        return builder.createCylinder(Terrain.LAKE_RADIUS * 2f, 0.08f, Terrain.LAKE_RADIUS * 2f, 64, water, ATTRS);
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
