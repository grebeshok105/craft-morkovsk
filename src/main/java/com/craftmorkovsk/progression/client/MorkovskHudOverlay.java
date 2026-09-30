package com.craftmorkovsk.progression.client;

import com.craftmorkovsk.client.ClientFarmingData;
import com.craftmorkovsk.data.FarmingLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/** Bottom-left farming HUD: rank, XP progress toward the next rank and morkoins.
 *  Drawn as a semi-transparent agri-brown panel — no texture assets needed. */
@OnlyIn(Dist.CLIENT)
public final class MorkovskHudOverlay implements IGuiOverlay {

    private static final int COL_BG = 0xAA2E2418;         // translucent dark brown
    private static final int COL_EDGE = 0xFF7A6A4F;       // beige edge
    private static final int COL_GOLD = 0xFFE0A828;       // amber
    private static final int COL_TEXT = 0xFFC6B99B;       // beige text
    private static final int COL_RANK = 0xFF6FAE4F;       // leaf green

    @Override
    public void render(ForgeGui gui, GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.screen != null) return;

        int x = 6;
        int y = screenHeight - 46;
        int w = 132;
        int h = 40;
        g.fill(x, y, x + w, y + h, COL_BG);
        g.fill(x, y, x + w, y + 1, COL_EDGE);
        g.fill(x, y + h - 1, x + w, y + h, COL_EDGE);
        g.fill(x, y, x + 1, y + h, COL_EDGE);
        g.fill(x + w - 1, y, x + w, y + h, COL_EDGE);

        Font font = mc.font;
        FarmingLevel rank = ClientFarmingData.rank();
        int xp = ClientFarmingData.xp();
        long money = ClientFarmingData.money();

        g.drawString(font, rank.displayName(), x + 6, y + 5, COL_RANK, false);

        int nextXp = nextRankXp(rank);
        Component xpLine = nextXp < 0
                ? Component.translatable("hud.craftmorkovsk.xp_max", xp)
                : Component.translatable("hud.craftmorkovsk.xp", xp, nextXp);
        g.drawString(font, xpLine, x + 6, y + 16, COL_TEXT, false);
        g.drawString(font, Component.translatable("hud.craftmorkovsk.money", money),
                x + 6, y + 27, COL_GOLD, false);
    }

    private static int nextRankXp(FarmingLevel rank) {
        for (FarmingLevel l : FarmingLevel.values()) {
            if (l.level == rank.level + 1) return l.requiredXp;
        }
        return -1;
    }
}
