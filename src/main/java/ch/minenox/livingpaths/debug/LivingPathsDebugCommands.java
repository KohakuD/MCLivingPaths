package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.LivingPaths;
import ch.minenox.livingpaths.path.BiomePathProfiles;
import ch.minenox.livingpaths.path.PathWearData;
import ch.minenox.livingpaths.path.PathWearEvents;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class LivingPathsDebugCommands {

    private LivingPathsDebugCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("livingpaths")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("debug")
                                .then(Commands.literal("status")
                                        .executes(context -> showStatus(context.getSource().getPlayerOrException())))
                                .then(Commands.literal("inspect")
                                        .executes(context -> inspectPath(context.getSource().getPlayerOrException())))
                                .then(Commands.literal("profile")
                                        .executes(context -> showProfile(context.getSource().getPlayerOrException())))
                                .then(Commands.literal("addwear")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10_000))
                                                .executes(context -> addWear(
                                                        context.getSource().getPlayerOrException(),
                                                        IntegerArgumentType.getInteger(context, "amount")
                                                ))))
                                .then(Commands.literal("agewear")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1, 10_000))
                                                .executes(context -> ageWear(
                                                        context.getSource().getPlayerOrException(),
                                                        IntegerArgumentType.getInteger(context, "days")
                                                )))))
        );
    }

    private static int showStatus(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        var groundPos = player.getOnPos().immutable();
        int wear = PathWearEvents.getWear(level, groundPos);
        int threshold = PathWearEvents.getThreshold(level, groundPos);

        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.status",
                groundPos.getX(), groundPos.getY(), groundPos.getZ(), wear, threshold
        ));
        return wear;
    }

    private static int inspectPath(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos centre = player.getOnPos().immutable();
        Direction forward = player.getDirection();
        Direction leftDirection = forward.getCounterClockWise();
        Direction rightDirection = forward.getClockWise();
        BlockPos left = centre.relative(leftDirection);
        BlockPos right = centre.relative(rightDirection);

        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.inspect.header",
                forward.getName()
        ));
        sendInspectEntry(player, level, "command.livingpaths.debug.inspect.left", left);
        sendInspectEntry(player, level, "command.livingpaths.debug.inspect.centre", centre);
        sendInspectEntry(player, level, "command.livingpaths.debug.inspect.right", right);
        return 1;
    }

    private static void sendInspectEntry(ServerPlayer player, ServerLevel level, String translationKey, BlockPos pos) {
        int wear = PathWearEvents.getWear(level, pos);
        int threshold = PathWearEvents.getThreshold(level, pos);
        player.sendSystemMessage(Component.translatable(
                translationKey,
                pos.getX(), pos.getY(), pos.getZ(), wear, threshold
        ));
    }

    private static int showProfile(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        var groundPos = player.getOnPos().immutable();
        var profile = BiomePathProfiles.profileFor(level, groundPos);

        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.profile",
                groundPos.getX(), groundPos.getY(), groundPos.getZ(), profile.name()
        ));
        return profile.ordinal() + 1;
    }

    private static int addWear(ServerPlayer player, int amount) {
        ServerLevel level = player.serverLevel();
        var groundPos = player.getOnPos().immutable();
        PathWearEvents.addWear(level, groundPos, amount);

        int wear = PathWearEvents.getWear(level, groundPos);
        int threshold = PathWearEvents.getThreshold(level, groundPos);
        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.addwear",
                amount, groundPos.getX(), groundPos.getY(), groundPos.getZ(), wear, threshold
        ));
        return wear;
    }

    private static int ageWear(ServerPlayer player, int days) {
        ServerLevel level = player.serverLevel();
        var groundPos = player.getOnPos().immutable();
        PathWearData.get(level).ageWearForDebug(groundPos, days);

        int wear = PathWearEvents.getWear(level, groundPos);
        int threshold = PathWearEvents.getThreshold(level, groundPos);
        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.agewear",
                days, groundPos.getX(), groundPos.getY(), groundPos.getZ(), wear, threshold
        ));
        return wear;
    }
}
