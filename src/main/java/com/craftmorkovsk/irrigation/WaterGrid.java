package com.craftmorkovsk.irrigation;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import java.util.HashMap;
import java.util.Map;

/** Server-side registry of soil positions currently watered by irrigation blocks.
 *  Sprinklers/channels mark their covered area while active and unmark when they
 *  run dry, unload, or are removed. Consulted through FarmingHooks.isIrrigated by
 *  the shared soil/crop code. Positions carry a refcount so overlapping sources
 *  do not erase each other's marks. */
public final class WaterGrid {

    private static final Map<ResourceKey<Level>, Map<BlockPos, LongSet>> BY_SOURCE = new HashMap<>();
    private static final Map<ResourceKey<Level>, Long2IntOpenHashMap> COUNTS = new HashMap<>();

    private WaterGrid() {}

    /** Registers `area` (packed BlockPos longs) as watered by `source` in `level`.
     *  Idempotent per source: re-marking replaces the source's previous area. */
    public static void mark(Level level, BlockPos source, LongSet area) {
        if (level.isClientSide) return;
        ResourceKey<Level> dim = level.dimension();
        Map<BlockPos, LongSet> bySource = BY_SOURCE.computeIfAbsent(dim, k -> new HashMap<>());
        Long2IntOpenHashMap counts = COUNTS.computeIfAbsent(dim, k -> new Long2IntOpenHashMap());
        LongSet prev = bySource.put(source.immutable(), area);
        if (prev != null) decrement(counts, prev);
        LongIterator it = area.iterator();
        while (it.hasNext()) counts.addTo(it.nextLong(), 1);
    }

    /** Removes everything `source` had marked. Safe to call when nothing is marked. */
    public static void unmark(Level level, BlockPos source) {
        if (level == null || level.isClientSide) return;
        Map<BlockPos, LongSet> bySource = BY_SOURCE.get(level.dimension());
        if (bySource == null) return;
        LongSet prev = bySource.remove(source);
        if (prev == null) return;
        decrement(COUNTS.get(level.dimension()), prev);
        if (bySource.isEmpty()) BY_SOURCE.remove(level.dimension());
    }

    /** Drops every mark in a dimension (level unload). */
    public static void clear(ResourceKey<Level> dim) {
        BY_SOURCE.remove(dim);
        COUNTS.remove(dim);
    }

    public static boolean isIrrigated(LevelReader level, BlockPos pos) {
        if (!(level instanceof Level realLevel)) return false;
        Long2IntOpenHashMap counts = COUNTS.get(realLevel.dimension());
        return counts != null && counts.get(pos.asLong()) > 0;
    }

    private static void decrement(Long2IntOpenHashMap counts, LongSet area) {
        LongIterator it = area.iterator();
        while (it.hasNext()) {
            long packed = it.nextLong();
            int left = counts.get(packed) - 1;
            if (left <= 0) counts.remove(packed);
            else counts.put(packed, left);
        }
    }
}
