package com.craftmorkovsk.animal;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge-bus hooks for the animal package: first product collection grants the
 *  {@code animal_keeper} advancement and a bit of breeding XP. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AnimalEvents {

    private AnimalEvents() {}

    private static boolean isAnimalProduct(ItemStack stack) {
        return stack.is(AnimalModule.DUCK_EGG.get())
                || stack.is(AnimalModule.TURKEY_EGG.get())
                || stack.is(AnimalModule.GOAT_MILK_BUCKET.get())
                || stack.is(AnimalModule.MANURE.get());
    }

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isAnimalProduct(event.getStack())) return;
        FarmingStats.awardXp(player, FarmingStats.XpReason.BREEDING, 1);
        Award.grant(player, "animal_keeper");
    }
}
