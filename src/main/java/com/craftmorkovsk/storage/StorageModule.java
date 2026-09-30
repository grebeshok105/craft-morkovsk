package com.craftmorkovsk.storage;

import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Farm storage: crop crate, large crate, seed vault, grain silo and the
 *  FE-powered refrigerated chest. Screens wire in {@link StorageClientInit}. */
public final class StorageModule {

    public static RegistryObject<Block> CROP_CRATE;
    public static RegistryObject<Block> LARGE_CRATE;
    public static RegistryObject<Block> SEED_VAULT;
    public static RegistryObject<Block> SILO;
    public static RegistryObject<Block> REFRIGERATED_CHEST;

    public static RegistryObject<BlockEntityType<StorageBlockEntity>> CROP_CRATE_BE;
    public static RegistryObject<BlockEntityType<StorageBlockEntity>> LARGE_CRATE_BE;
    public static RegistryObject<BlockEntityType<StorageBlockEntity>> SEED_VAULT_BE;
    public static RegistryObject<BlockEntityType<StorageBlockEntity>> SILO_BE;
    public static RegistryObject<BlockEntityType<RefrigeratedChestBlockEntity>> REFRIGERATED_BE;

    public static RegistryObject<MenuType<StorageMenu>> CROP_CRATE_MENU;
    public static RegistryObject<MenuType<StorageMenu>> LARGE_CRATE_MENU;
    public static RegistryObject<MenuType<StorageMenu>> SEED_VAULT_MENU;
    public static RegistryObject<MenuType<StorageMenu>> SILO_MENU;
    public static RegistryObject<MenuType<RefrigeratedMenu>> REFRIGERATED_MENU;

    private StorageModule() {}

    /** Menu type for a storage kind — used by the block entity's createMenu. */
    public static MenuType<StorageMenu> menuType(StorageKind kind) {
        return switch (kind) {
            case CROP_CRATE -> CROP_CRATE_MENU.get();
            case LARGE_CRATE -> LARGE_CRATE_MENU.get();
            case SEED_VAULT -> SEED_VAULT_MENU.get();
            case SILO -> SILO_MENU.get();
        };
    }

    private static RegistryObject<Item> blockItem(RegistryObject<? extends Block> block) {
        RegistryObject<Item> item = MorkovskRegistries.ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties()));
        MorkovskTabs.add(MorkovskTabs.ModTab.STORAGE, item::get);
        return item;
    }

    /** Registers a storage BE; {@code self} is the lazy field reference (RegistryObject
     *  is only assigned after this call returns). */
    private static RegistryObject<BlockEntityType<StorageBlockEntity>> storageBE(
            String name, RegistryObject<Block> block, StorageKind kind,
            Supplier<BlockEntityType<StorageBlockEntity>> self) {
        return MorkovskRegistries.BLOCK_ENTITIES.register(name,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new StorageBlockEntity(self.get(), pos, state, kind),
                        block.get()).build(null));
    }

    private static RegistryObject<MenuType<StorageMenu>> storageMenu(
            String name, StorageKind kind, Supplier<MenuType<StorageMenu>> self) {
        return MorkovskRegistries.MENUS.register(name,
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new StorageMenu(self.get(), kind, id, inv, buf)));
    }

    public static void init() {
        BlockBehaviour.Properties wood = BlockBehaviour.Properties.of()
                .strength(2.0f).sound(SoundType.WOOD);
        BlockBehaviour.Properties metal = BlockBehaviour.Properties.of()
                .strength(3.0f).sound(SoundType.METAL);

        CROP_CRATE = MorkovskRegistries.BLOCKS.register("crop_crate",
                () -> new StorageBlock(wood, () -> CROP_CRATE_BE.get()));
        LARGE_CRATE = MorkovskRegistries.BLOCKS.register("large_crate",
                () -> new StorageBlock(wood, () -> LARGE_CRATE_BE.get()));
        SEED_VAULT = MorkovskRegistries.BLOCKS.register("seed_vault",
                () -> new StorageBlock(wood, () -> SEED_VAULT_BE.get()));
        SILO = MorkovskRegistries.BLOCKS.register("silo",
                () -> new StorageBlock(metal, () -> SILO_BE.get()));
        REFRIGERATED_CHEST = MorkovskRegistries.BLOCKS.register("refrigerated_chest",
                () -> new RefrigeratedChestBlock(metal, () -> REFRIGERATED_BE.get()));

        blockItem(CROP_CRATE);
        blockItem(LARGE_CRATE);
        blockItem(SEED_VAULT);
        blockItem(SILO);
        blockItem(REFRIGERATED_CHEST);

        CROP_CRATE_BE = storageBE("crop_crate", CROP_CRATE, StorageKind.CROP_CRATE,
                () -> CROP_CRATE_BE.get());
        LARGE_CRATE_BE = storageBE("large_crate", LARGE_CRATE, StorageKind.LARGE_CRATE,
                () -> LARGE_CRATE_BE.get());
        SEED_VAULT_BE = storageBE("seed_vault", SEED_VAULT, StorageKind.SEED_VAULT,
                () -> SEED_VAULT_BE.get());
        SILO_BE = storageBE("silo", SILO, StorageKind.SILO,
                () -> SILO_BE.get());
        REFRIGERATED_BE = MorkovskRegistries.BLOCK_ENTITIES.register("refrigerated_chest",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new RefrigeratedChestBlockEntity(REFRIGERATED_BE.get(), pos, state),
                        REFRIGERATED_CHEST.get()).build(null));

        CROP_CRATE_MENU = storageMenu("crop_crate", StorageKind.CROP_CRATE,
                () -> CROP_CRATE_MENU.get());
        LARGE_CRATE_MENU = storageMenu("large_crate", StorageKind.LARGE_CRATE,
                () -> LARGE_CRATE_MENU.get());
        SEED_VAULT_MENU = storageMenu("seed_vault", StorageKind.SEED_VAULT,
                () -> SEED_VAULT_MENU.get());
        SILO_MENU = storageMenu("silo", StorageKind.SILO,
                () -> SILO_MENU.get());
        REFRIGERATED_MENU = MorkovskRegistries.MENUS.register("refrigerated_chest",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new RefrigeratedMenu(REFRIGERATED_MENU.get(), id, inv, buf)));
    }
}
