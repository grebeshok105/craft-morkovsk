package com.craftmorkovsk.economy;

import com.craftmorkovsk.client.ClientFarmingData;
import com.craftmorkovsk.network.MorkovskNet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/** Morkovsk shop screen: a scrollable list of offers (icon + name + price).
 *  Clicking a row sends {@link BuyPacket} to the server, which validates and
 *  completes the purchase. Balance comes from {@link ClientFarmingData}. */
@OnlyIn(Dist.CLIENT)
public class ShopScreen extends AbstractContainerScreen<ShopMenu> {

    private static final int COL_PANEL = 0xFF4A3B28;
    private static final int COL_PANEL_LIGHT = 0xFF7A6A4F;
    private static final int COL_INTERIOR = 0xFFC6B99B;
    private static final int COL_ROW_HOVER = 0xFF8A7A5F;
    private static final int COL_ROW = 0xFF9A8A6A;
    private static final int COL_SLOT = 0xFF8A7A5F;
    private static final int COL_TEXT = 0xFF3A2E1E;
    private static final int COL_PRICE = 0xFF2A6A2A;
    private static final int COL_LOCKED = 0xFF6A5A4A;

    private static final int ROW_H = 18;
    private static final int VISIBLE_ROWS = 6;
    private static final int LIST_TOP = 24;
    private static final int LIST_X = 8;
    private static final int LIST_W = 214;      // rows span x+8 .. x+222
    private static final int SCROLL_X = 224;    // scrollbar track

    private int scrollOffset;

    public ShopScreen(ShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 240;
        this.imageHeight = 222;
        this.inventoryLabelX = ShopMenu.PLAYER_INV_X;
        this.inventoryLabelY = 128;
        this.titleLabelY = 7;
    }

    /** Offer indices the local player is allowed to see (secrets hidden below the level gate). */
    private List<Integer> visibleOffers() {
        List<Integer> visible = new ArrayList<>();
        List<ShopCatalog.ShopOffer> all = ShopCatalog.offers();
        int level = ClientFarmingData.level();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).secret() && level < ShopCatalog.SECRET_LEVEL) continue;
            visible.add(i);
        }
        return visible;
    }

    private int maxScroll(int visibleCount) {
        return Math.max(0, visibleCount - VISIBLE_ROWS);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, COL_PANEL);
        g.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, COL_PANEL_LIGHT);
        g.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, COL_INTERIOR);

        // Player inventory slot backings.
        for (Slot slot : menu.slots) {
            g.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, COL_SLOT);
        }

        List<Integer> visible = visibleOffers();
        scrollOffset = Math.min(scrollOffset, maxScroll(visible.size()));

        // Offer rows.
        g.enableScissor(x + LIST_X, y + LIST_TOP, x + LIST_X + LIST_W + 8, y + LIST_TOP + VISIBLE_ROWS * ROW_H);
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int vi = scrollOffset + row;
            if (vi >= visible.size()) break;
            int offerIndex = visible.get(vi);
            ShopCatalog.ShopOffer offer = ShopCatalog.offer(offerIndex);
            int ry = y + LIST_TOP + row * ROW_H;
            boolean hovered = mouseX >= x + LIST_X && mouseX <= x + LIST_X + LIST_W
                    && mouseY >= ry && mouseY < ry + ROW_H;
            g.fill(x + LIST_X, ry, x + LIST_X + LIST_W, ry + ROW_H - 1,
                    hovered ? COL_ROW_HOVER : COL_ROW);
            ItemStack stack = offer.stack().get();
            g.renderItem(stack, x + LIST_X + 2, ry);
            boolean affordable = ClientFarmingData.money() >= offer.price();
            g.drawString(font, stack.getHoverName(), x + LIST_X + 22, ry + 5,
                    affordable ? COL_TEXT : COL_LOCKED, false);
            Component price = Component.translatable("gui.craftmorkovsk.price", offer.price());
            g.drawString(font, price, x + LIST_X + LIST_W - 4 - font.width(price), ry + 5,
                    affordable ? COL_PRICE : COL_LOCKED, false);
        }
        g.disableScissor();

        drawScrollbar(g, x, y, visible.size());
    }

    private void drawScrollbar(GuiGraphics g, int x, int y, int visibleCount) {
        int trackTop = y + LIST_TOP;
        int trackBottom = y + LIST_TOP + VISIBLE_ROWS * ROW_H - 1;
        g.fill(x + SCROLL_X, trackTop, x + SCROLL_X + 6, trackBottom, COL_PANEL);
        int maxScroll = maxScroll(visibleCount);
        if (maxScroll <= 0) return;
        int trackH = trackBottom - trackTop;
        int thumbH = Math.max(10, trackH * VISIBLE_ROWS / visibleCount);
        int thumbY = trackTop + (trackH - thumbH) * scrollOffset / maxScroll;
        g.fill(x + SCROLL_X + 1, thumbY, x + SCROLL_X + 5, thumbY + thumbH, COL_PANEL_LIGHT);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
        Component balance = Component.translatable("gui.craftmorkovsk.balance", ClientFarmingData.money());
        g.drawString(font, balance, imageWidth - 8 - font.width(balance), titleLabelY, COL_TEXT, false);
    }

    @Override
    public void renderTooltip(GuiGraphics g, int x, int y) {
        super.renderTooltip(g, x, y);
        List<Integer> visible = visibleOffers();
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int vi = scrollOffset + row;
            if (vi >= visible.size()) break;
            int ry = topPos + LIST_TOP + row * ROW_H;
            if (x >= leftPos + LIST_X && x <= leftPos + LIST_X + LIST_W && y >= ry && y < ry + ROW_H) {
                ShopCatalog.ShopOffer offer = ShopCatalog.offer(visible.get(vi));
                ItemStack stack = offer.stack().get();
                List<Component> lines = new ArrayList<>(stack.getTooltipLines(
                        minecraft.player, net.minecraft.world.item.TooltipFlag.NORMAL));
                lines.add(Component.translatable("gui.craftmorkovsk.price", offer.price())
                        .withStyle(ChatFormatting.GOLD));
                if (ClientFarmingData.money() < offer.price()) {
                    lines.add(Component.translatable("economy.craftmorkovsk.cant_afford")
                            .withStyle(ChatFormatting.RED));
                }
                g.renderComponentTooltip(font, lines, x, y);
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Scrollbar track: page up/down on click.
            if (mouseX >= leftPos + SCROLL_X && mouseX <= leftPos + SCROLL_X + 6
                    && mouseY >= topPos + LIST_TOP && mouseY < topPos + LIST_TOP + VISIBLE_ROWS * ROW_H) {
                int rowUnderMouse = (int) ((mouseY - topPos - LIST_TOP) / ROW_H) + scrollOffset;
                scrollOffset = Math.max(0, Math.min(maxScroll(visibleOffers().size()),
                        rowUnderMouse < scrollOffset + 1 ? scrollOffset - VISIBLE_ROWS
                                : scrollOffset + VISIBLE_ROWS));
                return true;
            }
            List<Integer> visible = visibleOffers();
            for (int row = 0; row < VISIBLE_ROWS; row++) {
                int vi = scrollOffset + row;
                if (vi >= visible.size()) break;
                int ry = topPos + LIST_TOP + row * ROW_H;
                if (mouseX >= leftPos + LIST_X && mouseX <= leftPos + LIST_X + LIST_W
                        && mouseY >= ry && mouseY < ry + ROW_H) {
                    MorkovskNet.sendToServer(new BuyPacket(visible.get(vi)));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= leftPos + LIST_X && mouseX <= leftPos + SCROLL_X + 6
                && mouseY >= topPos + LIST_TOP && mouseY < topPos + LIST_TOP + VISIBLE_ROWS * ROW_H) {
            scrollOffset = Math.max(0, Math.min(maxScroll(visibleOffers().size()),
                    scrollOffset - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
