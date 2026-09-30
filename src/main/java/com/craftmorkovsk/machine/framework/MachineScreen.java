package com.craftmorkovsk.machine.framework;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Base machine screen: agricultural palette, energy bar and progress arrow drawn in code
 *  (no GUI texture assets needed). Subclasses override {@link #drawExtras} for labels/slots. */
@OnlyIn(Dist.CLIENT)
public abstract class MachineScreen<T extends MachineMenu> extends AbstractContainerScreen<T> {

    protected static final int COL_PANEL = 0xFF4A3B28;      // dark wood brown
    protected static final int COL_PANEL_LIGHT = 0xFF7A6A4F; // beige edge
    protected static final int COL_INTERIOR = 0xFFC6B99B;    // beige
    protected static final int COL_ENERGY = 0xFFE0A828;      // amber
    protected static final int COL_PROGRESS = 0xFF6FAE4F;    // leaf green
    protected static final int COL_TEXT = 0xFF3A2E1E;

    protected MachineScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, COL_PANEL);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, COL_PANEL_LIGHT);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, COL_INTERIOR);
        drawEnergyBar(g, x, y);
        drawProgressArrow(g, x, y);
        drawExtras(g, partialTick, mouseX, mouseY);
    }

    protected void drawEnergyBar(GuiGraphics g, int x, int y) {
        int h = 52;
        int bx = x + 8;
        int by = y + 18;
        g.fill(bx, by, bx + 10, by + h, 0xFF2A2118);
        int max = menu.maxEnergy();
        if (max > 0) {
            int filled = (int) (h * (menu.energy() / (double) max));
            g.fill(bx + 1, by + h - filled, bx + 9, by + h - 1, COL_ENERGY);
        }
    }

    protected void drawProgressArrow(GuiGraphics g, int x, int y) {
        int max = menu.maxProgress();
        if (max <= 0 || menu.progress() <= 0) return;
        int w = (int) (24 * (menu.progress() / (double) max));
        int ax = x + 76;
        int ay = y + 40;
        g.fill(ax, ay, ax + w, ay + 4, COL_PROGRESS);
        g.fill(ax, ay + 6, ax + Math.max(0, w - 4), ay + 10, COL_PROGRESS);
    }

    /** Draw machine-specific widgets; default no-op. Called after bg/energy/progress. */
    protected void drawExtras(GuiGraphics g, float partialTick, int mouseX, int mouseY) { }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        renderEnergyTooltip(g, mouseX, mouseY);
    }

    private void renderEnergyTooltip(GuiGraphics g, int mouseX, int mouseY) {
        if (mouseX >= leftPos + 8 && mouseX <= leftPos + 18
                && mouseY >= topPos + 18 && mouseY <= topPos + 70) {
            g.renderTooltip(font,
                    Component.literal(menu.energy() + " / " + menu.maxEnergy() + " FE"),
                    mouseX, mouseY);
        }
    }
}
