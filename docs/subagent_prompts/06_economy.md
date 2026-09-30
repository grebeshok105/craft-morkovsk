# Package: `economy` — crop economy: shipping crate + Morkovsk shop

Module class: `com.craftmorkovsk.economy.EconomyModule` with `public static void init()`.

## `shipping_crate` block

- BlockEntity with a 27-slot sell inventory; players deposit produce/items.
- Every 30 seconds (or at dawn — pick: every 600 ticks check `level.getDayTime()%24000`
  crossing) the contents are "sold": each stack valued at its price ×
  `Quality.of(stack).priceMultiplier` × daily fluctuation, inventory cleared,
  `FarmingStats.addMoney(nearestOrInteractingPlayer, total)` + XP `SELLING`.
  Selling needs a player attribution: credit the last player who opened the menu
  (store UUID; if absent, nearest player within 8 blocks; else no credit but still
  consume? — safer: only sell when a known player UUID's player is online? Keep
  simple: sell whenever, credit last interacting player if online, else hold until
  they are — simplest correct: sell on tick; credit lastInteractingPlayer if
  resolvable via `level.getPlayerByUUID`, else stash earnings in a pendingPayout
  field paid out on next interaction. Implement pendingPayout — it's the robust
  choice).
- Menu + screen (`MachineMenu`-style or custom AbstractContainerMenu + Screen showing
  current estimated total and money balance `ClientFarmingData.money()`).
- Price source: `CropCatalog.BY_ID` defs have `price`; for non-crop items keep a small
  price map (food items ~nutrition×2, misc = `MorkovskConfig.BASE_CROP_PRICE`).
  `PriceTable.itemPrice(stack)` helper: produce → def.price (or BASE_CROP_PRICE),
  quality multiplier applied.
- Daily fluctuation: per-player-day random factor `1 ± PRICE_FLUCTUATION` seeded by
  `level.getDayTime()/24000 + item hash` (deterministic per day — fine).

## `morkovsk_shop` block (the seed/fertilizer/parts store)

- Block + Menu + Screen; a "villager-less" trading post. Shop offers (hardcoded list —
  real, functional):
  - Seeds: all `CropCatalog` seed items, price = def.price/2 rounded up (min 2);
    rares priced steeply (golden 400, giant 150, supreme 900, the_carrot 9999 — if
    `def.secret`, hide it until player has `morkovsk_supreme` advancement —
    check via `player.getAdvancements()`... simplest: hide secret seeds unless
    `FarmingStats.getLevel >= 3`).
  - Fertilizers: compost(5), manure_fertilizer(8), mineral_fertilizer(12),
    morkovsk_fertilizer(40).
  - Machine/tractor "parts": `engine_part`, `gearbox`, `sprinkler_head`,
    `tractor_wheel` (crafting components — also add shaped recipes for them;
    they are used in OTHER packages' recipes — flag in your report that these ids
    exist for others to use? They are YOUR ids; other sessions may reference them
    in recipes by name `craftmorkovsk:engine_part` etc — keep the names exact).
  - Upgrades: `soil_test_kit` (item; right-click farmland → chat shows tier+moisture
    via SoilAPI — nice tool), `growth_charm`? keep list modest: soil_test_kit,
    `golden_hoe`? No vanilla-clone items — just soil_test_kit + parts.
- Buy flow: shop menu has offer slots (ghost display stacks); click an offer →
  `trySpend(player, price)` → give item (into player inventory or shop output slot).
  Use a C2S packet `BuyPacket(offerIndex)` via MorkovskNet (server validates +
  transaction). Ghost slots: render-only slots or a simple screen drawing offer
  stacks + price text — acceptable: a `ShopMenu` with no real slots + custom
  `ShopScreen` listing offers as buttons (AbstractContainerScreen with buttons).
- Currency is `FarmingStats` money (capability), NOT an item. Show balance on screen.

## Assets/data

- Blocks: textures/models/loot/recipes (crate: planks+iron; shop: planks+wool+carrot).
- Lang `lang_parts/economy.json` incl. all offer names/descriptions + shop strings.
- Advancement `first_sale` (first successful ship) + `big_spender` (spend 5000 total —
  track via... simplest: check in trySpend-success path: keep a per-offer check
  `if spent this purchase >= 5000`? Better: `FarmingStats.getMoney` decline? skip
  big_spender if awkward — keep `first_sale` only, and `rich_farmer` when money
  crosses 10_000 (check inside addMoney call site: shop/crate — just check after
  selling: `if FarmingStats.getMoney(player) >= 10000 → Award.grant`).
- Processing of `Quality`: show quality star in offer tooltips? For sales use
  `Quality.of(stack)` multiplier — implemented in PriceTable.
