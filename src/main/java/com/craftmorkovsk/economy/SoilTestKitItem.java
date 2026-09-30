package com.craftmorkovsk.economy;

import com.craftmorkovsk.soil.SoilAPI;
import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Hand-held soil probe: right-click farmland to hear the soil's tier and moisture. */
public class SoilTestKitItem extends Item {

    public SoilTestKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        BlockPos soilPos = ctx.getClickedPos();
        if (!SoilAPI.isFarmlandLike(level.getBlockState(soilPos))) {
            // Clicking the crop on top should still test the soil beneath it.
            BlockPos below = soilPos.below();
            if (SoilAPI.isFarmlandLike(level.getBlockState(below))) {
                soilPos = below;
            }
        }
        BlockState soil = level.getBlockState(soilPos);
        if (!SoilAPI.isFarmlandLike(soil)) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("economy.craftmorkovsk.soil_report_none"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        SoilTier tier = SoilAPI.tierAt(level, soilPos);
        boolean hydrated = SoilAPI.isHydrated(level, soilPos);
        if (player != null) {
            player.displayClientMessage(Component.translatable(
                    "economy.craftmorkovsk.soil_report",
                    Component.translatable("block.craftmorkovsk." + tier.blockName),
                    Component.translatable(hydrated
                            ? "economy.craftmorkovsk.hydrated"
                            : "economy.craftmorkovsk.dry")), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craftmorkovsk.soil_test_kit"));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
