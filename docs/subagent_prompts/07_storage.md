# Package: `storage` — farming storage blocks

Module class: `com.craftmorkovsk.storage.StorageModule` with `public static void init()`.

## Content

- `crop_crate` (wooden, 27 slots) and `large_crate` (54 slots — double-chest sized
  menu with two-row title) — generic storage for produce; standard chest-like
  block entity + menu + screen. Real containers: `ItemStackHandler`, ContainerData
  not needed (inventory via Slots), `stillValid` via ContainerLevelAccess.
- `seed_vault` (seed cabinet) — 27-slot storage that ONLY accepts items whose id
  ends with `_seeds` or `ItemNameBlockItem` instances (filter in Slot
  `mayPlace` via `SlotItemHandler` override or a filtered handler). Shows a small
  leaf icon. Bonus: sorting button? skip.
- `silo` — tall grain silo (block entity): accepts only grain/seeds/produce
  (tag `craftmorkovsk:silo_storable` — put crop produce + seeds + grain items in
  it); single item-type storage: stores ONE item type up to 2048 units — itemstack
  count view on screen, insert/extract via slots or hopper-friendly IItemHandler
  cap. Simpler acceptable impl: 18 large slots each capped at 256 (custom slot
  limit via SlotItemHandler.getMaxStackSize) — but "one type" silo is more
  authentic; pick the simpler one and document choice.
- `refrigerated_chest` — 27-slot fridge: preserves food — mechanics: food items
  stored gain a hidden "kept" tag? There's no food decay in MC — so make it
  *meaningful*: while powered by FE (draw 1 FE/tick via network pull, or free),
  food inside slowly accumulates a "chilled" bonus? Simplest real mechanic:
  fridge requires FE (5 FE/tick via `EnergyNetwork.pull` or neighbor energy cap);
  while powered, any `craftmorkovsk` food inside gets +1 quality-tier on extraction
  (cap at PERFECT) — implement via `Quality.upgrade` on take-out slot? That is a
  genuine mechanic: `RefrigeratedChestMenu`'s output/take path calls
  `Quality.apply(stack, min(PERFECT, of(stack)+1))` for OUR food items (QualityProduceItem
  only — produce items, not prepared foods). Alternatively chilled food just gets
  renamed tag — keep the quality-upgrade version but ONLY for produce items
  (instanceof QualityProduceItem).
- All storage blocks: `EntityBlock` + proper `onRemove` drop contents + loot table
  drop-self + `mineable/axe` tag + recipe (planks/iron themed).

## Menus & screens

- Chest-like `ChestMenu`-equivalent custom menus (don't reuse vanilla ChestMenu —
  your own AbstractContainerMenu) + `StorageScreen extends AbstractContainerScreen`
  with the agri palette (reuse MachineScreen colors: 0xFF4A3B28/0xFF7A6A4F/0xFFC6B99B)
  — draw panels + slot grid (18px cells). Screen size adapts to rows (chest 3 rows
  player + 3 storage rows; double = 6).
- Register menus via `IForgeMenuType.create` with pos-in-buf client ctor pattern
  (copy FuelMachineMenu's approach); screens in YOUR client subscriber.

## Integration

- Tab `MorkovskTabs.ModTab.STORAGE`.
- Lang `lang_parts/storage.json`.
- Advancement `stockpiler` — first item stored in a silo or seed vault (Forge event
  or menu insert hook → simplest: when player inserts into silo/seed_vault via your
  menu quickMove/slot listener, call `Award.grant(player, "stockpiler")` — you can
  override `Slot.set`/`onTake` or just check in menu click: implement a tiny
  `onContentsChanged` → grant to the menu's player).
- If refrigerator needs FE: give the block entity an IEnergyStorage (use
  `MorkovskEnergyStorage.machine(...)`) + energy bar on screen.
