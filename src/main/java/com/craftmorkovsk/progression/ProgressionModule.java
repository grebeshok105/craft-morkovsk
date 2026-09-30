package com.craftmorkovsk.progression;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.registry.MorkovskTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

/** Progression package entry point: agricultural handbook item + mod sound events.
 *  Commands (FORGE bus) and client wiring (MOD bus, Dist.CLIENT) live in their own
 *  subscribers — see MorkovskCommands, FarmingEvents and client/ProgressionClientInit. */
public final class ProgressionModule {

    public static RegistryObject<Item> AGRICULTURAL_HANDBOOK;

    public static RegistryObject<SoundEvent> SOUND_CROP_HARVEST;
    public static RegistryObject<SoundEvent> SOUND_LEVEL_UP;
    public static RegistryObject<SoundEvent> SOUND_MACHINE_HUM;
    public static RegistryObject<SoundEvent> SOUND_TRACTOR_ENGINE;
    public static RegistryObject<SoundEvent> SOUND_HANDBOOK_OPEN;

    private ProgressionModule() {}

    private static RegistryObject<SoundEvent> sound(String id) {
        return MorkovskRegistries.SOUNDS.register(id,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(CraftMorkovsk.MOD_ID, id)));
    }

    public static void init() {
        AGRICULTURAL_HANDBOOK = MorkovskRegistries.ITEMS.register("agricultural_handbook",
                () -> new HandbookItem(new Item.Properties().stacksTo(1)));
        MorkovskTabs.add(MorkovskTabs.ModTab.MISC, AGRICULTURAL_HANDBOOK::get);

        SOUND_CROP_HARVEST = sound("crop.harvest");
        SOUND_LEVEL_UP = sound("level.up");
        SOUND_MACHINE_HUM = sound("machine.hum");
        SOUND_TRACTOR_ENGINE = sound("tractor.engine");
        SOUND_HANDBOOK_OPEN = sound("handbook.open");
    }
}
