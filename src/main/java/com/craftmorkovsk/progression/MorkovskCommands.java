package com.craftmorkovsk.progression;

import com.craftmorkovsk.CraftMorkovsk;
import com.craftmorkovsk.crop.CropCatalog;
import com.craftmorkovsk.crop.CropDef;
import com.craftmorkovsk.data.Award;
import com.craftmorkovsk.data.FarmingLevel;
import com.craftmorkovsk.data.FarmingStats;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;

/** `/morkovsk` operator commands: give crops, money/xp add|set, level info,
 *  crop list and weather modifiers. Permission level 2 required. */
@Mod.EventBusSubscriber(modid = CraftMorkovsk.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MorkovskCommands {

    private static final SuggestionProvider<CommandSourceStack> CROP_IDS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(CropCatalog.BY_ID.keySet(), builder);

    private MorkovskCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("morkovsk")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("give")
                        .then(Commands.argument("crop", StringArgumentType.word())
                                .suggests(CROP_IDS)
                                .executes(MorkovskCommands::giveCrop)))
                .then(Commands.literal("money")
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", LongArgumentType.longArg())
                                        .executes(ctx -> money(ctx, false))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(ctx -> money(ctx, true)))))
                .then(Commands.literal("xp")
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> xp(ctx, false))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> xp(ctx, true)))))
                .then(Commands.literal("level")
                        .executes(MorkovskCommands::printLevel))
                .then(Commands.literal("crops")
                        .executes(MorkovskCommands::listCrops))
                .then(Commands.literal("weather")
                        .then(Commands.literal("drought")
                                .executes(ctx -> weather(ctx, true)))
                        .then(Commands.literal("blessed")
                                .executes(ctx -> weather(ctx, false)))));
    }

    private static int giveCrop(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "crop");
        CropDef def = CropCatalog.BY_ID.get(id);
        CommandSourceStack src = ctx.getSource();
        if (def == null) {
            src.sendFailure(Component.translatable("commands.craftmorkovsk.give.unknown", id));
            return 0;
        }
        ServerPlayer player = src.getPlayerOrException();
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(def.seeds.get(), 16));
        ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(def.produce.get(), 8));
        src.sendSuccess(() -> Component.translatable(
                "commands.craftmorkovsk.give.success", def.produce.get().getDescription()), true);
        return 1;
    }

    private static int money(CommandContext<CommandSourceStack> ctx, boolean set) throws CommandSyntaxException {
        long amount = LongArgumentType.getLong(ctx, "amount");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (set) {
            FarmingStats.get(player).ifPresent(data -> data.setMoney(amount));
            FarmingStats.sync(player);
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.craftmorkovsk.money.set", amount), true);
        } else {
            FarmingStats.addMoney(player, amount);
            ctx.getSource().sendSuccess(() -> Component.translatable(
                    "commands.craftmorkovsk.money.add", amount, FarmingStats.getMoney(player)), true);
        }
        return 1;
    }

    private static int xp(CommandContext<CommandSourceStack> ctx, boolean set) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int[] levels = new int[2];
        FarmingStats.get(player).ifPresent(data -> {
            levels[0] = data.getLevel();
            if (set) data.setXp(amount); else data.addXp(amount);
            levels[1] = data.getLevel();
        });
        FarmingStats.sync(player);
        if (levels[1] > levels[0]) announceLevelUp(player, levels[1]);
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.craftmorkovsk.xp." + (set ? "set" : "add"),
                amount, FarmingStats.getXp(player)), true);
        return 1;
    }

    private static int printLevel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        FarmingLevel rank = FarmingLevel.of(FarmingStats.getLevel(player));
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.craftmorkovsk.level.info",
                rank.displayName(), FarmingStats.getXp(player), FarmingStats.getMoney(player)), false);
        return 1;
    }

    private static int listCrops(CommandContext<CommandSourceStack> ctx) {
        String ids = String.join(", ", CropCatalog.BY_ID.keySet());
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "commands.craftmorkovsk.crops.list", CropCatalog.BY_ID.size(), ids), false);
        return 1;
    }

    private static int weather(CommandContext<CommandSourceStack> ctx, boolean drought) {
        ServerLevel level = ctx.getSource().getLevel();
        if (drought) {
            // Five days of clear skies — farmland dries out.
            level.setWeatherParameters(12000 * 5, 0, false, false);
            broadcast(ctx.getSource(), "commands.craftmorkovsk.weather.drought");
        } else {
            // Blessed day: a full day of rain waters every field.
            level.setWeatherParameters(0, 12000, true, false);
            broadcast(ctx.getSource(), "commands.craftmorkovsk.weather.blessed");
            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                Award.grant(player, "rain_dance");
            }
        }
        return 1;
    }

    private static void broadcast(CommandSourceStack src, String key) {
        src.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(key).withStyle(ChatFormatting.GOLD), false);
    }

    /** Mirrors FarmingStats' level-up announcement, using our level.up sound event. */
    private static void announceLevelUp(ServerPlayer player, int newLevel) {
        FarmingLevel rank = FarmingLevel.of(newLevel);
        player.displayClientMessage(Component.translatable(
                        "farming.craftmorkovsk.level_up", rank.displayName())
                .withStyle(ChatFormatting.GOLD), false);
        player.level().playSound(null, player.blockPosition(),
                ProgressionModule.SOUND_LEVEL_UP.get(), SoundSource.PLAYERS, 0.7f, 1.2f);
    }
}
