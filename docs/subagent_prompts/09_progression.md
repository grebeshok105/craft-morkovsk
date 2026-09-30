# Package: `progression` — handbook, /morkovsk commands, weather FX, HUD, sounds

Module class: `com.craftmorkovsk.progression.ProgressionModule` with `public static void init()`.

## Morkovsk Agricultural Handbook

- `agricultural_handbook` item (model `item` texture, agri-brown book icon) —
  right-click opens `HandbookScreen` (client-only Screen, NOT a menu — open via
  client `PlayerInteractEvent.RightClickItem` or `item.use` returning success then
  `DistExecutor` open screen — standard pattern: override `use` to
  `Minecraft.getInstance().setScreen(new HandbookScreen())` inside a client-only
  branch via `DistExecutor.unsafeCallWhenOn`).
- Screen: paged book GUI in agri palette — left/right nav arrows; sections:
  welcome, crops table (pull `CropCatalog.ALL` — name, soil prefs, water need,
  price — secret crops hidden or "???"), soils & fertilizer tiers, machines list,
  energy primer, animals, economy, progression levels (`FarmingLevel.values()`)
  and THE CARROT teaser. Static pages via `font.drawWordWrap`; keep ~8-12 pages.

## `/morkovsk` command (server ops)

`RegisterCommandsEvent` subscriber (FORGE bus, your package):
`/morkovsk give <crop>` — gives seeds+produce of a CropDef (suggestion list from
`CropCatalog.BY_ID.keySet()`); `/morkovsk money <add|set> <amount>`;
`/morkovsk xp <add|set> <amount>`; `/morkovsk level` — print level/xp/money;
`/morkovsk crops` — list all crop ids; `/morkovsk weather <drought|blessed>`
applies a server weather modifier (see below). Requires permission level 2.
Money/xp via `FarmingStats` (addMoney/awardXp; "set" needs a setter — call
`PlayerFarmingData` capability directly via `getCapability`+`setXp`? capability has
fields—add no shared edits: manipulate via existing methods addMoney/awardXp loops
or access `PlayerFarmingData` public fields—check the class: xp/money fields are
instance fields on the data object — you CAN do `cap.setXp(...)`? If no setter,
use `cap.xp = v`? fields are private? — check `PlayerFarmingData.java`; if private
you may only use awardXp/addMoney — then implement set via direct field access
using the public getters + a new small helper inside YOUR package? You may NOT edit
data/. Use `cap.serializeNBT`/`deserializeNBT` round-trip to set values — works).

## Weather effects

- `WeatherModule` logic: on `ServerTickEvent` (FORGE) keep a daily "blessed/drought"
  flag per level (or global): `drought` — crops won't grow unless hydrated (already
  modeled — drought just forces `SoilAPI.isHydrated=false`? can't fake the API —
  implement drought as *speeding soil moisture decay*: every ~100 ticks, for each
  random block? Expensive — alternative: drought = cosmetic: send actionbar msg +
  disable rain? `level.setRainLevel(0)`. Keep it simple and real: `blessed day`
  grants +1 quality luck to harvests (Quality.roll soilBonus+1 — hook: a
  `progression` static flag consulted where? Quality.roll is called inside
  MorkovskCropBlock with soil's qualityBonus — can't inject. → Simplify to what IS
  possible cleanly: weather commands only do `ServerLevel.setWeatherParameters`
  rain/thunder + our own `DroughtTracker` that periodically dehydrates farmland
  blocks in loaded chunks (MorkovskSoilBlock setValue MOISTURE 0 for a few random
  positions per chunk — bounded scan via chunk random tick? too complex).
  FINAL: `blessed`/`drought` commands set vanilla weather (rain/thunder — affects
  growth via existing rain/thunder logic in crops) + broadcast themed message.
  Add `rain_dance` advancement when player makes it rain via command? grant in
  command handler.
- Crop sparkle: on `BlockEvent.BreakEvent`? no — particles on harvest handled
  client-side already optional; add `harvest_poof` particle? SKIP particles type —
  use server `sendParticles(ParticleTypes.HAPPY_VILLAGER)` on level-up (do in
  your subscriber: listen for chat? can't hook FarmingStats — spawn particles in
  your own code paths only — e.g., on handbook open? skip if not clean).

## HUD overlay (client)

`MorkovskHudOverlay` — `RegisterGuiOverlaysEvent` (Forge 1.20.1:
`net.minecraftforge.client.event.RegisterGuiOverlaysEvent` exists — register an
`IGuiOverlay` named `morkovsk_hud`): bottom-left agri-styled panel showing
`ClientFarmingData` xp/level(`FarmingLevel` name)/money. Draw with fill rects +
text (no texture needed) — semi-transparent brown panel, gold money line.
Toggleable via `/morkovsk_hud` client command? skip toggle.

## Sounds

Register sound events via `MorkovskRegistries.SOUNDS`: `harvest` (reuse
`minecraft:item.crop.plant`? sound events need `sounds.json` entries — simplest:
create `sounds.json` referencing vanilla sound files, e.g. `crop.harvest` →
`minecraft:block.crop.break`, `level.up` → `minecraft:entity.player.levelup`,
`machine.hum` → `minecraft:block.furnace.fire_crackle`, `tractor.engine` →
`minecraft:entity.minecart.riding`) — `assets/craftmorkovsk/sounds.json` maps
event ids to vanilla asset paths (NO new .ogg needed — reference existing vanilla
sounds by name!). Play `level.up` from... FarmingStats already plays
`SoundEvents.PLAYER_LEVELUP` — your sound events usable elsewhere (handbook open
sound, etc.) — wire a few `playSound` calls where natural (trough eat, shop buy —
those are other packages; just register events + play `handbook_open` on use).

## Lang + data

- `lang_parts/progression.json` — handbook strings, command feedback (use
  translatable components `commands.craftmorkovsk.*`), HUD labels.
- Advancements: `rain_dance`, plus `bookworm` (open handbook first time —
  grant in `use` via Award.grant on server side — `use` runs server too: pass
  ServerPlayer).
- A tiny `FarmingEvents` FORGE subscriber in your package for whatever you wire.
