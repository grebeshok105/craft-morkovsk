# Craft Morkovsk — sub-session contract (READ FULLY BEFORE CODING)

You are one of several parallel sessions implementing a large Minecraft Forge mod.
This file is the contract. Your package file (named in your prompt) is your spec.

## Environment

- Repo: `grebeshok105/craft-morkovsk`. Fresh clone each session:
  `git clone https://github.com/grebeshok105/craft-morkovsk.git ~/repos/craft-morkovsk`
- Base branch (the skeleton — everything below already exists there):
  `cd ~/repos/craft-morkovsk && git fetch origin devin/morkovsk-base && git checkout -b devin/morkovsk-<YOURPKG> origin/devin/morkovsk-base`
- Java 17: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64`. If that path does not
  exist, pick any JDK 17 under `/usr/lib/jvm`.
- Maven Central is rate-limited on this network. Before the first build:
  `mkdir -p ~/.gradle/init.d && cp tools/central-mirror.gradle ~/.gradle/init.d/`
- Build: `./gradlew build -x test --no-daemon`. First build takes a few minutes
  (ForgeGradle fetches and decompiles Minecraft — be patient, do not kill it).
- Forge 1.20.1 (`1.20.1-47.4.23`), official Mojang mappings, Java 17, mod id `craftmorkovsk`,
  root package `com.craftmorkovsk`.

## Hard rules

1. **Package ownership.** You may only create/edit files under
   `src/main/java/com/craftmorkovsk/<yourpkg>/**`, plus assets/data files under
   `src/main/resources/{assets,data}/craftmorkovsk/**` that are clearly yours
   (names prefixed with your ids), and exactly ONE lang fragment:
   `assets/craftmorkovsk/lang_parts/<yourpkg>.json`.
2. **Do NOT edit shared files**: `build.gradle`, `settings.gradle`, `gradle.properties`,
   `CraftMorkovsk.java`, `MorkovskModules.java`, `MorkovskClientInit.java`,
   `MorkovskRegistries.java`, `MorkovskNet.java`, `MorkovskConfig.java`,
   `tools/*.py`, files in other packages, or other `lang_parts/*.json`.
   The orchestrator adds one wiring line (`<YourPkg>Module.init()`) after merge.
3. Entry point: `com.craftmorkovsk.<yourpkg>.<YourPkg>Module` with
   `public static void init()`. Register everything inside `init()` — it runs during
   mod construction, before RegisterEvent. Use `MorkovskRegistries.BLOCKS/ITEMS/...`
   (DeferredRegister hub). Add items to creative tabs via `MorkovskTabs.add(ModTab.X, item::get)`.
4. Client-only wiring (menu screens, entity renderers, particle providers, key bindings,
   overlay renderers): create your OWN `@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID,
   bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)` class inside your package —
   never touch `MorkovskClientInit`. Guard `@OnlyIn(Dist.CLIENT)` classes so the dedicated
   server jar never loads them.
5. Networking: `MorkovskNet.registerS2C(type, handler, encoder, decoder)` /
   `registerC2S(...)` inside your `Module.init()`. Handlers run on the network thread —
   wrap work in `ctx.get().enqueueWork(...)`, and `ctx.get().setPacketHandled(true)`.
   Do NOT change registration order expectations: register in your init, nothing else.
6. Server-authoritative: all gameplay state lives server-side (block entities /
   capabilities / menus sync). No client-only state driving gameplay. Dedicated-server
   safe: no client classes referenced from common code paths (use DistExecutor or
   separate client classes; check existing `SyncFarmingPacket`/`ClientFarmingData` for
   the pattern).
7. **Real implementations, no decorative stubs**: blocks/machines need working logic,
   inventories (ItemStackHandler), menus + screens where interactive, recipes/loot/lang.
   This is the user's explicit quality bar.
8. No purple/black missing textures and no placeholder art: generate 16x16 textures with
   `tools/gen_tex.py` (`python3 tools/gen_tex.py <out.png> <pattern> <colors...>` —
   patterns: soil, produce, seeds, crop, machine, item, cable; see `tools/texlib.py`),
   or write JSON assets by hand / with a small script you keep under `tools/` is FORBIDDEN
   (tools/ is shared) — put throwaway scripts outside the repo or inline in your workflow,
   not committed. Actually: you may generate assets via a python script run locally but do
   not commit new files into tools/. Commit the *generated* pngs/jsons.
9. Lang: add ALL display strings to `assets/craftmorkovsk/lang_parts/<yourpkg>.json`,
   then run `python3 tools/merge_lang.py` (it regenerates `en_us.json`). Commit both.
10. Machine framework: powered machines extend `machine/framework/*` —
    `MachineBlockEntity` (energy + progress + ContainerData), `AbstractMachineBlock`
    (FACING + WORKING props, opens GUI), `MachineMenu`/`MachineScreen` (agri palette,
    energy bar + progress arrow), `ProcessingRecipe` (custom JSON recipe type
    `craftmorkovsk:processing`: `{type, machine, input:{ingredient}, output:{item,count},
    time, energy}` — `machine` field is your machine id string) and
    `ProcessingMachineBlockEntity` for simple in→out machines. Machines pull FE via
    `EnergyNetwork.pull(level, pos, amount, selfPos)` from adjacent cables/storages or
    expose `ForgeCapabilities.ENERGY` (framework already exposes it).
11. Farming integration: award XP via `FarmingStats.awardXp(player, XpReason.X, times)`,
    money via `FarmingStats.addMoney(player, n)` / `trySpend`, read level via
    `FarmingStats.getLevel(player)`; apply produce quality via `Quality.apply(stack,
    Quality.roll(...))` / read `Quality.of(stack)` for pricing; soil via `SoilAPI`;
    advancements via `Award.grant(player, "<path>")` + a JSON with criterion "grant"
    (`minecraft:impossible` trigger) — add your advancement ids to
    `tools/gen_advancements.py`? NO — write advancement JSONs by hand under
    `data/craftmorkovsk/advancements/` instead (tools/ is read-only for you).
12. Config: do not edit `MorkovskConfig`. If you need tunables, create
    `config/<YourPkg>Config.java` with its own `ForgeConfigSpec` + register via
    `ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC,
    "craftmorkovsk-<yourpkg>.toml")` inside your `init()` — or simply reuse the existing
    `MorkovskConfig` values where they fit.
13. **No in-game verification, ever.** Do NOT run `runClient`/`runServer`, do NOT open
    the game, do NOT screenshot. Your verification is `build` green. Report explicitly:
    "compiles clean; unverified in game — user will check".
14. Git: branch `devin/morkovsk-<yourpkg>` off `devin/morkovsk-base`; commit with
    `feat(<pkg>): ...`; `git push -u origin devin/morkovsk-<yourpkg>`. **Do NOT open a PR.**
    Do NOT merge other branches. Do NOT ask the user questions — decide and note
    assumptions in your final message to the orchestrator (it reads your session summary,
    not questions).
15. If you need an API on shared code that doesn't exist, add a small package-private or
    public helper INSIDE your package (e.g. `<pkg>/<Pkg>Hooks.java`) instead of editing
    shared files; note the integration point in your final report.

## Existing systems quick-map (on devin/morkovsk-base)

- `registry/MorkovskRegistries` — DeferredRegister hub: BLOCKS, ITEMS, BLOCK_ENTITIES,
  MENUS, ENTITIES, SOUNDS, RECIPE_SERIALIZERS, RECIPE_TYPES, PARTICLES, CREATIVE_TABS,
  FEATURES.
- `registry/MorkovskTabs` — ModTab enum {CROPS, MACHINES, FOOD, STORAGE, MISC} + `add`.
- `config/MorkovskConfig` — COMMON spec: growth/XP multipliers, machine FE/tick,
  generator rates, sprinkler ranges, base price, price fluctuation, tractor fuel,
  soil degrade chance, THE CARROT ticks.
- `data/` — PlayerFarmingData capability (xp+money), FarmingStats (awardXp/addMoney/
  trySpend/getLevel/sync), FarmingLevel enum, Quality (NBT tier on produce),
  FarmingCapabilityEvents (attach+clone+login sync), Award.grant.
- `network/` — MorkovskNet channel + SyncFarmingPacket + client cache ClientFarmingData.
- `core/FarmingHooks` — `registerIrrigation(IrrigationChecker)`,
  `registerGreenhouse(GreenhouseChecker)` — irrigation and greenhouse packages must
  call these in their `init()` so soil/crop code sees them.
- `soil/` — SoilTier enum (EXHAUSTED/NORMAL/RICH/WET/COMPOST/FERTILIZED),
  MorkovskSoilBlock, SoilAPI (tierAt/isHydrated/degrade/setTier/isFarmlandLike).
- `fertilizer/` — FertilizerItem (4 tiers registered), CompostBinBlock (LEVEL 0-7,
  compostable tags `craftmorkovsk:compostable[_rich]`).
- `crop/` — CropCatalog (29 CropDefs incl. rares), MorkovskCropBlock (growth model,
  quality on harvest, XP/degradation events wired in CropModule.Events).
- `energy/` — CableBlock (connection props), EnergyNetwork (BFS push/pull through
  cables), GeneratorBlockEntity + FuelMachineMenu + FuelScreen (canonical example),
  BiomassGeneratorBlockEntity (tag `craftmorkovsk:biomass_fuel`), BatteryBlock(+BE).
- `machine/framework/` — see rule 10.
- `tools/` — gen_tex.py, texlib.py, gen_crop_assets.py, gen_core_assets.py,
  gen_block_loot.py, gen_advancements.py, merge_lang.py, central-mirror.gradle.

## Definition of done for your session

- `./gradlew build -x test` exits 0 (run it fresh, read the output — a cached
  success is not evidence).
- All your registered content has: lang keys, models/blockstates, textures (generated),
  loot tables (blocks), recipes where the spec implies craftability, advancement JSONs.
- Your branch pushed. Final message: summary of what exists, ids registered, integration
  notes for the orchestrator, "compiles clean; unverified in game — user will check".
