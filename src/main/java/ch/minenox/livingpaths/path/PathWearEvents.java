package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class PathWearEvents {

    private static final int GRASS_THRESHOLD = 25;
    private static final int PODZOL_THRESHOLD = 75;
    private static final int MYCELIUM_THRESHOLD = 75;
    private static final int DIRT_PATH_THRESHOLD = 50;
    private static final int ROOTED_DIRT_THRESHOLD = 75;
    private static final int COARSE_DIRT_THRESHOLD = 100;
    private static final int GRAVEL_THRESHOLD = 200;

    /**
     * Runtime-only movement state. Wear itself is stored persistently in {@link PathWearData}.
     * Each player is tracked independently so multiple players can contribute to the same path.
     */
    private static final Map<UUID, StepLocation> LAST_STEP = new HashMap<>();

    private PathWearEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!player.onGround() || player.isSpectator() || player.isPassenger()) {
            return;
        }

        BlockPos groundPos = player.getOnPos().immutable();
        StepLocation currentStep = new StepLocation(level.dimension(), groundPos);
        StepLocation previousStep = LAST_STEP.put(player.getUUID(), currentStep);

        if (currentStep.equals(previousStep)) {
            return;
        }

        addWear(level, groundPos, 1);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_STEP.remove(event.getEntity().getUUID());
    }

    public static int addWear(ServerLevel level, BlockPos pos, int amount) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        PathWearData data = PathWearData.get(level);

        if (block == Blocks.FARMLAND) {
            data.clearWear(pos);
            return 0;
        }

        int threshold = thresholdFor(block);
        if (threshold <= 0) {
            data.clearWear(pos);
            return 0;
        }

        int visits = data.addWear(pos, amount, level.getGameTime());
        if (visits < threshold) {
            return visits;
        }

        Block nextBlock = nextBlockFor(level, pos, block);
        if (nextBlock == null) {
            data.clearWear(pos);
            return 0;
        }

        level.setBlockAndUpdate(pos, nextBlock.defaultBlockState());
        data.clearWear(pos);
        return 0;
    }

    public static int getWear(ServerLevel level, BlockPos pos) {
        return PathWearData.get(level).getWear(pos, level.getGameTime());
    }

    public static int getThreshold(ServerLevel level, BlockPos pos) {
        return thresholdFor(level.getBlockState(pos).getBlock());
    }

    private static int thresholdFor(Block block) {
        if (block == Blocks.GRASS_BLOCK) {
            return GRASS_THRESHOLD;
        }
        if (block == Blocks.PODZOL) {
            return PODZOL_THRESHOLD;
        }
        if (block == Blocks.MYCELIUM) {
            return MYCELIUM_THRESHOLD;
        }
        if (block == Blocks.DIRT_PATH) {
            return DIRT_PATH_THRESHOLD;
        }
        if (block == Blocks.ROOTED_DIRT) {
            return ROOTED_DIRT_THRESHOLD;
        }
        if (block == Blocks.COARSE_DIRT) {
            return COARSE_DIRT_THRESHOLD;
        }
        if (block == Blocks.GRAVEL) {
            return GRAVEL_THRESHOLD;
        }
        return -1;
    }

    private static Block nextBlockFor(ServerLevel level, BlockPos pos, Block block) {
        if (block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.MYCELIUM) {
            return Blocks.DIRT_PATH;
        }
        if (block == Blocks.DIRT_PATH) {
            return BiomePathProfiles.profileFor(level, pos) == BiomePathProfiles.PathProfile.FOREST
                    ? Blocks.ROOTED_DIRT
                    : Blocks.COARSE_DIRT;
        }
        if (block == Blocks.ROOTED_DIRT) {
            return Blocks.COARSE_DIRT;
        }
        if (block == Blocks.COARSE_DIRT) {
            return Blocks.GRAVEL;
        }
        if (block == Blocks.GRAVEL) {
            return Blocks.COBBLESTONE;
        }
        return null;
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
