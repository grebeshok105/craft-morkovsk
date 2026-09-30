package com.craftmorkovsk.registry;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/** Creative tabs. Feature packages contribute items via {@link #add(ModTab, Supplier)} during
 *  module init; the tab display lists are built lazily by Forge after all registration. */
public final class MorkovskTabs {

    public enum ModTab { CROPS, MACHINES, FOOD, STORAGE, MISC }

    private static final EnumMap<ModTab, List<Supplier<Item>>> CONTENTS = new EnumMap<>(ModTab.class);
    static {
        for (ModTab t : ModTab.values()) CONTENTS.put(t, new ArrayList<>());
    }

    public static final RegistryObject<CreativeModeTab> CROPS = tab("crops", ModTab.CROPS);
    public static final RegistryObject<CreativeModeTab> MACHINES = tab("machines", ModTab.MACHINES);
    public static final RegistryObject<CreativeModeTab> FOOD = tab("food", ModTab.FOOD);
    public static final RegistryObject<CreativeModeTab> STORAGE = tab("storage", ModTab.STORAGE);
    public static final RegistryObject<CreativeModeTab> MISC = tab("misc", ModTab.MISC);

    private static RegistryObject<CreativeModeTab> tab(String name, ModTab group) {
        return MorkovskRegistries.CREATIVE_TABS.register(name, () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.craftmorkovsk." + name))
                .icon(() -> new ItemStack(CONTENTS.get(group).isEmpty()
                        ? net.minecraft.world.item.Items.CARROT
                        : CONTENTS.get(group).get(0).get()))
                .displayItems((params, out) -> {
                    for (Supplier<Item> s : CONTENTS.get(group)) out.accept(s.get());
                })
                .build());
    }

    /** Called by feature packages during module init to expose an item in a tab. */
    public static void add(ModTab tab, Supplier<Item> item) {
        CONTENTS.get(tab).add(item);
    }

    private MorkovskTabs() {}
}
