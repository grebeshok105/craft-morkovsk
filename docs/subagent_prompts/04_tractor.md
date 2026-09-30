# Package: `tractor` — rideable field vehicle + attachments

Module class: `com.craftmorkovsk.tractor.TractorModule` with `public static void init()`.

## Entity: `tractor`

- `TractorEntity extends net.minecraft.world.entity.vehicle.VehicleEntity` (or
  `Entity` + custom ride logic — VehicleEntity gives nice plumbing) — a ridable,
  player-controlled vehicle:
  - Ride via right-click; driver controls with `travel` (WASD forward/back/turn) —
    see Boat/Pig movement patterns; keep it simple: land vehicle, affected by gravity,
    moves on ground, slow turn rate.
  - Fuel: internal tank `MorkovskConfig.TRACTOR_MAX_FUEL` units; burns
    `MorkovskConfig.TRACTOR_FUEL_PER_TICK` while moving. Refuel by right-click with
    coal/charcoal or `craftmorkovsk:plant_oil` (better fuel value). No fuel → won't move.
  - Inventory: small (9 slots) ItemStackHandler — harvested/planted items land here.
  - Menu+screen: `TractorMenu`/`TractorScreen` — open while riding via jump/sneak+use
    key or via a `TractorKeyPacket` C2S (register via MorkovskNet) — simplest acceptable:
    sneak-right-click opens menu, normal right-click mounts.
  - Sync: fuel/attachment id via `EntityDataAccessor` (define SERIALIZERS — fuel int,
    attachment string→id, or byte enum).
- Spawn item `tractor_item` — places the entity (like minecart item pattern).
- Renderer: `TractorRenderer` in your package client subscriber — a simple
  `EntityModel` box-ish tractor (TexturedModelData boxes: body, seat, exhaust pipe,
  4 wheels) — colored agri-yellow/green. It must actually render (implement
  `createLayerLocation` + `ModelLayerLocation` registration in client subscriber).

## Attachments (4 — change what driving over crops/dirt does)

Attachment is ONE item type `tractor_attachment`? No — 4 distinct items:
`attachment_plow`, `attachment_planter`, `attachment_harvester`, `attachment_spreader`.
Stored on the entity (data param int 0..3); changed via the menu slot (attachment slot
accepts the 4 items) or right-click with the item while riding.

Every ~10 ticks while moving, the attachment acts on the block under the tractor:

- plow: dirt/grass_block/dirt_path → converts to `craftmorkovsk:rich_farmland`
  (use `SoilAPI.setTier`? setTier only accepts existing soil/farmland/dirt — extend via
  level.setBlock with the NORMAL-tier block from `SoilModule.BLOCKS_BY_TIER`).
- planter: over hydrated farmland (`SoilAPI.isFarmlandLike` + empty above): takes seeds
  from the tractor inventory and plants (ItemNameBlockItem place logic or just
  `level.setBlock(cropState)` + shrink) — scan inventory for `*_seeds` items.
- harvester: mature `MorkovskCropBlock` under/front of tractor → collect its drops into
  inventory (getDrops via LootParams), apply `SoilAPI.degrade`, set age 0 (keep plant!)
  or break it (document choice; recommend keep-plant-at-age0 for throughput).
- spreader: takes fertilizer items (`FertilizerItem`) from inventory and applies
  `SoilAPI.setTier` on farmland under, like manual use.

Sounds: `SoundEvents.MINECART_RIDING` loop while driving? entity riding sound is complex —
acceptable minimum: engine sound on mount + occasional `SoundEvents.PISTON_EXTEND`/
GRASS_BREAK on attachment action. Particles: exhaust `ParticleTypes.LARGE_SMOKE` while
moving (client-side tick via entity.tick — spawn in `tick` on client only).

## Assets + integration

- Entity registration via `MorkovskRegistries.ENTITIES` (`EntityType.Builder`).
- Entity attributes: custom `EntityType` + `EntityRenderersEvent.RegisterRenderers` in
  YOUR client subscriber; also `EntityAttributeCreationEvent` not needed (vehicle).
- Spawn egg? `ForgeSpawnEggItem` fine (tab MACHINES).
- Item models/textures via gen_tex (`item`, `machine`); tractor item icon.
- Menu: write `TractorMenu` extending MachineMenu? Tractor isn't a BlockEntity —
  extend `AbstractContainerMenu` directly with the entity's ItemStackHandler +
  a `ContainerData` fuel sync (implement `SimpleContainerData` updated server-side
  or just read entity fields server side — menu on server reads entity; client gets
  entityDataAccessor-synced fuel — simplest: use data accessor for GUI, no ContainerData).
- Advancement `tractor_driver` — mount tractor (event/hook) → Award.grant.
- Tractor crafting recipe: wheels (iron) + engine (generator-ish) + seat (planks);
  attachment recipes each (iron + tool-themed items).
- Lang parts file `lang_parts/tractor.json`; README note in your final message about
  menu/open gesture chosen.
