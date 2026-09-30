package com.craftmorkovsk.data;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Grants a custom advancement programmatically. The advancement JSON should use a single
 *  criterion named "grant" with trigger minecraft:impossible (or any trigger name). */
public final class Award {

    private Award() {}

    public static void grant(ServerPlayer player, String path) {
        Advancement adv = player.server.getAdvancements()
                .getAdvancement(new ResourceLocation(CraftMorkovsk.MOD_ID, path));
        if (adv == null) return;
        if (!player.getAdvancements().getOrStartProgress(adv).isDone()) {
            player.getAdvancements().award(adv, "grant");
        }
    }
}
