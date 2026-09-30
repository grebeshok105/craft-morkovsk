package com.craftmorkovsk.tractor;

import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.crop.MorkovskCropBlock;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.fertilizer.FertilizerItem;
import com.craftmorkovsk.soil.MorkovskSoilBlock;
import com.craftmorkovsk.soil.SoilAPI;
import com.craftmorkovsk.soil.SoilModule;
import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Rideable field tractor. Right-click mounts; sneak-right-click opens the cargo menu;
 *  right-click with coal/charcoal/plant_oil refuels, with an attachment item installs it.
 *  While a fueled driver steers (WASD), the installed attachment acts on the block under
 *  and ahead of the tractor every {@value #ATTACHMENT_PERIOD} ticks. */
public class TractorEntity extends Entity implements MenuProvider {

    private static final EntityDataAccessor<Integer> DATA_ID_HURT =
            SynchedEntityData.defineId(TractorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR =
            SynchedEntityData.defineId(TractorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE =
            SynchedEntityData.defineId(TractorEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_FUEL =
            SynchedEntityData.defineId(TractorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_ATTACHMENT =
            SynchedEntityData.defineId(TractorEntity.class, EntityDataSerializers.INT);

    private static final int ATTACHMENT_PERIOD = 10;
    private static final int CARGO_SLOTS = 9;
    private static final float MOVE_SPEED = 0.22f;
    private static final float TURN_SPEED = 2.5f;

    private static final int FUEL_COAL = 160;
    private static final int FUEL_PLANT_OIL = 800;
    private static final ResourceLocation PLANT_OIL_ID = new ResourceLocation("craftmorkovsk", "plant_oil");

    public static final int NO_ATTACHMENT = -1;

    private final ItemStackHandler cargo = new ItemStackHandler(CARGO_SLOTS);
    private final ItemStackHandler attachmentSlot = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof AttachmentItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            syncAttachmentId();
        }
    };

    private LazyOptional<IItemHandler> itemCap;
    private int movingTicks;

    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private int lerpSteps;

    public TractorEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0f);
        this.entityData.set(DATA_ID_HURTDIR, 1);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, 0.0f);
        this.entityData.define(DATA_ID_FUEL, 0);
        this.entityData.define(DATA_ID_ATTACHMENT, NO_ATTACHMENT);
    }

    // ---- fuel ----

    public int getFuel() {
        return this.entityData.get(DATA_ID_FUEL);
    }

    public int getMaxFuel() {
        return MorkovskConfig.TRACTOR_MAX_FUEL.get();
    }

    private void setFuel(int value) {
        this.entityData.set(DATA_ID_FUEL, Mth.clamp(value, 0, getMaxFuel()));
    }

    private static int fuelValueOf(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.COAL || item == Items.CHARCOAL) return FUEL_COAL;
        if (isPlantOil(item)) return FUEL_PLANT_OIL;
        return 0;
    }

    /** plant_oil is registered by the machines package; resolved by registry key so this
     *  package has no compile-time dependency on it. */
    private static boolean isPlantOil(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return PLANT_OIL_ID.equals(key);
    }

    // ---- attachment ----

    /** -1 = none, else {@link AttachmentItem.Kind#id}. */
    public int getAttachmentId() {
        return this.entityData.get(DATA_ID_ATTACHMENT);
    }

    @Nullable
    public AttachmentItem.Kind getAttachment() {
        return AttachmentItem.Kind.byId(getAttachmentId());
    }

    private void syncAttachmentId() {
        ItemStack stack = this.attachmentSlot.getStackInSlot(0);
        int id = stack.getItem() instanceof AttachmentItem att ? att.kind().id : NO_ATTACHMENT;
        this.entityData.set(DATA_ID_ATTACHMENT, id);
    }

    public ItemStackHandler getCargo() {
        return this.cargo;
    }

    public ItemStackHandler getAttachmentSlot() {
        return this.attachmentSlot;
    }

    // ---- interaction ----

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            if (!this.level().isClientSide && player instanceof ServerPlayer sp) {
                NetworkHooks.openScreen(sp, this, buf -> buf.writeVarInt(this.getId()));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof AttachmentItem) {
            if (!this.level().isClientSide) {
                ItemStack previous = this.attachmentSlot.getStackInSlot(0);
                this.attachmentSlot.setStackInSlot(0, held.copyWithCount(1));
                if (!previous.isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, previous);
                if (!player.getAbilities().instabuild) held.shrink(1);
                this.playSound(SoundEvents.PISTON_EXTEND, 0.8f, 1.2f);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        int fuelValue = fuelValueOf(held);
        if (fuelValue > 0 && this.getFuel() < this.getMaxFuel()) {
            if (!this.level().isClientSide) {
                this.setFuel(this.getFuel() + fuelValue);
                if (!player.getAbilities().instabuild) held.shrink(1);
                this.playSound(SoundEvents.BUCKET_EMPTY_LAVA, 0.6f, 1.4f);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (player.isPassenger() || this.isVehicle()) return InteractionResult.PASS;
        if (!this.level().isClientSide) {
            boolean mounted = player.startRiding(this);
            if (mounted) {
                if (player instanceof ServerPlayer sp) Award.grant(sp, "tractor_driver");
                this.playSound(SoundEvents.MINECART_RIDING, 0.7f, 0.9f);
            }
            return mounted ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    // ---- riding ----

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && passenger instanceof Player;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        return first instanceof LivingEntity living ? living : null;
    }

    @Override
    protected boolean canRide(Entity vehicle) {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.05;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return !(other instanceof TractorEntity) && super.canCollideWith(other);
    }

    // ---- damage ----

    public void setHurtTime(int t) { this.entityData.set(DATA_ID_HURT, t); }
    public void setHurtDir(int dir) { this.entityData.set(DATA_ID_HURTDIR, dir); }
    public void setDamage(float damage) { this.entityData.set(DATA_ID_DAMAGE, damage); }
    public float getDamage() { return this.entityData.get(DATA_ID_DAMAGE); }
    public int getHurtTime() { return this.entityData.get(DATA_ID_HURT); }
    public int getHurtDir() { return this.entityData.get(DATA_ID_HURTDIR); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) return false;
        if (this.level().isClientSide || this.isRemoved()) return true;
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + amount * 10.0f);
        this.markHurt();
        boolean creative = source.getEntity() instanceof Player p && p.getAbilities().instabuild;
        if (creative || this.getDamage() > 40.0f) {
            this.ejectPassengers();
            if (!creative) {
                this.spawnAtLocation(new ItemStack(TractorModule.TRACTOR_ITEM.get()));
                this.dropContents();
            }
            this.discard();
        }
        return true;
    }

    private void dropContents() {
        for (int i = 0; i < this.cargo.getSlots(); i++) {
            ItemStack stack = this.cargo.getStackInSlot(i);
            if (!stack.isEmpty()) this.spawnAtLocation(stack);
        }
        ItemStack attachment = this.attachmentSlot.getStackInSlot(0);
        if (!attachment.isEmpty()) this.spawnAtLocation(attachment);
    }

    // ---- tick ----

    @Override
    public void tick() {
        super.tick();
        if (this.getHurtTime() > 0) this.setHurtTime(this.getHurtTime() - 1);
        if (this.getDamage() > 0) this.setDamage(this.getDamage() - 1.0f);

        this.tickLerp();

        if (this.level().isClientSide) {
            if (this.isControlledByLocalInstance()) this.drive();
            this.clientEffects();
        } else {
            this.drive();
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.checkInsideBlocks();
    }

    private void drive() {
        Vec3 motion = this.getDeltaMovement();
        LivingEntity driver = this.getControllingPassenger();
        if (driver instanceof Player player && this.getFuel() > 0) {
            this.setYRot(this.getYRot() - player.xxa * TURN_SPEED);
            float input = player.zza;
            Vec3 forward = Vec3.directionFromRotation(0, this.getYRot());
            double speed = input * MOVE_SPEED;
            motion = new Vec3(forward.x * speed, motion.y, forward.z * speed);
            if (!this.level().isClientSide) {
                if (Math.abs(input) > 0.01f) {
                    this.setFuel(this.getFuel() - MorkovskConfig.TRACTOR_FUEL_PER_TICK.get());
                    this.movingTicks++;
                    if (this.movingTicks % ATTACHMENT_PERIOD == 0) this.actAttachment();
                    if (this.tickCount % 45 == 0) {
                        this.playSound(SoundEvents.MINECART_RIDING, 0.25f, 1.1f);
                    }
                } else {
                    this.movingTicks = 0;
                }
            }
        } else {
            this.movingTicks = 0;
            motion = new Vec3(motion.x * 0.7, motion.y, motion.z * 0.7);
        }
        if (!this.onGround()) motion = motion.add(0.0, -0.08, 0.0);
        this.setDeltaMovement(motion);
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps > 0) {
            double x = this.getX() + (this.lerpX - this.getX()) / this.lerpSteps;
            double y = this.getY() + (this.lerpY - this.getY()) / this.lerpSteps;
            double z = this.getZ() + (this.lerpZ - this.getZ()) / this.lerpSteps;
            double dRot = Mth.wrapDegrees(this.lerpYRot - this.getYRot());
            this.setYRot(this.getYRot() + (float) (dRot / this.lerpSteps));
            this.setXRot(this.getXRot() + (float) (this.lerpXRot - this.getXRot()) / this.lerpSteps);
            this.lerpSteps--;
            this.setPos(x, y, z);
            this.setRot(this.getYRot(), this.getXRot());
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 10;
    }

    private void clientEffects() {
        if (this.getControllingPassenger() != null
                && this.getDeltaMovement().horizontalDistanceSqr() > 0.0004
                && this.random.nextInt(3) == 0) {
            Vec3 forward = Vec3.directionFromRotation(0, this.getYRot());
            Vec3 left = new Vec3(forward.z, 0, -forward.x);
            double x = this.getX() + forward.x * 0.7 + left.x * 0.35;
            double y = this.getY() + 1.55;
            double z = this.getZ() + forward.z * 0.7 + left.z * 0.35;
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.03, 0.0);
        }
    }

    // ---- attachments ----

    private void actAttachment() {
        AttachmentItem.Kind kind = this.getAttachment();
        if (kind == null) return;
        BlockPos under = this.getOnPos();
        Vec3 forward = Vec3.directionFromRotation(0, this.getYRot());
        BlockPos front = BlockPos.containing(
                this.getX() + forward.x * 1.4, under.getY() + 0.5, this.getZ() + forward.z * 1.4);
        switch (kind) {
            case PLOW -> this.plowAt(under, front);
            case PLANTER -> this.plantAt(under.above(), front.above());
            case HARVESTER -> this.harvestAt(under.above(), front.above());
            case SPREADER -> this.spreadAt(under, front);
        }
    }

    private void plowAt(BlockPos... soilPositions) {
        boolean acted = false;
        for (BlockPos pos : soilPositions) {
            BlockState state = this.level().getBlockState(pos);
            boolean tillable = state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK)
                    || state.is(Blocks.DIRT_PATH) || state.getBlock() instanceof FarmBlock
                    || (state.getBlock() instanceof MorkovskSoilBlock soil && soil.tier() == SoilTier.EXHAUSTED);
            if (!tillable) continue;
            BlockState farmland = SoilModule.BLOCKS_BY_TIER.get(SoilTier.NORMAL).get().defaultBlockState();
            if (state.hasProperty(FarmBlock.MOISTURE) && farmland.hasProperty(FarmBlock.MOISTURE)) {
                farmland = farmland.setValue(FarmBlock.MOISTURE, state.getValue(FarmBlock.MOISTURE));
            }
            this.level().setBlock(pos, farmland, 3);
            acted = true;
        }
        if (acted) this.playSound(SoundEvents.HOE_TILL, 0.7f, 1.0f);
    }

    private void plantAt(BlockPos... cropPositions) {
        for (BlockPos pos : cropPositions) {
            if (!this.level().getBlockState(pos).isAir()) continue;
            BlockPos soilPos = pos.below();
            BlockState soil = this.level().getBlockState(soilPos);
            if (!SoilAPI.isFarmlandLike(soil) || !SoilAPI.isHydrated(this.level(), soilPos)) continue;
            int slot = this.findSeedSlot();
            if (slot < 0) return;
            ItemStack seeds = this.cargo.getStackInSlot(slot);
            if (!(seeds.getItem() instanceof ItemNameBlockItem blockItem
                    && blockItem.getBlock() instanceof MorkovskCropBlock crop)) continue;
            BlockState cropState = crop.defaultBlockState();
            if (!crop.canSurvive(cropState, this.level(), pos)) continue;
            this.level().setBlock(pos, cropState, 3);
            this.cargo.extractItem(slot, 1, false);
            this.playSound(SoundEvents.CROP_PLANTED, 0.7f, 1.0f);
        }
    }

    private void harvestAt(BlockPos... cropPositions) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        for (BlockPos pos : cropPositions) {
            BlockState state = this.level().getBlockState(pos);
            if (!(state.getBlock() instanceof MorkovskCropBlock crop) || !crop.isMaxAge(state)) continue;
            LivingEntity driver = this.getControllingPassenger();
            LootParams.Builder params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                    .withParameter(LootContextParams.THIS_ENTITY, driver != null ? driver : this);
            for (ItemStack drop : crop.getDrops(state, params)) {
                ItemStack rest = ItemHandlerHelper.insertItemStacked(this.cargo, drop, false);
                if (!rest.isEmpty()) this.spawnAtLocation(rest);
            }
            SoilAPI.degrade(this.level(), pos.below(), this.random);
            this.level().setBlock(pos, crop.getStateForAge(0), 2);
            this.playSound(SoundEvents.GRASS_BREAK, 0.8f, 1.0f);
        }
    }

    private void spreadAt(BlockPos... soilPositions) {
        for (BlockPos pos : soilPositions) {
            BlockState state = this.level().getBlockState(pos);
            if (!SoilAPI.isFarmlandLike(state)
                    && !(state.getBlock() instanceof MorkovskSoilBlock)
                    && !state.is(Blocks.DIRT) && !state.is(Blocks.GRASS_BLOCK)) continue;
            int slot = this.findFertilizerSlot();
            if (slot < 0) return;
            ItemStack fertilizer = this.cargo.getStackInSlot(slot);
            if (!(fertilizer.getItem() instanceof FertilizerItem fert)) continue;
            SoilTier current = SoilAPI.tierAt(this.level(), pos);
            if (current.rank >= fert.potency().target.rank) continue;
            SoilAPI.setTier(this.level(), pos, fert.potency().target);
            this.cargo.extractItem(slot, 1, false);
            this.playSound(SoundEvents.BONE_MEAL_USE, 0.8f, 1.1f);
        }
    }

    private int findSeedSlot() {
        for (int i = 0; i < this.cargo.getSlots(); i++) {
            ItemStack stack = this.cargo.getStackInSlot(i);
            if (stack.getItem() instanceof ItemNameBlockItem blockItem
                    && blockItem.getBlock() instanceof MorkovskCropBlock) return i;
        }
        return -1;
    }

    private int findFertilizerSlot() {
        for (int i = 0; i < this.cargo.getSlots(); i++) {
            if (this.cargo.getStackInSlot(i).getItem() instanceof FertilizerItem) return i;
        }
        return -1;
    }

    // ---- persistence / capability ----

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Fuel", this.getFuel());
        tag.put("Cargo", this.cargo.serializeNBT());
        tag.put("Attachment", this.attachmentSlot.serializeNBT());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setFuel(tag.getInt("Fuel"));
        this.cargo.deserializeNBT(tag.getCompound("Cargo"));
        this.attachmentSlot.deserializeNBT(tag.getCompound("Attachment"));
        this.syncAttachmentId();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (this.itemCap == null) this.itemCap = LazyOptional.of(() -> this.cargo);
            return this.itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (this.itemCap != null) this.itemCap.invalidate();
    }

    // ---- menu ----

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.craftmorkovsk.tractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new TractorMenu(TractorModule.TRACTOR_MENU.get(), windowId, playerInventory, this);
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TractorModule.TRACTOR_ITEM.get());
    }
}
