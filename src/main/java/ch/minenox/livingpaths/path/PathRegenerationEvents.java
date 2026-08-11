package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import ch.minenox.livingpaths.config.LivingPathsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class PathRegenerationEvents {

    private static final long SCAN_INTERVAL_TICKS = 1_200L;
    private static final int MAX_CANDIDATES_PER_SCAN = 1_024;
    private static final int MAX_REGENERATIONS_PER_SCAN = 256;

    private PathRegenerationEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !LivingPathsConfig.REGENERATION_ENABLED.get()
                || level.getGameTime() % SCAN_INTERVAL_TICKS != 0L) {
            return;
        }

        PathWearData data = PathWearData.get(level);
        int regenerated = 0;

        for (BlockPos pos : data.regenerationCandidates(
                level.getGameTime(),
                LivingPathsConfig.REGENERATION_INTERVAL_DAYS.get(),
                MAX_CANDIDATES_PER_SCAN
        )) {
            if (regenerated >= MAX_REGENERATIONS_PER_SCAN) {
                break;
            }
            if (!level.hasChunkAt(pos)) {
                continue;
            }

            Block currentBlock = level.getBlockState(pos).getBlock();
            Block previousBlock = previousBlockFor(currentBlock);
            if (previousBlock == null) {
                data.clearWear(pos);
                continue;
            }

            level.setBlockAndUpdate(pos, previousBlock.defaultBlockState());
            data.completeRegeneration(
                    pos,
                    level.getGameTime(),
                    hasFurtherRegeneration(previousBlock)
            );
            regenerated++;
        }
    }

    private static Block previousBlockFor(Block block) {
        if (block == Blocks.COBBLESTONE) {
            return Blocks.GRAVEL;
        }
        if (block == Blocks.MOSSY_COBBLESTONE) {
            return Blocks.MOSS_BLOCK;
        }
        if (block == Blocks.GRAVEL) {
            return Blocks.COARSE_DIRT;
        }
        if (block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT) {
            return Blocks.DIRT_PATH;
        }
        if (block == Blocks.DIRT_PATH || block == Blocks.PODZOL || block == Blocks.MOSS_BLOCK) {
            return Blocks.GRASS_BLOCK;
        }
        if (block == Blocks.PACKED_MUD) {
            return Blocks.MUD;
        }
        return null;
    }

    private static boolean hasFurtherRegeneration(Block block) {
        return block == Blocks.GRAVEL
                || block == Blocks.COARSE_DIRT
                || block == Blocks.DIRT_PATH
                || block == Blocks.MOSS_BLOCK;
    }
}
