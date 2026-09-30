# Package: `machine` — the powered processing fleet

Module class: `com.craftmorkovsk.machine.MachineModule` with `public static void init()`.
Use `machine/framework/*` (already on base branch): `MachineBlockEntity`,
`AbstractMachineBlock`, `MachineMenu`, `MachineScreen`, `ProcessingRecipe`
(JSON type `craftmorkovsk:processing`), `ProcessingMachineBlockEntity`,
`FuelMachineMenu`/`FuelScreen` as a reference implementation.

Register each `MenuType` via `IForgeMenuType.create(...)` buf-ctor like `EnergyModule`
does, and register screens in your own client event subscriber (see SHARED.md rule 4).

## Required: 9 powered machines (FE consumers, real inventories/menus/progress)

1. `mill` — grain (wheat_spelt, wheat_rye, corn → flour). ProcessingMachineBlockEntity +
   `craftmorkovsk:processing` JSONs (`machine: "mill"`). Output item `craftmorkovsk:flour`
   — YOU register it (non-food ingredient item, tab FOOD).
2. `oil_press` — sunflower/produce → `craftmorkovsk:plant_oil` — YOU register it.
   ProcessingRecipe.
3. `separator` — `minecraft:milk_bucket` → `craftmorkovsk:cream` + `craftmorkovsk:butter`.
   IMPORTANT: `cream`/`butter` are registered by the FOOD package — do NOT register them;
   just output their ids in recipes. On your isolated branch these recipe JSONs will warn
   "unknown item" — expected, ignore it. ProcessingRecipe is 1-in/1-out: write a small
   custom BlockEntity extending MachineBlockEntity with 2 output slots (input bucket →
   returns empty bucket too — handle the container item), or alternate cream/butter —
   document the choice.
4. `dryer` — produce → dried versions. Machine block is yours; the OUTPUT items
   (`raisins`, `dried_tomato`, `dried_herb`) are registered by the FOOD package — do not
   register them and do not write the recipes (food package writes them).
5. `fermenter` — same split: you own the machine block only; outputs (`pickles`,
   `sauerkraut`, `grape_juice`) and recipes belong to the food package.
6. `seed_extractor` — produce → 2× that crop's seeds. Needs per-crop mapping:
   `CropCatalog.BY_ID` is public; map input produce item → `def.seeds` (a custom BE;
   recipe JSON not expressive enough — a custom `SeedExtractorBlockEntity` looking up
   `CropCatalog` is the right call).
7. `auto_harvester` — tick: scans a small radius (5×5 flat, configurable constant) for
   mature `MorkovskCropBlock`; occasionally (every ~200 ticks, FE cost) breaks one crop
   as if harvested: use `state.getDrops(...)`/drop into internal 9-slot buffer, apply
   `SoilAPI.degrade`, replant seeds automatically (set crop age 0 if seeds drop≥1).
   Menu shows buffer inventory.
8. `fertilizer_mixer` — compost + mineral_fertilizer → `craftmorkovsk:morkovsk_fertilizer`;
   manure_fertilizer + compost → compost ×2. ProcessingRecipe with tag/ingredient inputs.
9. `packing_station` — 9× same produce → `craftmorkovsk:produce_crate` — YOU register the
   crate item (generic item with `"Packed"` String tag + count int storing packed produce
   id+count+quality; shows name via hoverText). Custom BE.

Machines consume `MorkovskConfig.MACHINE_ENERGY_PER_TICK` while working (framework reads
`getEnergyPerTick()`). Machines pull FE from adjacent energy-capable BEs/cables each tick
(`EnergyNetwork.pull` or `ForgeCapabilities.ENERGY` on neighbors — EnergyNetwork handles
cable traversal; just also check direct neighbors without cable? EnergyNetwork.collect
already includes direct-adjacent endpoints — use it).

## Data assets

- All blocks: FACING horizontal facing handled by AbstractMachineBlock; blockstate needs
  `facing` variants + `working` — write a blockstate JSON with `facing=north/east/...`
  variants all pointing at one model (or add `_on` lit variant texture for polish).
- `tools/gen_tex.py` `machine` pattern per block (two-tone colors fitting agri palette).
- Menus: subclasses of MachineMenu adding slots; screens: MachineScreen subclasses
  (optional `drawExtras` labels). Client screens registered in YOUR client subscriber.
- Recipes for each machine block (iron + redstone + wood/copper themed).
- Loot tables drop-self; `mineable/pickaxe` tag; lang_parts/machine.json.
- Processing recipe JSONs under `data/craftmorkovsk/recipes/` (ids like `mill_flour_spelt`).
- Advancement: `machine_operator` (first machine craft or first processed item) — grant via
  Forge event (e.g. ItemCraftedEvent for any of your machine block items).
