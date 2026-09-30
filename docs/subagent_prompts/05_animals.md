# Package: `animal` — farm animals: duck, turkey, goat + feeding troughs

Module class: `com.craftmorkovsk.animal.AnimalModule` with `public static void init()`.

## Entities (real, breedable, productive)

All three `extends Animal` (`net.minecraft.world.entity.animal.Animal`) with sane
attributes (health, speed), `TemptGoal` on their feed item, `BreedGoal`, `FollowParent`,
`WaterAvoidingRandomStrollGoal`, `LookAtPlayer`/`RandomLookAround`:

- `duck` — small (bb ~0.6×0.7), `SoundEvents.CHICKEN_AMBIENT` custom quack?
  Register your own sound events? Keep vanilla sounds (DUCK doesn't exist — use
  CHICKEN sounds). Feed: any `*_seeds` tag `craftmorkovsk:duck_food`.
  Production: lays `craftmorkovsk:duck_egg` on a timer (like chicken — see
  `Chicken.aiStep` egg timer pattern, every ~6000 ticks when fed & adult).
  `duck_egg` item is throwable like vanilla egg (`ThrownEgg` subclass `ThrownDuckEgg`
  hatching a duck on break) — implement the throwable.
- `turkey` — slightly bigger chicken analog. Feed: seeds tag `craftmorkovsk:turkey_food`.
  Lays `craftmorkovsk:turkey_egg` similarly (thrown → turkey chick).
- `goat` — `craftmorkovsk:goat` (name it `farm_goat` internally if `minecraft:goat`
  collision confusion — use entity id `farm_goat`). Feed: wheat/morkov.
  Production: right-click with empty bucket → `craftmorkovsk:goat_milk_bucket`
  (cooldown ~2400 ticks; reuse vanilla `Cow.getCowType`/`mobInteract` bucket pattern).
  `goat_milk_bucket` is drinkable (UseAnim.DRINK, clears effects like milk? give
  small nutrition via `Item.finishUsingItem`).
- All three periodically drop `craftmorkovsk:manure` item (every ~6000 ticks when
  adult+alive — like egg timer) — manure is the `manure_fertilizer` ingredient source
  (recipe: manure + compost? manure itself already exists as `manure_fertilizer` item —
  register `manure` as a NEW item and add a shapeless recipe manure→manure_fertilizer×2,
  or simply drop `manure_fertilizer` directly — decide, keep it consistent).
- Breeding: use `feeditems` above; babies via super breed logic.
- Egg/manure production + eating from trough:
  `feeding_trough` — block + block entity with 9-slot inventory accepting the food
  tags; nearby animals (AABB ~6 blocks) get "fed" (reset their production timer faster /
  let them eat: reduce inventory, boost production rate ×2 and enable breeding mode).
  Implement `TroughEatGoal` (animals pathfind to trough when hungry) — optional but
  the trough MUST mechanically work: simplest reliable impl — trough BE tick scans
  nearby animals of its species, if food present and animal timer cooldown high,
  consume 1 item and mark animal (set a "fedUntil" long on the animal via a simple
  int NBT or a capability-less transient field).

## Renderer+models

`EntityModel` subclasses — keep boxes simple (duck: body+head+beak; turkey: bigger
+tail fan; goat: body+head+horns+legs). Register `ModelLayerLocation`s in your client
subscriber (`EntityRenderersEvent.RegisterLayerDefinitions` + `RegisterRenderers`).
Use vanilla-part-based simple models; `MobModel`/`QuadrupedModel` subclass approach is
fine (1.20.1 `QuadrupedModel` exists — goat can extend it; birds extend `EntityModel`).

## Assets/data

- Spawn eggs `ForgeSpawnEggItem` for each (tab MISC).
- Item textures (eggs, milk bucket, manure), trough block texture/model/loot/recipe.
- Tags: `craftmorkovsk:duck_food`, `turkey_food`, `goat_food` (items), and
  `craftmorkovsk:farm_animals` (entity types — for trough scan or tools).
- `data/craftmorkovsk/entity_types`? entity tag path: `data/craftmorkovsk/tags/entity_types/farm_animals.json`.
- Breeding production awards XP: hook production tick → `FarmingStats.awardXp(
  nearestPlayerOrFeeder, XpReason.BREEDING, 1)` — if no player context, skip XP
  (only when a player milks/collects? milk interact → BREEDING xp +1).
- Advancement `animal_keeper` — first successful animal product collection.
- `natural spawning`: use `ForgeBiomeModifiers.AddSpawnsBiomeModifier` JSON
  (`data/craftmorkovsk/forge/biome_modifier/*.json`) — plains+meadow biomes
  (`minecraft:is_overworld` or `forge:is_plains` tag) so animals appear in world.
  Spawn placement: `SpawnPlacements` register in `FMLCommonSetupEvent` enqueueWork
  with `SpawnPlacements.Type.ON_GROUND` + `Heightmap.Types.MOTION_BLOCKING_NO_LEAVES`
  + `Animal::checkAnimalSpawnRules` for each type.
