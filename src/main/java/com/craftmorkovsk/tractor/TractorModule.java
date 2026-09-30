package com.craftmorkovsk.tractor;

import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

/** Rideable field tractor + plow/planter/harvester/spreader attachments. */
public final class TractorModule {

    public static RegistryObject<EntityType<TractorEntity>> TRACTOR;
    public static RegistryObject<Item> TRACTOR_ITEM;
    public static RegistryObject<Item> ATTACHMENT_PLOW;
    public static RegistryObject<Item> ATTACHMENT_PLANTER;
    public static RegistryObject<Item> ATTACHMENT_HARVESTER;
    public static RegistryObject<Item> ATTACHMENT_SPREADER;
    public static RegistryObject<MenuType<TractorMenu>> TRACTOR_MENU;

    private TractorModule() {}

    public static void init() {
        TRACTOR = MorkovskRegistries.ENTITIES.register("tractor",
                () -> EntityType.Builder.of(TractorEntity::new, MobCategory.MISC)
                        .sized(1.5f, 1.7f)
                        .clientTrackingRange(10)
                        .build("tractor"));

        TRACTOR_ITEM = MorkovskRegistries.ITEMS.register("tractor_item",
                () -> new TractorItem(new Item.Properties().stacksTo(1)));
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, TRACTOR_ITEM::get);

        ATTACHMENT_PLOW = MorkovskRegistries.ITEMS.register("attachment_plow",
                () -> new AttachmentItem(new Item.Properties().stacksTo(1), AttachmentItem.Kind.PLOW));
        ATTACHMENT_PLANTER = MorkovskRegistries.ITEMS.register("attachment_planter",
                () -> new AttachmentItem(new Item.Properties().stacksTo(1), AttachmentItem.Kind.PLANTER));
        ATTACHMENT_HARVESTER = MorkovskRegistries.ITEMS.register("attachment_harvester",
                () -> new AttachmentItem(new Item.Properties().stacksTo(1), AttachmentItem.Kind.HARVESTER));
        ATTACHMENT_SPREADER = MorkovskRegistries.ITEMS.register("attachment_spreader",
                () -> new AttachmentItem(new Item.Properties().stacksTo(1), AttachmentItem.Kind.SPREADER));
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, ATTACHMENT_PLOW::get);
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, ATTACHMENT_PLANTER::get);
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, ATTACHMENT_HARVESTER::get);
        MorkovskTabs.add(MorkovskTabs.ModTab.MACHINES, ATTACHMENT_SPREADER::get);

        TRACTOR_MENU = MorkovskRegistries.MENUS.register("tractor",
                () -> IForgeMenuType.create((id, inv, buf) ->
                        new TractorMenu(TRACTOR_MENU.get(), id, inv, buf.readVarInt())));
    }
}
