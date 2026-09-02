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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class LivingPathsDebugCommands {

    private static final Block[] SHOWCASE_STAGES = {
            Blocks.GRASS_BLOCK,
            Blocks.DIRT_PATH,
            Blocks.COARSE_DIRT,
            Blocks.GRAVEL,
            Blocks.COBBLESTONE,
            Blocks.STONE,
            Blocks.SMOOTH_STONE
    };
    private static final int SHOWCASE_STAGE_LENGTH = 4;
    private static final int SHOWCASE_HALF_WIDTH = 3;

    private LivingPathsDebugCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("livingpaths")
                        .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
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
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        false
                                                ))))
                                .then(Commands.literal("addedgewear")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 10_000))
                                                .executes(context -> addWear(
                                                        context.getSource().getPlayerOrException(),
                                                        IntegerArgumentType.getInteger(context, "amount"),
                                                        true
                                                ))))
                                .then(Commands.literal("agewear")
                                        .then(Commands.argument("days", IntegerArgumentType.integer(1, 10_000))
                                                .executes(context -> ageWear(
                                                        context.getSource().getPlayerOrException(),
                                                        IntegerArgumentType.getInteger(context, "days")
                                                ))))
                                .then(Commands.literal("showcase")
                                        .executes(context -> createShowcase(
                                                context.getSource().getPlayerOrException()
                                        ))))
        );
    }

    private static int createShowcase(ServerPlayer player) {
        ServerLevel level = player.level();
        Direction forward = player.getDirection();
        Direction right = forward.getClockWise();
        BlockPos origin = player.getOnPos().relative(forward, 4);
        PathWearData data = PathWearData.get(level);
        long gameTime = level.getGameTime();
        int length = SHOWCASE_STAGES.length * SHOWCASE_STAGE_LENGTH;
        int changedBlocks = 0;

        for (int distance = 0; distance < length; distance++) {
            int stageIndex = distance / SHOWCASE_STAGE_LENGTH;

            for (int sideways = -SHOWCASE_HALF_WIDTH; sideways <= SHOWCASE_HALF_WIDTH; sideways++) {
                BlockPos pos = origin.relative(forward, distance).relative(right, sideways);
                Block block = showcaseBlock(stageIndex, distance, sideways);

                data.clearWear(pos);
                if (level.setBlockAndUpdate(pos, block.defaultBlockState())) {
                    changedBlocks++;
                }
                level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos.above(2), Blocks.AIR.defaultBlockState());

                if (block != Blocks.GRASS_BLOCK) {
                    boolean stonePathOrigin = block == Blocks.STONE || block == Blocks.SMOOTH_STONE;
                    data.markEstablished(pos, gameTime, stonePathOrigin);
                }
            }
        }

        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.showcase",
                changedBlocks,
                origin.getX(), origin.getY(), origin.getZ()
        ));
        return changedBlocks;
    }

    private static Block showcaseBlock(int stageIndex, int distance, int sideways) {
        int absoluteSideways = Math.abs(sideways);
        if (absoluteSideways <= 1) {
            return SHOWCASE_STAGES[stageIndex];
        }
        if (absoluteSideways == SHOWCASE_HALF_WIDTH) {
            return Blocks.GRASS_BLOCK;
        }

        int variation = Math.floorMod(distance * 31 + sideways * 17, 5);
        if (variation < 2) {
            return SHOWCASE_STAGES[stageIndex];
        }
        return SHOWCASE_STAGES[Math.max(0, stageIndex - 1)];
    }

    private static int showStatus(ServerPlayer player) {
        ServerLevel level = player.level();
        var groundPos = player.getOnPos().immutable();
        int wear = PathWearEvents.getWear(level, groundPos);
        int edgeWear = PathWearEvents.getEdgeWear(level, groundPos);
        int threshold = PathWearEvents.getThreshold(level, groundPos);

        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.status",
                groundPos.getX(), groundPos.getY(), groundPos.getZ(), wear, edgeWear, threshold
        ));
        return wear;
    }

    private static int inspectPath(ServerPlayer player) {
        ServerLevel level = player.level();
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
        int edgeWear = PathWearEvents.getEdgeWear(level, pos);
        int threshold = PathWearEvents.getThreshold(level, pos);
        player.sendSystemMessage(Component.translatable(
                translationKey,
                pos.getX(), pos.getY(), pos.getZ(), wear, edgeWear, threshold
        ));
    }

    private static int showProfile(ServerPlayer player) {
        ServerLevel level = player.level();
        var groundPos = player.getOnPos().immutable();
        var profile = BiomePathProfiles.profileFor(level, groundPos);

        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.profile",
                groundPos.getX(), groundPos.getY(), groundPos.getZ(), profile.name()
        ));
        return profile.ordinal() + 1;
    }

    private static int addWear(ServerPlayer player, int amount, boolean edgeWear) {
        ServerLevel level = player.level();
        var groundPos = player.getOnPos().immutable();

        if (edgeWear) {
            PathWearEvents.addEdgeWear(level, groundPos, amount);
        } else {
            PathWearEvents.addWear(level, groundPos, amount);
        }

        int wear = PathWearEvents.getWear(level, groundPos);
        int edge = PathWearEvents.getEdgeWear(level, groundPos);
        int threshold = PathWearEvents.getThreshold(level, groundPos);
        player.sendSystemMessage(Component.translatable(
                edgeWear ? "command.livingpaths.debug.addedgewear" : "command.livingpaths.debug.addwear",
                amount, groundPos.getX(), groundPos.getY(), groundPos.getZ(), wear, edge, threshold
        ));
        return wear;
    }

    private static int ageWear(ServerPlayer player, int days) {
        ServerLevel level = player.level();
        var groundPos = player.getOnPos().immutable();
        PathWearData.get(level).ageWearForDebug(groundPos, days);

        int wear = PathWearEvents.getWear(level, groundPos);
        int edgeWear = PathWearEvents.getEdgeWear(level, groundPos);
        int threshold = PathWearEvents.getThreshold(level, groundPos);
        player.sendSystemMessage(Component.translatable(
                "command.livingpaths.debug.agewear",
                days, groundPos.getX(), groundPos.getY(), groundPos.getZ(), wear, edgeWear, threshold
        ));
        return wear;
    }
}
