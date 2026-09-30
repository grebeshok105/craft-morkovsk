package com.craftmorkovsk.data;

import com.craftmorkovsk.CraftMorkovsk;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Attaches, persists and syncs {@link PlayerFarmingData} on players. Forge bus events. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FarmingCapabilityEvents {

    private static final ResourceLocation CAP_ID = new ResourceLocation(CraftMorkovsk.MOD_ID, "farming");

    private FarmingCapabilityEvents() {}

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        PlayerFarmingData data = new PlayerFarmingData();
        event.addCapability(CAP_ID, new ICapabilitySerializable<CompoundTag>() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return cap == PlayerFarmingData.CAPABILITY
                        ? PlayerFarmingData.CAPABILITY.orEmpty(cap, LazyOptional.of(() -> data))
                        : LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT() {
                return data.serializeNBT();
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                data.deserializeNBT(tag);
            }
        });
    }

    /** Persist across death/respawn (End return clones the player). */
    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        LazyOptional<PlayerFarmingData> oldCap = event.getOriginal().getCapability(PlayerFarmingData.CAPABILITY);
        LazyOptional<PlayerFarmingData> newCap = event.getEntity().getCapability(PlayerFarmingData.CAPABILITY);
        newCap.ifPresent(n -> oldCap.ifPresent(o -> n.copyFrom(o)));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) FarmingStats.sync(sp);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) FarmingStats.sync(sp);
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) FarmingStats.sync(sp);
    }
}
