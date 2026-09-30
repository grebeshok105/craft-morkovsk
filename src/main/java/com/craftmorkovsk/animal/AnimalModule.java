package com.craftmorkovsk.animal;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;

/** Farm animals: duck, turkey, farm goat + feeding trough, eggs, goat milk, manure.
 *  Registration only — wiring entry is {@code AnimalModule.init()} (called by the
 *  orchestrator from MorkovskModules). */
public final class AnimalModule {

    public static final TagKey<Item> DUCK_FOOD =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "duck_food"));
    public static final TagKey<Item> TURKEY_FOOD =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "turkey_food"));
    public static final TagKey<Item> GOAT_FOOD =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "goat_food"));
    public static final TagKey<EntityType<?>> FARM_ANIMALS =
            TagKey.create(Registries.ENTITY_TYPE,
                    new ResourceLocation(CraftMorkovsk.MOD_ID, "farm_animals"));

    public static RegistryObject<EntityType<DuckEntity>> DUCK;
    public static RegistryObject<EntityType<TurkeyEntity>> TURKEY;
    public static RegistryObject<EntityType<FarmGoatEntity>> FARM_GOAT;
    public static RegistryObject<EntityType<ThrownDuckEgg>> DUCK_EGG_PROJECTILE;
    public static RegistryObject<EntityType<ThrownTurkeyEgg>> TURKEY_EGG_PROJECTILE;

    public static RegistryObject<Item> DUCK_EGG;
    public static RegistryObject<Item> TURKEY_EGG;
    public static RegistryObject<Item> GOAT_MILK_BUCKET;
    public static RegistryObject<Item> MANURE;
    public static RegistryObject<Item> DUCK_SPAWN_EGG;
    public static RegistryObject<Item> TURKEY_SPAWN_EGG;
    public static RegistryObject<Item> FARM_GOAT_SPAWN_EGG;

    public static RegistryObject<Block> FEEDING_TROUGH;
    public static RegistryObject<BlockEntityType<FeedingTroughBlockEntity>> FEEDING_TROUGH_BE;

    private AnimalModule() {}

    private static RegistryObject<Item> simpleItem(String name, MorkovskTabs.ModTab tab) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(name,
                () -> new Item(new Item.Properties()));
        MorkovskTabs.add(tab, item::get);
        return item;
    }

    public static void init() {
        // --- Entities ---
        DUCK = MorkovskRegistries.ENTITIES.register("duck",
                () -> EntityType.Builder.of(DuckEntity::new, MobCategory.CREATURE)
                        .sized(0.6F, 0.7F).clientTrackingRange(10)
                        .build(new ResourceLocation(CraftMorkovsk.MOD_ID, "duck").toString()));
        TURKEY = MorkovskRegistries.ENTITIES.register("turkey",
                () -> EntityType.Builder.of(TurkeyEntity::new, MobCategory.CREATURE)
                        .sized(0.7F, 0.9F).clientTrackingRange(10)
                        .build(new ResourceLocation(CraftMorkovsk.MOD_ID, "turkey").toString()));
        FARM_GOAT = MorkovskRegistries.ENTITIES.register("farm_goat",
                () -> EntityType.Builder.of(FarmGoatEntity::new, MobCategory.CREATURE)
                        .sized(0.9F, 1.3F).clientTrackingRange(10)
                        .build(new ResourceLocation(CraftMorkovsk.MOD_ID, "farm_goat").toString()));
        DUCK_EGG_PROJECTILE = MorkovskRegistries.ENTITIES.register("duck_egg",
                () -> EntityType.Builder.<ThrownDuckEgg>of(ThrownDuckEgg::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
                        .build(new ResourceLocation(CraftMorkovsk.MOD_ID, "duck_egg").toString()));
        TURKEY_EGG_PROJECTILE = MorkovskRegistries.ENTITIES.register("turkey_egg",
                () -> EntityType.Builder.<ThrownTurkeyEgg>of(ThrownTurkeyEgg::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
                        .build(new ResourceLocation(CraftMorkovsk.MOD_ID, "turkey_egg").toString()));

        // --- Items ---
        DUCK_EGG = MorkovskRegistries.ITEMS.register("duck_egg",
                () -> new FarmEggItem(new Item.Properties(), ThrownDuckEgg::new));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, DUCK_EGG::get);
        TURKEY_EGG = MorkovskRegistries.ITEMS.register("turkey_egg",
                () -> new FarmEggItem(new Item.Properties(), ThrownTurkeyEgg::new));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, TURKEY_EGG::get);
        GOAT_MILK_BUCKET = MorkovskRegistries.ITEMS.register("goat_milk_bucket",
                () -> new GoatMilkBucketItem(new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.FOOD, GOAT_MILK_BUCKET::get);
        MANURE = simpleItem("manure", MorkovskTabs.ModTab.MISC);

        DUCK_SPAWN_EGG = MorkovskRegistries.ITEMS.register("duck_spawn_egg",
                () -> new ForgeSpawnEggItem(DUCK, 0xC9B896, 0x2E6B3A, new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, DUCK_SPAWN_EGG::get);
        TURKEY_SPAWN_EGG = MorkovskRegistries.ITEMS.register("turkey_spawn_egg",
                () -> new ForgeSpawnEggItem(TURKEY, 0x5A3B22, 0xB03A2E, new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, TURKEY_SPAWN_EGG::get);
        FARM_GOAT_SPAWN_EGG = MorkovskRegistries.ITEMS.register("farm_goat_spawn_egg",
                () -> new ForgeSpawnEggItem(FARM_GOAT, 0xD8D2C4, 0x6E6E6E, new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, FARM_GOAT_SPAWN_EGG::get);

        // --- Feeding trough ---
        FEEDING_TROUGH = MorkovskRegistries.BLOCKS.register("feeding_trough",
                () -> new FeedingTroughBlock(BlockBehaviour.Properties.of()
                        .strength(0.8F).sound(SoundType.WOOD)));
        RegistryObject<Item> troughItem = MorkovskRegistries.ITEMS.register("feeding_trough",
                () -> new BlockItem(FEEDING_TROUGH.get(), new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, troughItem::get);

        FEEDING_TROUGH_BE = MorkovskRegistries.BLOCK_ENTITIES.register("feeding_trough",
                () -> BlockEntityType.Builder.of(FeedingTroughBlockEntity::new,
                        FEEDING_TROUGH.get()).build(null));
    }
}
