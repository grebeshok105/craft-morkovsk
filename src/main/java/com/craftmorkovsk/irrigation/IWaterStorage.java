package com.craftmorkovsk.irrigation;

/** Block entities that hold irrigation water (tanks, sprinklers, channels).
 *  The unit is intentionally abstract: 1 unit ~ 100 mB. Implemented with plain
 *  integers instead of the Forge fluid capability to keep the system simple. */
public interface IWaterStorage {

    /** Adds water; returns the amount actually accepted. */
    int receiveWater(int amount, boolean simulate);

    /** Removes water; returns the amount actually drained. */
    int extractWater(int amount, boolean simulate);

    int getWaterStored();

    int getWaterCapacity();
}
