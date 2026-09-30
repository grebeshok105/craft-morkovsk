package com.craftmorkovsk.soil;

/** Ordered soil quality tiers. growthMult scales crop speed; qualityBonus feeds the
 *  harvested-produce quality roll; degradeResist reduces exhaustion on harvest. */
public enum SoilTier {
    EXHAUSTED(0, "exhausted_soil", 0.5f, 0, 0.0f),
    NORMAL(1, "rich_farmland", 1.0f, 0, 0.0f),
    RICH(2, "rich_soil", 1.3f, 1, 0.1f),
    WET(2, "wet_soil", 1.15f, 1, 0.0f),
    COMPOST(3, "compost_soil", 1.5f, 2, 0.25f),
    FERTILIZED(4, "fertilized_soil", 1.8f, 3, 0.5f);

    public final int rank;
    public final String blockName;
    public final float growthMult;
    public final int qualityBonus;
    public final float degradeResist;

    SoilTier(int rank, String blockName, float growthMult, int qualityBonus, float degradeResist) {
        this.rank = rank;
        this.blockName = blockName;
        this.growthMult = growthMult;
        this.qualityBonus = qualityBonus;
        this.degradeResist = degradeResist;
    }

    /** Next lower tier, or itself at the bottom. */
    public SoilTier degraded() {
        return switch (this) {
            case FERTILIZED -> COMPOST;
            case COMPOST -> NORMAL;
            case WET -> NORMAL;
            case RICH -> NORMAL;
            default -> EXHAUSTED;
        };
    }
}
