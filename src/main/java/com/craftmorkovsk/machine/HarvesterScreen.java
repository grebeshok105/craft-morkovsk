package com.craftmorkovsk.machine;

import com.craftmorkovsk.machine.framework.MachineScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HarvesterScreen extends MachineScreen<HarvesterMenu> {

    public HarvesterScreen(HarvesterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
