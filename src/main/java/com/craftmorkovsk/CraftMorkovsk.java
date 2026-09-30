package com.craftmorkovsk;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.registry.MorkovskRegistries;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CraftMorkovsk.MOD_ID)
public final class CraftMorkovsk {
    public static final String MOD_ID = "craftmorkovsk";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CraftMorkovsk() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MorkovskConfig.register();
        MorkovskRegistries.registerAll(modBus);
        MorkovskModules.init();
        LOGGER.info("Craft Morkovsk loaded - treating carrot farming with unreasonable seriousness");
    }
}
