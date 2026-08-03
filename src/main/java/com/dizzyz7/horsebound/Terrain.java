// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

final class Terrain implements Disposable {
    static final float WORLD_HALF_SIZE = 120f;
    static final float LAKE_X = -28f;
    static final float LAKE_Z = 18f;
    static final float LAKE_RADIUS = 17f;
    static final float WATER_LEVEL = 0.35f;

    private static final Color LOWLAND = new Color(0.20f, 0.43f, 0.20f, 1f);
    private static final Color MEADOW = new Color(0.34f, 0.58f, 0.27f, 1f);
    private static final Color HIGHLAND = new Color(0.43f, 0.55f, 0.29f, 1f);
    private static final Color SHORE = new Color(0.52f, 0.48f, 0.29f, 1f);
    private static final Color LAKE_BED = new Color(0.16f, 0.28f, 0.20f, 1f);

    final Model model;
    final ModelInstance instance;

    Terrain() {
        model = buildTerrain(WORLD_HALF_SIZE * 2f, 3f);
        instance = new ModelInstance(model);
    }

    static float heightAt(float x, float z) {
        float rolling = MathUtils.sin(x * 0.043f) * 1.65f
            + MathUtils.cos(z * 0.052f) * 1.25f
            + MathUtils.sin((x + z) * 0.021f) * 1.15f
            + MathUtils.cos((x - z) * 0.016f) * 0.75f;
        float macro = MathUtils.sin(x * 0.009f) * MathUtils.cos(z * 0.011f) * 3.1f;
        float height = 2.2f + rolling + macro;

        float dx = x - LAKE_X;
        float dz = z - LAKE_Z;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if (distance < LAKE_RADIUS + 7f) {
            float factor = 1f - MathUtils.clamp(distance / (LAKE_RADIUS + 7f), 0f, 1f);
            height -= factor * 6.2f;
        }
        return Math.max(-2.2f, height);
    }

    static boolean isInsideLake(float x, float z) {
        float dx = x - LAKE_X;
        float dz = z - LAKE_Z;
        return dx * dx + dz * dz < (LAKE_RADIUS - 1.5f) * (LAKE_RADIUS - 1.5f);
    }

    static Vector3 normalAt(float x, float z, Vector3 out) {
        float sample = 0.5f;
        float left = heightAt(x - sample, z);
        float right = heightAt(x + sample, z);
        float back = heightAt(x, z - sample);
        float front = heightAt(x, z + sample);
        return out.set(left - right, sample * 2f, back - front).nor();
    }

    private static Model buildTerrain(float size, float step) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        Material grass = new Material(ColorAttribute.createDiffuse(Color.WHITE));
        MeshPartBuilder mesh = builder.part(
            "rolling_meadow",
            GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position
                | VertexAttributes.Usage.Normal
                | VertexAttributes.Usage.ColorUnpacked,
            grass
        );

        float min = -size * 0.5f;
        float max = size * 0.5f;
        Vector3 normal = new Vector3();
        Color color = new Color();

        for (float x = min; x < max; x += step) {
            for (float z = min; z < max; z += step) {
                float x1 = Math.min(x + step, max);
                float z1 = Math.min(z + step, max);

                MeshPartBuilder.VertexInfo v00 = vertex(x, z, normal, color);
                MeshPartBuilder.VertexInfo v10 = vertex(x1, z, normal, color);
                MeshPartBuilder.VertexInfo v11 = vertex(x1, z1, normal, color);
                MeshPartBuilder.VertexInfo v01 = vertex(x, z1, normal, color);

                short i00 = mesh.vertex(v00);
                short i10 = mesh.vertex(v10);
                short i11 = mesh.vertex(v11);
                short i01 = mesh.vertex(v01);

                mesh.triangle(i00, i11, i10);
                mesh.triangle(i00, i01, i11);
            }
        }
        return builder.end();
    }

    private static MeshPartBuilder.VertexInfo vertex(
        float x,
        float z,
        Vector3 normalScratch,
        Color colorScratch
    ) {
        Vector3 normal = normalAt(x, z, normalScratch);
        Color color = colorAt(x, z, normal, colorScratch);
        return new MeshPartBuilder.VertexInfo()
            .setPos(x, heightAt(x, z), z)
            .setNor(normal.x, normal.y, normal.z)
            .setCol(color);
    }

    private static Color colorAt(float x, float z, Vector3 normal, Color out) {
        float height = heightAt(x, z);
        float highland = MathUtils.clamp((height - 2f) / 6.5f, 0f, 1f);
        float variation = 0.5f + 0.5f * MathUtils.sin(x * 0.13f + MathUtils.cos(z * 0.11f) * 2.1f);
        out.set(LOWLAND).lerp(MEADOW, 0.48f + variation * 0.30f).lerp(HIGHLAND, highland * 0.58f);

        float dx = x - LAKE_X;
        float dz = z - LAKE_Z;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if (distance < LAKE_RADIUS + 5.5f) {
            float shoreBand = 1f - MathUtils.clamp(
                Math.abs(distance - (LAKE_RADIUS + 0.5f)) / 5.5f,
                0f,
                1f
            );
            out.lerp(SHORE, shoreBand * 0.74f);
        }
        if (distance < LAKE_RADIUS) {
            float bed = 1f - MathUtils.clamp(distance / LAKE_RADIUS, 0f, 1f);
            out.lerp(LAKE_BED, 0.72f + bed * 0.20f);
        }

        float slopeShade = MathUtils.clamp((1f - normal.y) * 0.85f, 0f, 0.24f);
        out.mul(1f - slopeShade, 1f - slopeShade * 0.72f, 1f - slopeShade, 1f);
        return out;
    }

    @Override
    public void dispose() {
        model.dispose();
    }
}
