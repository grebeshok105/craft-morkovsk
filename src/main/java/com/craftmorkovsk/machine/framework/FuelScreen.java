package com.craftmorkovsk.machine.framework;

import com.craftmorkovsk.energy.FuelMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Generic screen for FuelMachineMenu — energy bar + fuel-burn progress arrow. */
@OnlyIn(Dist.CLIENT)
public class FuelScreen extends MachineScreen<FuelMachineMenu> {

    public FuelScreen(FuelMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void drawExtras(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("gui.craftmorkovsk.fuel"), leftPos + 40, topPos + 20, COL_TEXT, false);
    }
}
