package com.craftmorkovsk.irrigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Sprinkler head. Fed water through pipes; hydrates farmland in its radius.
 *  The advanced variant has a larger radius and a bigger internal buffer. */
public class SprinklerBlock extends Block implements EntityBlock {

    private final boolean advanced;
    private final java.util.function.Supplier<BlockEntityType<? extends SprinklerBlockEntity>> beType;

    public SprinklerBlock(Properties properties, boolean advanced,
                          java.util.function.Supplier<BlockEntityType<? extends SprinklerBlockEntity>> beType) {
        super(properties);
        this.advanced = advanced;
        this.beType = beType;
    }

    public boolean isAdvanced() { return advanced; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return beType.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return (l, pos, s, be) -> {
            if (be instanceof SprinklerBlockEntity sprinkler) SprinklerBlockEntity.tick(l, pos, s, sprinkler);
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (placer instanceof Player player
                && level.getBlockEntity(pos) instanceof SprinklerBlockEntity sprinkler) {
            sprinkler.setPlacer(player.getUUID());
        }
    }
}
