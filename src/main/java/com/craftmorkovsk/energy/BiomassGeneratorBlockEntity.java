package com.craftmorkovsk.energy;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.config.MorkovskConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Biomass generator: burns plant matter (tag craftmorkovsk:biomass_fuel) instead of solid fuel. */
public class BiomassGeneratorBlockEntity extends GeneratorBlockEntity {

    public static final TagKey<Item> BIOMASS_FUEL =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "biomass_fuel"));

    private static final int BIOMASS_BURN_TICKS = 800;

    public BiomassGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected int fePerTick() { return MorkovskConfig.BIOMASS_FE_PER_TICK.get(); }

    @Override
    protected boolean isFuel(ItemStack stack) {
        return stack.is(BIOMASS_FUEL);
    }

    @Override
    protected int burnTimeOf(ItemStack stack) {
        return BIOMASS_BURN_TICKS;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new FuelMachineMenu(EnergyModule.BIOMASS_MENU.get(), windowId, playerInventory,
                getItems(), getContainerData(), ContainerLevelAccess.create(level, worldPosition));
    }
}
