# Craft Morkovsk Implementation Plan

> **For agentic workers:** orchestrated build — the orchestrator implements the core skeleton, then parallel Devin child sessions implement one package each, then the orchestrator merges, wires, and verifies. User pre-authorized subagents and waived review ("суб агентов разрешаю, никаких вопросов").

**Goal:** A large, actually-playable Forge 1.20.1 farming expansion ("treat carrot farming with unreasonable seriousness"): many crops, soil/fertilizer, irrigation, powered machines, a tractor, new animals, food processing, storage, progression, economy, greenhouse, weather effects, worldgen, advancements, handbook, commands.

**Architecture:** Forge 1.20.1 (47.4.23), Java 17, single module `com.craftmorkovsk`. One shared registry hub (`registry/MorkovskRegistries`) with DeferredRegisters; each subsystem lives in its own top-level package with a `<Pkg>Module` class owning all its content registration; `CraftMorkovsk` + `client/MorkovskClient` are composition roots wired by the orchestrator only. Shared contracts live in `core/` (capabilities, quality, network, hooks, config, machine framework, energy net). No cross-package imports between sibling feature packages — only into `core/` and `registry/`.

**Tech Stack:** Forge MDK / ForgeGradle 6, official Mojang mappings, Forge Energy (IEnergyStorage), Forge capabilities, SimpleChannel, menus/screens, procedural 16x16 PNG generator (`tools/gen_tex.py`), JSON assets.

**Spec:** user-pasted spec (session prompt) — 25+ crops, 5 soil types, 4 fertilizer tiers + composting, irrigation chain, 9 machines + energy infra, tractor + 4 attachments, 3 animals + troughs, ~20 food products, 5 storage blocks, XP/levels/money, handbook, economy+shop, crop quality NBT, worldgen structures, greenhouse, weather, advancements, commands, config, multiplayer-safe.

## Global Constraints

- Java 17, Forge `1.20.1-47.4.23`, mod id `craftmorkovsk`, package `com.craftmorkovsk`.
- Server-authoritative: no gameplay state client-only; menus/BEs sync via ContainerData; packets validated server-side; dedicated server must start.
- No placeholders: every block/item has model, texture (procedural OK), loot/recipe where meaningful, lang entry.
- `en_us.json` is assembled from `lang_parts/<pkg>.json` fragments by `tools/merge_lang.py` (children never edit en_us.json directly).
- Children add files ONLY inside their owned package dirs + their resources subdirs + their lang fragment + `SESSION-CHILD` none; the only permitted edits to shared files are the ones listed in each child prompt (module wiring is orchestrator-only).
- No in-game verification by agents: compile (`./gradlew build`) is the gate; user verifies in-game.

## Package ownership

| Package | Owner | Contents |
|---|---|---|
| `core`, `registry`, `config`, `network`, `data`, `crop`, `soil`, `fertilizer`, `energy`, `machine/framework`, `item` | orchestrator (this session) | registries hub, tabs, Forge config, farming capability (xp/level/money+sync), Quality NBT, MorkovskNet, FarmingHooks (greenhouse/irrigation callbacks), soil blocks + degradation, fertilizer items + compost bin, ALL crop defs + blocks + seeds + produce incl. rare crops, FE network (cables/generator/biomass/battery), machine framework (AbstractMachineBlock/MachineBlockEntity/MachineMenu/MachineScreen/ProcessingRecipe), canonical example machine (composter? generator), texture/lang tooling |
| `irrigation` | child A | pipes, water channels, sprinklers + advanced, tanks, pumps, valves; registers `IrrigationHook` so SoilAPI sees hydration |
| `machine` (sans framework) | child B | planter, harvester, crop collector, fertilizer spreader, seed sorter — FE-powered, inventories, menus, screens |
| `food` + processing machines | child C | grain mill, oil press, food processor, industrial composter + ~20 food items (nutrition/saturation/buffs) + ProcessingRecipe JSONs |
| `tractor` | child D | tractor entity (rideable, fuel, inventory, health, engine sounds, wheel spin) + plow/planter/harvester/spreader attachments + tractor item |
| `animal` | child E | duck, turkey, goat entities (AI/breeding), trough block, manure/egg/milk production, feeding mechanics |
| `economy` | child F | shipping crate/market block (sell for quality-aware price → money cap), shop screen (seeds/fertilizer/parts/tractor upgrades/rare crops) |
| `storage` | child G | wooden crate, vegetable crate, grain silo (bulk), seed cabinet, refrigerated storage + GUIs |
| `worldgen` + `greenhouse` | child H | greenhouse glass/frame + GreenhouseCheck hook impl + growth boost; programmatic Features (abandoned farm, farmhouse, ruined greenhouse, warehouse, windmill), wild crop patches, loot |
| `progression` + `handbook` + `commands` + `weather` | child I | advancement tree JSON + code grants, Morkovsk Agricultural Handbook item+screen, farmer HUD, /morkovsk commands, weather event effects |

## Review Focus

- Packet discriminator order: children register packets inside their module init; module call order in the composition root is fixed — must stay identical on both ends (same jar → same order; keep module list a single ordered list).
- Crop quality NBT must survive loot → item; higher quality → higher sell price (economy reads `Quality.of(stack)`).
- Soil degradation on harvest + fertilizer restore; hydration from vanilla moisture OR irrigation hook OR rain.
- Dedicated-server safety: no `net.minecraft.client` imports outside `client/` and screen classes; DistExecutor/`@OnlyIn` on client-only paths.
- Capabilities: clone-on-death persistence, login sync, `invalidateCaps` on detach.
- Machines keep working inventories + energy + progress synced via ContainerData (not custom packets per tick).

## Tasks (orchestrator order)

1. MDK bootstrap + first `./gradlew build` green. *(done: MDK extracted, build running)*
2. Core skeleton files per table above; `gen_tex.py`, `merge_lang.py`, `gen_crop_assets.py` (blockstates/models/loot/lang fragment for all crops); compile green.
3. Push `devin/<ts>-morkovsk` branch, open draft PR — this is the integration base.
4. Launch 9 child sessions (parallel, fully independent packages) with contract prompts: consume `core` APIs, produce package code + assets + lang fragment + SESSION note; `./gradlew build` must pass; push branch `devin/morkovsk-<pkg>` from the base branch; NO in-game testing.
5. Merge branches into base, wire module list + client init + lang merge, resolve conflicts.
6. `./gradlew build` green; optional `runServer` smoke; PR ready; report (in-game verification left to user).
