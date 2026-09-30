package com.craftmorkovsk.storage;

import com.craftmorkovsk.data.Quality;
import com.craftmorkovsk.item.QualityProduceItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Refrigerated chest inventory: while the fridge is powered, produce extracted
 *  through ANY path (cursor pick, shift-click, hopper pull via the item capability)
 *  gains one quality tier, capped at PERFECT. Upgrade targets only
 *  {@link QualityProduceItem} — produce, not prepared foods. */
public class ChilledItemHandler extends ItemStackHandler {

    private final BooleanSupplier powered;
    private final Runnable onChange;

    public ChilledItemHandler(int size, BooleanSupplier powered, Runnable onChange) {
        super(size);
        this.powered = powered;
        this.onChange = onChange;
    }

    @Override
    protected void onContentsChanged(int slot) {
        onChange.run();
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack extracted = super.extractItem(slot, amount, simulate);
        if (!simulate && powered.getAsBoolean() && extracted.getItem() instanceof QualityProduceItem) {
            Quality q = Quality.of(extracted);
            if (q.ordinal() < Quality.PERFECT.ordinal()) {
                Quality.apply(extracted, Quality.values()[q.ordinal() + 1]);
            }
        }
        return extracted;
    }
}
