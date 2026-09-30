package com.craftmorkovsk.progression;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.crop.MorkovskCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side progression flavor: breaking a fully grown Morkovsk crop bursts
 *  happy-villager particles and plays our crop.harvest sound at the plant. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FarmingEvents {

    private FarmingEvents() {}

    @SubscribeEvent
    public static void onCropBreak(BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof MorkovskCropBlock crop) || !crop.isMaxAge(state)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                8, 0.25, 0.3, 0.25, 0.02);
        level.playSound(null, pos, ProgressionModule.SOUND_CROP_HARVEST.get(),
                SoundSource.BLOCKS, 0.7f, 1.0f + level.random.nextFloat() * 0.2f);
    }
}
