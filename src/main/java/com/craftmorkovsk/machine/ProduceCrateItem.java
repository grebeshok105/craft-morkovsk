package com.craftmorkovsk.machine;

import com.craftmorkovsk.data.Quality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A crate of packed produce. NBT: "Packed" (item id string), "Count" (int),
 *  "Quality" (int, optional). The contents show in the hover text, and right-clicking
 *  with the crate unpacks it back into the produce stack. */
public class ProduceCrateItem extends Item {

    public ProduceCrateItem(Properties properties) {
        super(properties);
    }

    /** Writes the packed-produce NBT onto a fresh crate stack. */
    public static void pack(ItemStack crate, ItemStack produce, int count) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(produce.getItem());
        if (id == null) return;
        CompoundTag tag = crate.getOrCreateTag();
        tag.putString("Packed", id.toString());
        tag.putInt("Count", count);
        Quality quality = Quality.of(produce);
        if (quality != Quality.NORMAL) tag.putInt("Quality", quality.index);
    }

    @Nullable
    private static Item packedItem(ItemStack crate) {
        CompoundTag tag = crate.getTag();
        if (tag == null || !tag.contains("Packed")) return null;
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("Packed")));
    }

    /** Rebuilds the produce stack stored in this crate (or EMPTY when not packed). */
    public static ItemStack unpack(ItemStack crate) {
        Item item = packedItem(crate);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return ItemStack.EMPTY;
        int count = crate.getOrCreateTag().getInt("Count");
        ItemStack produce = new ItemStack(item, Math.max(1, count));
        CompoundTag tag = crate.getTag();
        if (tag != null && tag.contains("Quality")) {
            int q = tag.getInt("Quality");
            if (q >= 0 && q < Quality.values().length) Quality.apply(produce, Quality.values()[q]);
        }
        return produce;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack crate = player.getItemInHand(hand);
        ItemStack produce = unpack(crate);
        if (produce.isEmpty()) return InteractionResultHolder.pass(crate);
        if (!level.isClientSide) {
            crate.shrink(1);
            if (!player.getInventory().add(produce)) player.drop(produce, false);
        }
        return InteractionResultHolder.sidedSuccess(crate, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Item packed = packedItem(stack);
        if (packed != null && packed != net.minecraft.world.item.Items.AIR) {
            int count = stack.getOrCreateTag().getInt("Count");
            tooltip.add(Component.translatable("tooltip.craftmorkovsk.produce_crate.packed",
                    count, Component.translatable(packed.getDescriptionId())));
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Quality")) {
                int q = tag.getInt("Quality");
                if (q >= 0 && q < Quality.values().length) {
                    tooltip.add(Component.translatable("tooltip.craftmorkovsk.produce_crate.quality",
                            Quality.values()[q].displayName()));
                }
            }
        } else {
            tooltip.add(Component.translatable("tooltip.craftmorkovsk.produce_crate.empty"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
