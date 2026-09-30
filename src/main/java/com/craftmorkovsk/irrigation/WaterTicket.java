package com.craftmorkovsk.irrigation;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.ticket.AABBTicket;

/** Owns one Forge farmland-water ticket for an irrigation block. Created lazily
 *  while the block is active and invalidated when it goes dry, unloads, or is
 *  removed. Server-side only; calls on the client are no-ops. */
final class WaterTicket {

    private AABBTicket ticket;

    void ensure(Level level, AABB area) {
        if (ticket == null && !level.isClientSide) {
            ticket = FarmlandWaterManager.addAABBTicket(level, area);
        }
    }

    void invalidate() {
        if (ticket != null) {
            ticket.invalidate();
            ticket = null;
        }
    }
}
