# Package: `worldgen` — structures, wild crops, greenhouse detection

Module class: `com.craftmorkovsk.worldgen.WorldgenModule` with `public static void init()`.
In `init()` you MUST call `FarmingHooks.registerGreenhouse(yourChecker)` — implement
`GreenhouseChecker` as `boolean isGreenhouse(LevelReader level, BlockPos pos)`:
crop counts as greenhouse-grown if, within the 6 blocks above it, there is glass
(any `block instanceof AbstractGlassBlock` or `BlockTags.IMPERMEABLE`? use
`state.is(Tags.Blocks.GLASS)` Forge tag `forge:glass`? use vanilla `minecraft:glass`+
`glass_pane`+`minecraft:stained_glass` via `Tags.Blocks.GLASS`/GLASS_PANES — check
what exists: `net.minecraftforge.common.Tags.Blocks.GLASS` exists) forming a roof —
definition: at least 3 glass blocks in the 3×3×6 volume above the crop. Cache nothing
(called per randomTick — it's a small bounded scan, fine).

## Wild crops

- `wild_crop` — a single-block decorative plant (like vanilla short grass style cross
  render, noCollission, instabreak) that drops 1-2 random common seeds (pick from
  `CropCatalog` BY_ID excluding secret/rares: morkov/carrot_red/potato/onion/lettuce/
  wheat_spelt/sunflower…) — implement `WildCropBlock.getDrops` with the seed pick.
- Placement: custom `Feature` — `WildCropFeature extends Feature<NoneFeatureConfiguration>`
  registered via `MorkovskRegistries.FEATURES`; scatter ~8 patches per chunk cluster
  (use `PlacementUtils`/ConfiguredFeature JSON + PlacedFeature JSON in
  `data/craftmorkovsk/worldgen/configured_feature/` + `placed_feature/` +
  `forge/biome_modifier/add_wild_crops.json` (ForgeBiomeModifiers.AddFeatures —
  plains/forest/meadow via `minecraft:is_overworld` or `forge:` biome tags — check
  tag names: use `#minecraft:is_overworld`? biome_modifier biomes field accepts a
  tag `#forge:is_overworld`... use `"biomes": "#minecraft:is_overworld"`? Minecraft has
  no `is_overworld` biome tag — Forge does: `forge:is_overworld`? There is
  `forge:is_overworld`? Forge adds `forge:is_overworld`? To be safe use
  `minecraft:is_forest`, `minecraft:is_plains`? is_plains isn't a tag either — forest
  has `minecraft:is_forest`, plains has no tag… use `"biomes": ["minecraft:plains",
  "minecraft:forest", "minecraft:meadow", "minecraft:sunflower_plains"]` explicit list —
  SIMPLEST and correct).

## Structures (NBT-free programmatic structures via features is NOT authentic enough —
use proper `structure`/`structure_set` JSON with `minecraft:jigsaw` pools or simplest
`legacy_single_pool_element`?). Choose the pragmatic path:

- Recommended: **template-pool jigsaw structures** in data JSON:
  `data/craftmorkovsk/worldgen/structure/{farmstead,greenhouse,windmill}.json`
  (type `minecraft:jigsaw`, start_pool + size 1-2, step SURFACE_STRUCTURES,
  terrain_adaptation beard_thin), `structure_set` with random spread, and
  `template_pool` JSONs whose elements are `minecraft:single_pool_element`
  pointing at NBT files... NBT structure files are binary — you can't hand-write
  them. So instead: use `processors`/`single_pool_element` with
  `element_type: legacy_single_pool_element`? still NBT.
  → FINAL DECISION: build structures **programmatically** — a
  `SimpleStructureFeature extends Feature<NoneFeatureConfiguration>` that places a
  small building via setBlock calls (farmstead: 7×5 wood+stone hut with door/farm
  plots containing planted morkov crops on rich_farmland; greenhouse: 5×5 glass house
  with crops inside; windmill-ish barn tower). Register 3 `Feature`s +
  configured/placed feature JSONs + `forge/biome_modifier` additions with
  explicit biome lists (plains, meadow, sunflower_plains — sparse, like
  `RarityFilter.onAverageOnceEvery(30)`). This is fully data-driven and reliable.
- Structures may include a `shop_chest`? no — include loot barrel with seeds:
  place `minecraft:barrel` and fill? Can't fill via setBlock — place a
  `RandomizableContainer`? simplest: seed loot barrels are skippable — place
  planted crops + a composter + hay bales instead.

## Bonus

- `fertile_land` wild variant? skip.
- Advancement `green_thumb` — first crop that grows inside a detected greenhouse:
  can't easily hook "grew inside" — grant when `randomTick` greenhouse branch fires
  for a MorkovskCropBlock? Crop block can't award (no player) — instead grant when
  player harvests a crop that `FarmingHooks.isInGreenhouse` at break time: hook
  `BlockEvent.BreakEvent` in YOUR subscriber (check block instanceof
  MorkovskCropBlock && isMaxAge && isInGreenhouse → Award.grant(player,"green_thumb")).
- Advancement `wild_harvest` — breaking wild_crop.
- Biome_modifier JSON syntax for Forge 1.20.1:
  `data/craftmorkovsk/forge/biome_modifier/<name>.json`:
  `{"type":"forge:add_features","biomes":[...],"features":"craftmorkovsk:<placed>",
  "step":"vegetal_decoration"}` — features may be a string or list.
- Lang entries + textures for wild_crop (`crop` pattern stage ~5).
