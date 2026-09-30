package com.craftmorkovsk;

import com.craftmorkovsk.crop.CropModule;
import com.craftmorkovsk.energy.EnergyModule;
import com.craftmorkovsk.fertilizer.FertilizerModule;
import com.craftmorkovsk.machine.framework.ProcessingRecipe;
import com.craftmorkovsk.network.MorkovskNet;
import com.craftmorkovsk.network.SyncFarmingPacket;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.craftmorkovsk.soil.SoilModule;

/** Composition root: every subsystem registers its content here during mod construction.
 *  Sub-sessions append exactly one line per module in the marked region — order is fixed
 *  so network packet ids stay aligned between client and server. */
public final class MorkovskModules {

    private MorkovskModules() {}

    public static void init() {
        // Core (orchestrator-owned) systems.
        MorkovskNet.registerS2C(SyncFarmingPacket.class, SyncFarmingPacket::handle,
                SyncFarmingPacket::encode, SyncFarmingPacket::decode);
        MorkovskRegistries.RECIPE_TYPES.register(ProcessingRecipe.TYPE_ID, () -> ProcessingRecipe.Type.INSTANCE);
        MorkovskRegistries.RECIPE_SERIALIZERS.register(ProcessingRecipe.TYPE_ID, () -> ProcessingRecipe.Serializer.INSTANCE);

        SoilModule.init();
        FertilizerModule.init();
        CropModule.init();
        EnergyModule.init();

        // ==== SUBSYSTEM MODULES — one line each, append below (orchestrator wires at merge) ====
        com.craftmorkovsk.food.FoodModule.init();
    }
}
