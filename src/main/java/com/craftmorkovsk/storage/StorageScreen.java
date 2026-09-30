package com.craftmorkovsk.storage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Chest-like storage screen in the agri palette, drawn entirely in code (no GUI
 *  texture): panel + recessed slot cells. Height adapts to the menu's row count
 *  (3 for crates/vault, 6 for the large crate, 2 for the silo). Kind-specific
 *  flourishes: leaf icon for the seed vault, stored/capacity readout for the silo. */
@OnlyIn(Dist.CLIENT)
public class StorageScreen extends AbstractContainerScreen<StorageMenu> {

    protected static final int COL_PANEL = 0xFF4A3B28;       // dark wood brown
    protected static final int COL_PANEL_LIGHT = 0xFF7A6A4F; // beige edge
    protected static final int COL_INTERIOR = 0xFFC6B99B;    // beige
    protected static final int COL_SLOT = 0xFF9A8B70;
    protected static final int COL_SLOT_INNER = 0xFFB8AA8C;
    protected static final int COL_TEXT = 0xFF3A2E1E;
    protected static final int COL_LEAF = 0xFF4F8F3F;
    protected static final int COL_LEAF_DARK = 0xFF3A6E2E;

    public StorageScreen(StorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + menu.kind.rows * 18;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, COL_PANEL);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, COL_PANEL_LIGHT);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, COL_INTERIOR);
        for (Slot slot : menu.slots) drawSlotCell(g, x + slot.x, y + slot.y);
        drawExtras(g, partialTick, mouseX, mouseY);
    }

    private void drawSlotCell(GuiGraphics g, int sx, int sy) {
        g.fill(sx - 1, sy - 1, sx + 17, sy + 17, COL_SLOT);
        g.fill(sx, sy, sx + 16, sy + 16, COL_SLOT_INNER);
    }

    /** Kind-specific extras: seed-vault leaf emblem, silo capacity readout. */
    protected void drawExtras(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        switch (menu.kind) {
            case SEED_VAULT -> drawLeaf(g, leftPos + imageWidth - 20, topPos + 4);
            case SILO -> g.drawString(font,
                    Component.translatable("gui.craftmorkovsk.storage.total",
                            menu.totalStored(), menu.capacity()),
                    leftPos + imageWidth - 8
                            - font.width(Component.translatable("gui.craftmorkovsk.storage.total",
                                    menu.totalStored(), menu.capacity())),
                    topPos + 7, COL_TEXT, false);
            default -> { }
        }
    }

    private void drawLeaf(GuiGraphics g, int lx, int ly) {
        g.fill(lx + 2, ly, lx + 6, ly + 2, COL_LEAF);
        g.fill(lx + 1, ly + 2, lx + 7, ly + 6, COL_LEAF);
        g.fill(lx + 1, ly + 6, lx + 4, ly + 8, COL_LEAF);
        g.fill(lx + 4, ly + 3, lx + 8, ly + 8, COL_LEAF_DARK);
        g.fill(lx + 1, ly + 8, lx + 2, ly + 10, COL_LEAF_DARK);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }
}
