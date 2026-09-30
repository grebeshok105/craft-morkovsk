# Package: `irrigation` — watering infrastructure

Module class: `com.craftmorkovsk.irrigation.IrrigationModule` with `public static void init()`.
Inside `init()` you MUST call `FarmingHooks.registerIrrigation(yourChecker)` so
`SoilAPI.isHydrated`/`MorkovskSoilBlock` see sprinkler water. Suggested impl:
a `WaterGrid` block-entity tick that marks irrigated positions, backed by
`FarmlandWaterManager` (chunk-section ticket map or per-level LongOpenHashSet of soil
positions, pruned on block change/unload — keep it simple and correct; a
`ServerLevel`-keyed map of `Set<BlockPos>` is fine).

## Content (all in package `com.craftmorkovsk.irrigation`)

- `water_pipe` — block, water-loggable, connects like CableBlock (NSEWUD BooleanProperties).
  Carries water units between pump → sprinkler/tank. Pipe network BFS similar to
  `energy/EnergyNetwork` (write your own `WaterNetwork`).
- `water_channel` — decorative+functional channel block (like a half-height water trough):
  irrigates farmland directly adjacent (4-neighbors) when it holds water; fills from
  adjacent pipes/tanks or water source blocks.
- `water_tank` — block entity storing up to 8000 "water units" (1 unit ≈ 100 mB conceptually;
  you may implement integer units, no Fluid API required — but you MAY use
  `net.minecraftforge.fluids` if simpler). Filled by pump or rain (raining + sky access
  slowly fills). Right-click with bucket adds/removes.
- `water_pump` — block entity; each tick, if adjacent to a water source block or a tank
  with water, pushes water into connected pipe network toward sprinklers/channels/tanks.
  Costs a little FE (pull via `EnergyNetwork.pull` — machines/energy framework exists).
- `sprinkler` — consumes water from the pipe network; every ~40 ticks hydrates farmland in
  radius `MorkovskConfig.IRRIGATION_SPRINKLER_RANGE` (affects `SoilAPI` hydration via your
  registered hook AND `FarmBlock.MOISTURE=7` for vanilla farmland). Green particle splash
  (use `net.minecraft.core.particles.ParticleTypes.SPLASH` server-side spawn).
- `advanced_sprinkler` — same but radius `MorkovskConfig.IRRIGATION_ADVANCED_SPRINKLER_RANGE`,
  also applies a small growth tick boost (call `block.randomTick` occasionally or just rely
  on hydration — hydration already boosts growth; keep it simple: larger radius + occasional
  bonemeal-like +1 age attempt with very low chance is optional, not required).
- `irrigation_valve` — right-click toggles open/closed; when closed it breaks water flow
  through that pipe position (WaterNetwork treats it as cut).

## Data assets

- Blockstates/models/textures via `tools/gen_tex.py` (`machine`, `cable`, `soil` patterns).
- Loot tables (drop self) for every block; crafting recipes (pipes: iron+glass; tank:
  iron+barrel; pump: iron+redstone+copper; sprinklers: iron+pipe; valve: iron+lever).
- Lang fragment `lang_parts/irrigation.json`; run `tools/merge_lang.py`.
- Optional: `craftmorkovsk:irrigation` advancement (criterion "grant") — grant when a
  sprinkler first irrigates soil (hook a Forge event or check inside tick → `Award.grant`).

Menu/screen only if a block genuinely needs it (tank can just show chat charge like
BatteryBlock). Keep block entities ticking efficiently (no per-tick heavy scans; cache).
