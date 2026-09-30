package com.craftmorkovsk.item;

import com.craftmorkovsk.data.Quality;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Produce item carrying a Quality NBT tier; shown in the name and tooltip. */
public class QualityProduceItem extends Item {

    public QualityProduceItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Quality q = Quality.of(stack);
        if (q == Quality.NORMAL) return super.getName(stack);
        return q.displayName().copy().append(" ").append(super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Quality q = Quality.of(stack);
        if (q != Quality.NORMAL) {
            tooltip.add(Component.translatable("tooltip.craftmorkovsk.quality", q.displayName()));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
