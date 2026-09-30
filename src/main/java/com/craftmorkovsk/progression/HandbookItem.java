package com.craftmorkovsk.progression;

import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.progression.client.HandbookClientHooks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Right-click opens the paged handbook GUI (client only) and grants the
 *  "bookworm" advancement the first time on the server. */
public class HandbookItem extends Item {

    public HandbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            Award.grant(serverPlayer, "bookworm");
            level.playSound(null, player.blockPosition(),
                    ProgressionModule.SOUND_HANDBOOK_OPEN.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> HandbookClientHooks::openHandbook);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
