package com.craftmorkovsk.tractor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Tractor GUI — agri palette like the machine screens: fuel bar, attachment slot,
 *  cargo row and the player inventory, all drawn in code (no texture assets). */
@OnlyIn(Dist.CLIENT)
public class TractorScreen extends AbstractContainerScreen<TractorMenu> {

    private static final int COL_PANEL = 0xFF4A3B28;
    private static final int COL_PANEL_LIGHT = 0xFF7A6A4F;
    private static final int COL_INTERIOR = 0xFFC6B99B;
    private static final int COL_ENERGY = 0xFFE0A828;
    private static final int COL_TEXT = 0xFF3A2E1E;

    public TractorScreen(TractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, COL_PANEL);
        g.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, COL_PANEL_LIGHT);
        g.fill(x + 3, y + 3, x + this.imageWidth - 3, y + this.imageHeight - 3, COL_INTERIOR);

        int barHeight = 52;
        int bx = x + 8;
        int by = y + 18;
        g.fill(bx, by, bx + 10, by + barHeight, 0xFF2A2118);
        int max = this.menu.maxFuel();
        if (max > 0) {
            int filled = (int) (barHeight * (this.menu.fuel() / (double) max));
            g.fill(bx + 1, by + barHeight - filled, bx + 9, by + barHeight - 1, COL_ENERGY);
        }

        g.drawString(this.font, Component.translatable("gui.craftmorkovsk.tractor_attachment"),
                x + 26, y + 12, COL_TEXT, false);
        g.drawString(this.font, Component.translatable("gui.craftmorkovsk.tractor_cargo"),
                x + 26, y + 36, COL_TEXT, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
        if (mouseX >= this.leftPos + 8 && mouseX <= this.leftPos + 18
                && mouseY >= this.topPos + 18 && mouseY <= this.topPos + 70) {
            g.renderTooltip(this.font,
                    Component.translatable("gui.craftmorkovsk.tractor_fuel",
                            this.menu.fuel(), this.menu.maxFuel()),
                    mouseX, mouseY);
        }
    }
}
