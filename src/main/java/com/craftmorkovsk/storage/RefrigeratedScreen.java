package com.craftmorkovsk.storage;

import com.craftmorkovsk.machine.framework.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Fridge screen: machine-framework energy bar on the left, storage grid shifted
 *  right (wider than a vanilla chest — 196px), status readout and a frost emblem. */
@OnlyIn(Dist.CLIENT)
public class RefrigeratedScreen extends MachineScreen<RefrigeratedMenu> {

    private static final int COL_SLOT = 0xFF9A8B70;
    private static final int COL_SLOT_INNER = 0xFFB8AA8C;
    private static final int COL_FROST = 0xFF9ED8E8;
    private static final int COL_OK = 0xFF6FAE4F;
    private static final int COL_WARN = 0xFFB04A3A;

    public RefrigeratedScreen(RefrigeratedMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 196;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = RefrigeratedMenu.GRID_X;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void drawExtras(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        for (Slot slot : menu.slots) {
            g.fill(leftPos + slot.x - 1, topPos + slot.y - 1,
                    leftPos + slot.x + 17, topPos + slot.y + 17, COL_SLOT);
            g.fill(leftPos + slot.x, topPos + slot.y,
                    leftPos + slot.x + 16, topPos + slot.y + 16, COL_SLOT_INNER);
        }
        boolean powered = menu.powered();
        Component status = Component.translatable(powered
                ? "gui.craftmorkovsk.storage.chilling"
                : "gui.craftmorkovsk.storage.no_power");
        g.drawString(font, status, leftPos + RefrigeratedMenu.GRID_X, topPos + 6,
                powered ? COL_OK : COL_WARN, false);
        drawSnowflake(g, leftPos + imageWidth - 16, topPos + 4);
    }

    private void drawSnowflake(GuiGraphics g, int cx, int cy) {
        // 9px asterisk snowflake
        g.fill(cx + 4, cy, cx + 5, cy + 9, COL_FROST);
        g.fill(cx, cy + 4, cx + 9, cy + 5, COL_FROST);
        g.fill(cx + 1, cy + 1, cx + 2, cy + 2, COL_FROST);
        g.fill(cx + 7, cy + 1, cx + 8, cy + 2, COL_FROST);
        g.fill(cx + 1, cy + 7, cx + 2, cy + 8, COL_FROST);
        g.fill(cx + 7, cy + 7, cx + 8, cy + 8, COL_FROST);
    }
}
