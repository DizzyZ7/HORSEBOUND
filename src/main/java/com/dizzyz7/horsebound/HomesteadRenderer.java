// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class HomesteadRenderer implements Disposable {
    private final HomesteadModels models = new HomesteadModels();
    private final ModelBatch modelBatch = new ModelBatch();
    private final Environment environment = new Environment();
    private final Map<UUID, ModelInstance> instances = new HashMap<>();
    private ModelInstance preview;
    private HomesteadStructureType previewType;
    private boolean previewValidity;

    HomesteadRenderer() {
        environment.set(ColorAttribute.createAmbientLight(0.62f, 0.64f, 0.58f, 1f));
        environment.add(new DirectionalLight().set(1f, 0.96f, 0.84f, -0.45f, -0.85f, -0.25f));
    }

    void render(
        PerspectiveCamera camera,
        HomesteadState state,
        Set<UUID> hiddenStructureIds,
        PlacementPreview placement
    ) {
        sync(state, hiddenStructureIds == null ? Set.of() : hiddenStructureIds);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        modelBatch.begin(camera);
        for (ModelInstance instance : instances.values()) modelBatch.render(instance, environment);
        if (placement != null && placement.visible()) {
            syncPreview(placement);
            modelBatch.render(preview, environment);
        }
        modelBatch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void sync(HomesteadState state, Set<UUID> hiddenStructureIds) {
        Set<UUID> alive = new HashSet<>();
        for (PlacedStructure structure : state.structures()) {
            if (hiddenStructureIds.contains(structure.id())) continue;
            alive.add(structure.id());
            ModelInstance instance = instances.computeIfAbsent(
                structure.id(),
                ignored -> new ModelInstance(models.normal(structure.type()))
            );
            float visualHeading = structure.heading();
            if (structure.type() == HomesteadStructureType.GATE && structure.isOpen()) visualHeading += 90f;
            instance.transform.idt()
                .translate(structure.x(), Terrain.heightAt(structure.x(), structure.z()), structure.z())
                .rotate(Vector3.Y, visualHeading);
        }
        instances.keySet().removeIf(id -> !alive.contains(id));
    }

    private void syncPreview(PlacementPreview placement) {
        if (preview == null || previewType != placement.type() || previewValidity != placement.valid()) {
            previewType = placement.type();
            previewValidity = placement.valid();
            preview = new ModelInstance(models.preview(previewType, previewValidity));
        }
        preview.transform.idt()
            .translate(placement.x(), placement.y() + 0.04f, placement.z())
            .rotate(Vector3.Y, placement.heading());
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        models.dispose();
        instances.clear();
        preview = null;
    }

    record PlacementPreview(
        boolean visible,
        HomesteadStructureType type,
        float x,
        float y,
        float z,
        float heading,
        boolean valid,
        String reason
    ) {
        PlacementPreview {
            reason = reason == null ? "" : reason;
        }
    }
}
