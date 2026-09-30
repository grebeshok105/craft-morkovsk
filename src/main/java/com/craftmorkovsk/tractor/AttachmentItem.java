package com.craftmorkovsk.tractor;

import net.minecraft.world.item.Item;

/** One of four tractor attachments. Installed via right-click on the tractor or the
 *  attachment slot in the tractor menu; determines what driving over a field does. */
public class AttachmentItem extends Item {

    public enum Kind {
        PLOW(0),
        PLANTER(1),
        HARVESTER(2),
        SPREADER(3);

        public final int id;
        Kind(int id) { this.id = id; }

        public static Kind byId(int id) {
            for (Kind k : values()) if (k.id == id) return k;
            return null;
        }
    }

    private final Kind kind;

    public AttachmentItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() { return kind; }
}
