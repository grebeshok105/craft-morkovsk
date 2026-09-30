# Craft Morkovsk — E2E In-Game Test Plan (Forge 1.20.1 dev client)

Env: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 DISPLAY=:0 ./gradlew runClient` from
`/home/ubuntu/repos/craft-morkovsk`. Dev client logs in as "Dev". Mod id `craftmorkovsk`.

Evidence gathered from code (file:line):
- Commands: `MorkovskCommands.java:41-69` — `/morkovsk give <crop>|money add|set|xp add|set|level|crops|weather drought|blessed`, perm level 2 → world must have cheats ON.
- `give` gives 16 seeds + 8 produce of `CropCatalog` id (`morkov`, `wheat_rye`, `corn`, ...).
- Quality NBT: int tag `"Quality"` on produce, tooltip line always rendered (`QualityProduceItem.java:31`, `Quality.java:30-37`). Tiers: NORMAL has no tag but tooltip still shows tier name.
- Mill recipes `data/craftmorkovsk/recipes/mill_flour_{wheat,spelt,rye,corn}.json`: vanilla `minecraft:wheat` or `craftmorkovsk:wheat_rye` → `craftmorkovsk:flour`, 200 ticks, 20 FE.
- Small generator burns any smelting fuel (coal), pushes FE via `EnergyNetwork` through `energy_cable` (`GeneratorBlockEntity.java:50-70`).
- Water channel sips from adjacent water source OR adjacent `water_tank`; irrigates 4 horizontal neighbors, pushes moisture onto FarmBlocks (`WaterChannelBlockEntity.java:43-79`).
- Water tank: right-click water bucket adds, empty bucket extracts (`WaterTankBlock.java:56-70`).
- Tractor: `tractor_item.useOn` spawns entity; right-click with coal/plant_oil fuels; right-click mounts; sneak-right-click opens cargo menu; WASD drives; attachment item right-click installs (`TractorEntity.java:180-220`).
- Shipping crate: sells 27 slots every 600 ticks (30 s), pays nearest player within 8 blocks (`ShippingCrateBlockEntity.java:29`).
- HUD overlay renders bottom-left: rank, XP, morkoins (`MorkovskHudOverlay.java:14-52`). NOTE: lead said "top area" — code says bottom-left; assert wherever visible.
- Shop block right-click opens ShopMenu (`MorkovskShopBlock.java:44-48`).
- Creative tabs: CROPS, MACHINES, FOOD, STORAGE, MISC (`MorkovskTabs.java:26-30`).

## World setup (in-recording, part of T1 flow)
Singleplayer → Create New World → Game Mode: Creative, Allow Cheats: ON, World Type: Superflat.
Adversarial note: cheats OFF would silently disable `/morkovsk` (perm 2) — must toggle ON.

## Test cases (recorded continuously with annotations)

1. **It should show Craft Morkovsk in the Mods list** — Main menu → Mods → scroll/filter "morkovsk".
   PASS: entry "Craft Morkovsk" (craftmorkovsk) listed. FAIL: absent.

2. **It should load a world and render the farming HUD** — create world per setup; once in-game,
   screenshot shows HUD panel (bottom-left per code) with rank name + XP + morkoins lines.
   PASS: panel visible with readable values. FAIL: no overlay.

3. **It should run /morkovsk admin commands** — open chat (T), run:
   `/morkovsk level` → chat line with rank/xp/money;
   `/morkovsk money add 100` → success line AND HUD money becomes 100;
   `/morkovsk xp add 50` → success line;
   `/morkovsk crops` → chat lists crop ids (must contain `morkov`).
   PASS: all four produce confirmation output; FAIL: red error / "unknown command".

4. **It should give crop kits via /morkovsk give** — `/morkovsk give morkov`, open inventory.
   PASS: 16× morkov_seeds + 8× morkov produce present.

5. **It should open the agricultural handbook GUI** — `/give @s craftmorkovsk:agricultural_handbook`,
   right-click in air. PASS: paged book/screen opens (not chat error); page nav buttons clickable.

6. **It should plant, bonemeal and harvest a quality crop** — hoe grass → farmland; plant
   morkov_seeds (right-click farmland); bonemeal crop until mature; break it; pick up drops;
   hover produce in inventory → tooltip shows Quality tier line (name may carry tier prefix).
   PASS: drops obtained AND tooltip shows a Quality line. FAIL: no drops or no quality info.

7. **It should power the mill and grind produce to flour** — place small_generator, energy_cable
   adjacent, mill adjacent to cable (a line of 3 blocks). Right-click generator → fuel GUI →
   insert coal → burn/progress visible. Right-click mill → agri screen shows energy rising +
   progress arrow; put `wheat_rye` (from `/morkovsk give wheat_rye`) in input → flour in output.
   PASS: energy indicator >0, progress animates, flour appears. FAIL: 0 energy / no output.

8. **It should irrigate farmland via tank + channel** — place water_tank, fill with water buckets
   (right-click ×4-8); place water_channel touching tank AND farmland; wait ≤10 s.
   PASS: adjacent farmland turns moist (dark soil) vs dry control farmland; F3 shows moisture>0
   if needed. FAIL: stays dry.

9. **It should spawn, fuel, mount and drive the tractor** — give `craftmorkovsk:tractor_item` +
   coal; right-click ground → tractor spawns; right-click with coal (fuel sound); right-click →
   player mounts; press W/A/S/D → tractor moves; sneak to dismount. Optional: attachment_plow.
   PASS: spawn + mount + WASD movement. FAIL: no spawn / can't mount / doesn't move.

10. **It should pay out the shipping crate and open the shop** — place shipping_crate, put produce
    inside (right-click → GUI), note money, wait ~35 s within 8 blocks.
    PASS: money increases (HUD/`/morkovsk level` delta) or payout message. Then place
    morkovsk_shop, right-click → shop GUI with priced offers opens. FAIL: no payout / no GUI.

11. **(Optional) animals/storage spot-check** — spawn duck (`/summon craftmorkovsk:duck` or
    duck_spawn_egg), place feeding_trough; open crop_crate like a chest. PASS: entity exists,
    GUIs open.

## Crash protocol
If client crashes: capture `run/crash-reports/crash-*.txt` path + last 60 lines of
`run/logs/latest.log`, report exact error. Do not retry blindly — one retry max with same flags.
