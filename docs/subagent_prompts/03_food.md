# Package: `food` — ~20 food products + cooking recipes

Module class: `com.craftmorkovsk.food.FoodModule` with `public static void init()`.

## Food items (all in your package; FoodProperties via a small builder helper)

Register plain `Item`/`QualityProduceItem`? Food products are NOT quality-tracked —
use a simple `FoodItem extends Item` with food props. At least 20 distinct items:

- Baked: `flour` is registered by the machine package as `craftmorkovsk:flour` —
  use it as ingredient only (do NOT re-register it). You register:
  `bread_morkovsk`, `carrot_cake`, `pumpkin_pie_morkovsk`, `corn_bread`.
- Machine-made items YOU OWN (register them + write their `craftmorkovsk:processing`
  recipe JSONs — the machines themselves belong to the machine package, ids:
  `mill`, `oil_press`, `separator`, `dryer`, `fermenter`): `raisins`,
  `dried_tomato`, `dried_herb`, `cream`, `butter`, `pickles`, `sauerkraut`,
  `grape_juice`, `berry_jam`. On your isolated branch those recipes reference
  machine ids that don't exist yet — recipe parse just won't match at runtime;
  that's expected, not an error.
- Meals: `vegetable_soup`, `morkov_stew`, `stuffed_peppers`, `cabbage_rolls`,
  `farmer_salad`, `grilled_corn`, `baked_potato_dish`, `tomato_sauce`, `ratatouille`.
- Preserves/sweets: `berry_jam` (strawberry/blueberry→jam), `carrot_jam`? keep:
  `berry_jam`, `pickles`, `sauerkraut`, `dried_herb` (generic), `raisins`,
  `dried_tomato`, `grape_juice`, `morkov_juice`, `butter`, `cream`, `goat_cheese`
  (uses goat milk bucket from animal package if present — else craft from milk_bucket
  + a bit of compost? no — use milk_bucket).
- Silly/legendary: `supreme_salad` (from morkovsk_supreme, huge stats), 
  `carrot_of_power` (from the_carrot: absorption+regen potion effects via
  `FoodProperties.Builder.effect(...)`).

Effects: modest; `carrot_of_power` may be flashy (regen II 10s, absorption 30s).
Give each food sane nutrition/saturation roughly proportional to ingredients.

## Recipes

- Vanilla `crafting_shaped`/`crafting_shapeless` JSONs + `minecraft:smoking`/`smelting`
  for cooked items (`grilled_corn` smoking corn, etc.).
- `craftmorkovsk:processing` JSONs for machine-made foods (mill flour, oil_press oil,
  dryer raisins/dried_tomato/dried_herb, fermenter pickles/sauerkraut) —
  `machine` ids: mill, oil_press, separator, dryer, fermenter. (Machine pkg owns the
  blocks; you own these recipe files — they live under data/craftmorkovsk/recipes/
  named `food_*` to avoid collisions.)
- Every item: `item/generated` model + `tools/gen_tex.py` `produce`/`item` texture,
  lang entries in `lang_parts/food.json`, `MorkovskTabs.add(ModTab.FOOD, ...)`.

## Integration

- Eating a Craft Morkovsk food awards tiny XP: subscribe (your own
  `@Mod.EventBusSubscriber(Bus.FORGE)` in-package) to `LivingEntityUseItemEvent.Finish`
  → if stack item is yours and entity is ServerPlayer → `FarmingStats.awardXp(
  player, XpReason.PROCESSING, 1)`? PROCESSING is closest fit — fine.
- Advancement `home_cook` (craft any 3 distinct mod foods — simplest: grant on
  `ItemCraftedEvent` when result is your item). JSON criterion "grant".
