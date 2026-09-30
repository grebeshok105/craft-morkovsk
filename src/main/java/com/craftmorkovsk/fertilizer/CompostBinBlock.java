package com.craftmorkovsk.fertilizer;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.resources.ResourceLocation;

/** Compost bin: fill with plant matter (LEVEL 0-5), it ferments to READY(6-7) over time,
 *  then right-click collects basic compost. A real system, not a crafting recipe. */
public class CompostBinBlock extends Block {

    public static final int MAX_FILL = 6;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_FILL + 1);
    public static final TagKey<Item> COMPOSTABLE =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "compostable"));
    public static final TagKey<Item> COMPOSTABLE_RICH =
            ItemTags.create(new ResourceLocation(CraftMorkovsk.MOD_ID, "compostable_rich"));

    private final java.util.function.Supplier<ItemLike> compostItem;

    public CompostBinBlock(Properties properties, java.util.function.Supplier<ItemLike> compostItem) {
        super(properties);
        this.compostItem = compostItem;
        registerDefaultState(defaultBlockState().setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        int fill = state.getValue(LEVEL);
        ItemStack held = player.getItemInHand(hand);
        if (fill == MAX_FILL + 1) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LEVEL, 0), 3);
                ItemStack compost = new ItemStack(compostItem.get(), 1 + level.random.nextInt(2));
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0,
                        pos.getZ() + 0.5, compost);
                level.addFreshEntity(entity);
                level.playSound(null, pos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1f, 1f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(COMPOSTABLE) || held.is(COMPOSTABLE_RICH)) {
            if (!level.isClientSide) {
                int add = held.is(COMPOSTABLE_RICH) ? 2 : 1;
                int next = Math.min(fill + add, MAX_FILL);
                level.setBlock(pos, state.setValue(LEVEL, next), 3);
                level.playSound(null, pos, SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 0.8f, 1f);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LEVEL) == MAX_FILL;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) == MAX_FILL && random.nextFloat() < 0.35f) {
            level.setBlock(pos, state.setValue(LEVEL, MAX_FILL + 1), 3);
            level.playSound(null, pos, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 0.6f, 1f);
        }
    }
}
