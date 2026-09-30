package com.craftmorkovsk.progression.client;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.config.MorkovskConfig;
import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import com.craftmorkovsk.crop.WaterNeed;
import com.craftmorkovsk.data.FarmingLevel;
import com.craftmorkovsk.data.Quality;
import com.craftmorkovsk.machine.framework.AbstractMachineBlock;
import com.craftmorkovsk.soil.SoilTier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** The Morkovsk Agricultural Handbook: a paged in-game reference drawn in the agri
 *  palette (fill rects + text, no texture assets). Pages cover crops, soils,
 *  machines, energy, animals, economy, farming levels and THE CARROT. */
@OnlyIn(Dist.CLIENT)
public class HandbookScreen extends Screen {

    private static final int COL_PANEL = 0xFF4A3B28;      // dark wood brown
    private static final int COL_EDGE = 0xFF7A6A4F;       // beige edge
    private static final int COL_PAGE = 0xFFC6B99B;       // beige page
    private static final int COL_TEXT = 0xFF3A2E1E;       // ink brown
    private static final int COL_TITLE = 0xFF4A2A0E;      // deep brown
    private static final int COL_GOLD = 0xFF8A5A00;       // muted amber (readable on beige)
    private static final int COL_DIM = 0xFF6B5D47;        // dim brown

    private static final int BOOK_W = 248;
    private static final int BOOK_H = 186;
    private static final int MARGIN = 14;

    private static final Map<SoilTier, String> SOIL_ABBR = new EnumMap<>(SoilTier.class);
    static {
        SOIL_ABBR.put(SoilTier.EXHAUSTED, "exh");
        SOIL_ABBR.put(SoilTier.NORMAL, "norm");
        SOIL_ABBR.put(SoilTier.RICH, "rich");
        SOIL_ABBR.put(SoilTier.WET, "wet");
        SOIL_ABBR.put(SoilTier.COMPOST, "comp");
        SOIL_ABBR.put(SoilTier.FERTILIZED, "fert");
    }

    private final List<Page> pages;
    private int page;
    private Button prevButton;
    private Button nextButton;

    public HandbookScreen() {
        super(Component.translatable("item.craftmorkovsk.agricultural_handbook"));
        this.pages = buildPages();
    }

    @Override
    protected void init() {
        int x = (width - BOOK_W) / 2;
        int y = (height - BOOK_H) / 2;
        prevButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> flip(-1))
                .bounds(x + 10, y + BOOK_H - 26, 20, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), b -> flip(1))
                .bounds(x + BOOK_W - 30, y + BOOK_H - 26, 20, 20).build());
        updateButtons();
    }

    private void flip(int delta) {
        int next = net.minecraft.util.Mth.clamp(page + delta, 0, pages.size() - 1);
        if (next == page) return;
        page = next;
        updateButtons();
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
        }
    }

    private void updateButtons() {
        prevButton.active = page > 0;
        nextButton.active = page < pages.size() - 1;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int x = (width - BOOK_W) / 2;
        int y = (height - BOOK_H) / 2;
        g.fill(x, y, x + BOOK_W, y + BOOK_H, COL_PANEL);
        g.fill(x + 2, y + 2, x + BOOK_W - 2, y + BOOK_H - 2, COL_EDGE);
        g.fill(x + 4, y + 4, x + BOOK_W - 4, y + BOOK_H - 4, COL_PAGE);

        Page p = pages.get(page);
        g.drawCenteredString(font, p.title(), x + BOOK_W / 2, y + 9, COL_TITLE);
        int textX = x + MARGIN;
        int textW = BOOK_W - 2 * MARGIN;
        p.render(g, font, textX, y + 24, textW);

        g.drawCenteredString(font,
                Component.translatable("handbook.craftmorkovsk.page", page + 1, pages.size()),
                x + BOOK_W / 2, y + BOOK_H - 19, COL_DIM);
        super.render(g, mouseX, mouseY, partialTick);
    }

    // ---------- pages ----------

    private sealed interface Page {
        Component title();
        void render(GuiGraphics g, Font font, int x, int y, int w);
    }

    private record TextPage(Component title, List<Component> paragraphs) implements Page {
        @Override
        public void render(GuiGraphics g, Font font, int x, int y, int w) {
            int cy = y;
            for (Component para : paragraphs) {
                for (FormattedCharSequence line : font.split(para, w)) {
                    if (cy > y + 132) return;
                    g.drawString(font, line, x, cy, COL_TEXT, false);
                    cy += 10;
                }
                cy += 3;
            }
        }
    }

    private record CropPage(Component title, List<CropDef> crops) implements Page {
        @Override
        public void render(GuiGraphics g, Font font, int x, int y, int w) {
            g.drawString(font, Component.translatable("handbook.craftmorkovsk.col.crop"),
                    x, y, COL_DIM, false);
            g.drawString(font, Component.translatable("handbook.craftmorkovsk.col.water"),
                    x + 96, y, COL_DIM, false);
            g.drawString(font, Component.translatable("handbook.craftmorkovsk.col.soil"),
                    x + 124, y, COL_DIM, false);
            g.drawString(font, Component.translatable("handbook.craftmorkovsk.col.price"),
                    x + 182, y, COL_DIM, false);
            int cy = y + 12;
            for (CropDef def : crops) {
                String name = def.secret ? "???"
                        : font.plainSubstrByWidth(def.produce.get().getDescription().getString(), 90);
                g.drawString(font, name, x, cy, def.secret ? COL_DIM : COL_TEXT, false);
                g.drawString(font, def.secret ? "?" : waterShort(def.waterNeed),
                        x + 96, cy, COL_TEXT, false);
                g.drawString(font, def.secret ? "?" : font.plainSubstrByWidth(soilShort(def), 54),
                        x + 124, cy, COL_TEXT, false);
                g.drawString(font, def.secret ? "???" : def.price + " mk",
                        x + 182, cy, COL_GOLD, false);
                cy += 11;
            }
        }
    }

    private static String waterShort(WaterNeed need) {
        return switch (need) {
            case LOW -> Component.translatable("handbook.craftmorkovsk.water.low").getString();
            case MEDIUM -> Component.translatable("handbook.craftmorkovsk.water.med").getString();
            case HIGH -> Component.translatable("handbook.craftmorkovsk.water.high").getString();
        };
    }

    private static String soilShort(CropDef def) {
        if (def.soils.isEmpty()) {
            return Component.translatable("handbook.craftmorkovsk.soil.any").getString();
        }
        StringBuilder sb = new StringBuilder();
        for (SoilTier t : def.soils) {
            if (sb.length() > 0) sb.append('+');
            sb.append(SOIL_ABBR.getOrDefault(t, "?"));
        }
        return sb.toString();
    }

    private static Component bullet(Component c) {
        return Component.literal("• ").append(c);
    }

    private static List<Page> buildPages() {
        List<Page> pages = new ArrayList<>();
        // 1. Welcome
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.welcome.title"),
                List.of(
                        Component.translatable("handbook.craftmorkovsk.welcome.1"),
                        Component.translatable("handbook.craftmorkovsk.welcome.2"),
                        Component.translatable("handbook.craftmorkovsk.welcome.3"))));
        // 2-4. Crops table, 10 rows per page
        List<CropDef> crops = CropCatalog.ALL;
        int perPage = 10;
        for (int i = 0; i < crops.size(); i += perPage) {
            pages.add(new CropPage(Component.translatable("handbook.craftmorkovsk.crops.title"),
                    crops.subList(i, Math.min(i + perPage, crops.size()))));
        }
        // 5. Soils & fertilizer
        List<Component> soilLines = new ArrayList<>();
        soilLines.add(Component.translatable("handbook.craftmorkovsk.soils.intro"));
        for (SoilTier t : SoilTier.values()) {
            soilLines.add(bullet(Component.translatable("block.craftmorkovsk." + t.blockName)
                    .append(Component.literal(" — ×" + t.growthMult
                            + ", " + Component.translatable("handbook.craftmorkovsk.soils.bonus",
                            t.qualityBonus).getString()))));
        }
        soilLines.add(Component.translatable("handbook.craftmorkovsk.soils.fertilizer"));
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.soils.title"), soilLines));
        // 6. Machines
        List<Component> machineLines = new ArrayList<>();
        machineLines.add(Component.translatable("handbook.craftmorkovsk.machines.intro"));
        List<Component> machineNames = new ArrayList<>();
        for (Map.Entry<ResourceKey<Block>, Block> e : ForgeRegistries.BLOCKS.getEntries()) {
            if (!e.getKey().location().getNamespace().equals(CraftMorkovsk.MOD_ID)) continue;
            if (e.getValue() instanceof AbstractMachineBlock) {
                machineNames.add(bullet(e.getValue().getName()));
            }
        }
        machineNames.sort(Comparator.comparing(Component::getString));
        machineLines.addAll(machineNames);
        machineLines.add(Component.translatable("handbook.craftmorkovsk.machines.note"));
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.machines.title"), machineLines));
        // 7. Energy primer
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.energy.title"),
                List.of(
                        Component.translatable("handbook.craftmorkovsk.energy.1"),
                        Component.translatable("handbook.craftmorkovsk.energy.2",
                                MorkovskConfig.GENERATOR_FE_PER_TICK.get(),
                                MorkovskConfig.BIOMASS_FE_PER_TICK.get()),
                        Component.translatable("handbook.craftmorkovsk.energy.3",
                                MorkovskConfig.MACHINE_ENERGY_PER_TICK.get()))));
        // 8. Animals
        List<Component> animalLines = new ArrayList<>();
        animalLines.add(Component.translatable("handbook.craftmorkovsk.animals.intro"));
        List<Component> animalNames = new ArrayList<>();
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> e
                : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            if (!e.getKey().location().getNamespace().equals(CraftMorkovsk.MOD_ID)) continue;
            if (e.getValue().getCategory() == MobCategory.CREATURE) {
                animalNames.add(bullet(e.getValue().getDescription()));
            }
        }
        animalNames.sort(Comparator.comparing(Component::getString));
        animalLines.addAll(animalNames);
        animalLines.add(Component.translatable("handbook.craftmorkovsk.animals.note"));
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.animals.title"), animalLines));
        // 9. Economy
        List<Component> econLines = new ArrayList<>();
        econLines.add(Component.translatable("handbook.craftmorkovsk.economy.intro",
                MorkovskConfig.BASE_CROP_PRICE.get(),
                Math.round(MorkovskConfig.PRICE_FLUCTUATION.get().floatValue() * 100)));
        for (Quality q : Quality.values()) {
            econLines.add(Component.literal("• ").append(q.displayName())
                    .append(Component.literal(" — ×" + q.priceMultiplier)));
        }
        econLines.add(Component.translatable("handbook.craftmorkovsk.economy.note"));
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.economy.title"), econLines));
        // 10. Farming levels
        List<Component> levelLines = new ArrayList<>();
        levelLines.add(Component.translatable("handbook.craftmorkovsk.levels.intro"));
        for (FarmingLevel l : FarmingLevel.values()) {
            levelLines.add(Component.literal("• ").append(l.displayName())
                    .append(Component.literal(" — " + l.requiredXp + " XP")));
        }
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.levels.title"), levelLines));
        // 11. THE CARROT teaser
        pages.add(new TextPage(Component.translatable("handbook.craftmorkovsk.carrot.title"),
                List.of(
                        Component.translatable("handbook.craftmorkovsk.carrot.1"),
                        Component.translatable("handbook.craftmorkovsk.carrot.2"))));
        return pages;
    }
}
