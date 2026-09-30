package com.craftmorkovsk.economy;

import com.craftmorkovsk.client.ClientFarmingData;
import com.craftmorkovsk.machine.framework.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Shipping crate screen: 27 sell slots, live estimate of the next sale, the time
 *  remaining until it, and the player's morkoin balance. */
@OnlyIn(Dist.CLIENT)
public class ShippingCrateScreen extends MachineScreen<ShippingCrateMenu> {

    public ShippingCrateScreen(ShippingCrateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void drawEnergyBar(GuiGraphics g, int x, int y) {
        // The crate is unpowered — no energy bar.
    }

    @Override
    protected void drawProgressArrow(GuiGraphics g, int x, int y) {
        // Replaced by the countdown text in drawExtras.
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 0xFF3A2E1E, false);
        // player-inventory label omitted: the economy HUD occupies that row
    }

    @Override
    protected void drawExtras(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        long dayIndex = minecraft.level != null ? minecraft.level.getDayTime() / 24000 : 0;
        long estimate = PriceTable.estimate(menu.handler(), dayIndex);
        g.drawString(font,
                Component.translatable("gui.craftmorkovsk.estimated_total", estimate),
                leftPos + 8, topPos + 74, COL_TEXT, false);

        Component balance = Component.translatable("gui.craftmorkovsk.balance", ClientFarmingData.money());
        g.drawString(font, balance,
                leftPos + imageWidth - 8 - font.width(balance), topPos + 74, COL_TEXT, false);

        int remaining = Math.max(0, menu.maxProgress() - menu.progress());
        Component countdown = Component.translatable("gui.craftmorkovsk.next_sale", (remaining + 19) / 20);
        g.drawString(font, countdown,
                leftPos + imageWidth - 8 - font.width(countdown), topPos + 7, COL_TEXT, false);
    }
}
