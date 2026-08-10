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
    private static final int COARSE_DIRT_THRESHOLD = 100;
    private static final int GRAVEL_THRESHOLD = 200;

    private static final Map<UUID, StepLocation> LAST_STEP = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> WEAR = new HashMap<>();

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

        recordWear(level, groundPos);
    }

    private static void recordWear(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.FARMLAND) {
            clearWear(level, pos);
            return;
        }

        int threshold = thresholdFor(block);
        if (threshold <= 0) {
            clearWear(level, pos);
            return;
        }

        Map<BlockPos, Integer> levelWear = WEAR.computeIfAbsent(level.dimension(), ignored -> new HashMap<>());
        int visits = levelWear.merge(pos.immutable(), 1, Integer::sum);

        if (visits < threshold) {
            return;
        }

        Block nextBlock = nextBlockFor(block);
        if (nextBlock == null) {
            levelWear.remove(pos);
            return;
        }

        level.setBlockAndUpdate(pos, nextBlock.defaultBlockState());
        levelWear.remove(pos);
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
        if (block == Blocks.COARSE_DIRT) {
            return COARSE_DIRT_THRESHOLD;
        }
        if (block == Blocks.GRAVEL) {
            return GRAVEL_THRESHOLD;
        }
        return -1;
    }

    private static Block nextBlockFor(Block block) {
        if (block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.MYCELIUM) {
            return Blocks.DIRT_PATH;
        }
        if (block == Blocks.DIRT_PATH) {
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

    private static void clearWear(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Integer> levelWear = WEAR.get(level.dimension());
        if (levelWear != null) {
            levelWear.remove(pos);
        }
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
