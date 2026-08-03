# HORSEBOUND — Production Visual Asset Roadmap

Created and directed by **Dimash Janibekov (DizZyZ7)**.

## Current state

0.5.7 improves the procedural prototype, but the runtime still builds most visible objects from boxes, spheres, cylinders and cones. This is useful for gameplay validation and collision work, but it cannot reach the intended commercial presentation by incremental primitive polishing alone.

## Target art direction

HORSEBOUND should move toward a warm stylized-realistic ranch world:

- readable silhouettes at third-person distance;
- natural but not photorealistic proportions;
- soft shapes and restrained color palettes;
- detailed horses as the visual priority;
- cozy environmental storytelling;
- clear gameplay readability in daylight, dusk and night;
- scalable quality for ordinary Windows PCs and Steam Deck-class hardware.

## Phase 1 — Asset loading boundary

Introduce an external asset catalog with procedural fallback:

```text
RanchAssetCatalog
├── player.glb
├── pushik.glb
├── horses/
├── vegetation/
├── structures/
└── fallback procedural models
```

Requirements:

- asynchronous AssetManager loading;
- explicit ownership and disposal;
- missing-asset fallback rather than startup failure;
- deterministic collision metadata separate from render meshes;
- package assertions for required production assets.

## Phase 2 — Hero characters

Priority order:

1. Base horse model.
2. Horse coat/material variants.
3. Player character.
4. Pushik.

Horse animation set:

- idle variants;
- walk;
- trot;
- canter;
- gallop;
- turn in place;
- jump takeoff, airborne and landing;
- graze;
- drink;
- sleep/rest;
- fear/startle;
- bonding interaction.

Player animation set:

- idle;
- walk/run;
- jump;
- gather;
- pet/feed;
- mount/dismount;
- riding poses.

Pushik animation set:

- idle breathing;
- walk/trot;
- sit;
- sleep;
- greet;
- tail and ear motion.

## Phase 3 — Rendering foundation

Add in this order:

1. Directional shadow map with quality tiers.
2. Contact shadows or blob-shadow fallback.
3. Distance fog and atmospheric color.
4. Sky dome or procedural sky gradient.
5. Tone mapping and gamma-correct output.
6. Dedicated water shader with depth color, shoreline fade and restrained reflection.
7. Wind response for grass and tree crowns.

Each feature must have Low / Medium / High behavior and a deterministic fallback.

## Phase 4 — Environment kit

Create modular authored assets:

- grass and meadow ground materials;
- multiple tree species and age variants;
- rocks and terrain scatter;
- flowers, shrubs and reeds;
- paths, mud and shoreline decals;
- fences, gates, troughs, feeders and stable modules;
- ranch clutter: buckets, tools, hay bales, sacks and lanterns.

Use GPU instancing or batched rendering for repeated vegetation. Do not create one high-overhead scene object per blade of grass.

## Phase 5 — Camera and presentation

- animation-aware camera target heights;
- shoulder and riding camera presets;
- camera collision that respects authored bounds;
- controlled depth of field only for photo mode or cinematics;
- photo mode after the core render path is stable.

## Asset technical contract

Preferred exchange format: **glTF 2.0 binary (`.glb`)**.

Guidelines:

- Y-up;
- meters as world units;
- consistent forward-axis convention;
- named animation clips;
- separate render and collision meshes;
- PBR metallic-roughness materials;
- 2K textures for hero assets, lower for repeated environment assets;
- texture atlases where batching benefits;
- no hidden duplicate geometry;
- controlled bone and material counts;
- LODs for vegetation and large structures.

## Definition of visual-production readiness

The game is not visually production-ready until:

- the horse is fully authored and animated;
- primitive character models are no longer visible in normal gameplay;
- shadows and atmosphere provide stable depth;
- water and shoreline are coherent;
- ranch structures share one art language;
- frame time remains stable at target quality presets;
- visuals are tested on multiple GPUs and at 1280×800.
