package com.craftmorkovsk.crop;

/** How strongly a crop depends on hydration; dryPenalty multiplies growth when not watered. */
public enum WaterNeed {
    LOW(0.8f), MEDIUM(0.45f), HIGH(0.1f);

    public final float dryPenalty;
    WaterNeed(float dryPenalty) { this.dryPenalty = dryPenalty; }
}
