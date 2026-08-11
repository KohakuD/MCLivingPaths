package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import ch.minenox.livingpaths.config.LivingPathsConfig;
import ch.minenox.livingpaths.debug.DebugHudSync;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    /**
     * Two out of five travelled positions contribute wear to a neighbouring shoulder.
     * The decision and side are derived from the block position so path width stays stable across reloads.
     */
    private static final int EDGE_WEAR_POSITION_PERCENT = 40;
    private static final long EDGE_WEAR_SALT = 0x45444745L;
    private static final long EDGE_SIDE_SALT = 0x53494445L;

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

        if (player instanceof ServerPlayer serverPlayer) {
            DebugHudSync.sendIfDue(level, serverPlayer);
        }

        BlockPos groundPos = player.getOnPos().immutable();
        StepLocation currentStep = new StepLocation(level.dimension(), groundPos);
        StepLocation previousStep = LAST_STEP.put(player.getUUID(), currentStep);

        if (currentStep.equals(previousStep)) {
            return;
        }

        addWear(level, groundPos, 1, false);
        if (previousStep != null && previousStep.dimension() == currentStep.dimension()) {
            addOrganicEdgeWear(level, previousStep.pos(), currentStep.pos(), 1);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_STEP.remove(event.getEntity().getUUID());
    }

    public static void addOrganicEdgeWear(
            ServerLevel level,
            BlockPos previousPos,
            BlockPos currentPos,
            int amount
    ) {
        int dx = currentPos.getX() - previousPos.getX();
        int dz = currentPos.getZ() - previousPos.getZ();
        int dy = currentPos.getY() - previousPos.getY();

        if ((dx == 0 && dz == 0)
                || Math.abs(dx) > 1
                || Math.abs(dz) > 1
                || Math.abs(dy) > 1) {
            return;
        }

        if (variationPercent(currentPos, EDGE_WEAR_SALT) >= EDGE_WEAR_POSITION_PERCENT) {
            return;
        }

        int sideX = -Integer.signum(dz);
        int sideZ = Integer.signum(dx);

        // Canonicalise the perpendicular vector so walking the same path in reverse selects the same shoulder.
        if (sideX < 0 || (sideX == 0 && sideZ < 0)) {
            sideX = -sideX;
            sideZ = -sideZ;
        }

        if (variationPercent(currentPos, EDGE_SIDE_SALT) >= 50) {
            sideX = -sideX;
            sideZ = -sideZ;
        }

        BlockPos edgeColumn = currentPos.offset(sideX, 0, sideZ);
        BlockPos edgePos = findWearableSurface(level, edgeColumn);
        if (edgePos != null) {
            addWear(level, edgePos, amount, true);
        }
    }

    /**
     * Finds the nearby walkable surface for organic edge wear. The neighbouring terrain may be on
     * the same level, one block higher or one block lower than the directly travelled block.
     */
    public static BlockPos findWearableSurface(ServerLevel level, BlockPos referencePos) {
        BlockPos sameLevel = referencePos.immutable();
        if (isWearableExposedSurface(level, sameLevel)) {
            return sameLevel;
        }

        BlockPos oneHigher = referencePos.above();
        if (isWearableExposedSurface(level, oneHigher)) {
            return oneHigher;
        }

        BlockPos oneLower = referencePos.below();
        if (isWearableExposedSurface(level, oneLower)) {
            return oneLower;
        }

        return null;
    }

    private static boolean isWearableExposedSurface(ServerLevel level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (thresholdFor(block) <= 0 || block == Blocks.FARMLAND) {
            return false;
        }

        BlockPos above = pos.above();
        return level.getBlockState(above).getCollisionShape(level, above).isEmpty();
    }

    public static int addWear(ServerLevel level, BlockPos pos, int amount) {
        return addWear(level, pos, amount, false);
    }

    public static int addEdgeWear(ServerLevel level, BlockPos pos, int amount) {
        return addWear(level, pos, amount, true);
    }

    private static int addWear(ServerLevel level, BlockPos pos, int amount, boolean edgeWear) {
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

        int visits = data.addWear(pos, amount, level.getGameTime(), edgeWear);
        if (visits < threshold) {
            return visits;
        }

        int edgeVisits = data.getEdgeWear(pos, level.getGameTime());
        boolean edgeDominated = edgeVisits * 2 >= visits;
        Block nextBlock = nextBlockFor(level, pos, block, edgeDominated);
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

    public static int getEdgeWear(ServerLevel level, BlockPos pos) {
        return PathWearData.get(level).getEdgeWear(pos, level.getGameTime());
    }

    public static int getThreshold(ServerLevel level, BlockPos pos) {
        return thresholdFor(level.getBlockState(pos).getBlock());
    }

    private static int thresholdFor(Block block) {
        if (block == Blocks.GRASS_BLOCK) {
            return LivingPathsConfig.GRASS_THRESHOLD.get();
        }
        if (block == Blocks.MUD) {
            return LivingPathsConfig.MUD_THRESHOLD.get();
        }
        if (block == Blocks.PACKED_MUD) {
            return LivingPathsConfig.PACKED_MUD_THRESHOLD.get();
        }
        if (block == Blocks.PODZOL) {
            return LivingPathsConfig.PODZOL_THRESHOLD.get();
        }
        if (block == Blocks.MYCELIUM) {
            return LivingPathsConfig.MYCELIUM_THRESHOLD.get();
        }
        if (block == Blocks.DIRT_PATH) {
            return LivingPathsConfig.DIRT_PATH_THRESHOLD.get();
        }
        if (block == Blocks.MOSS_BLOCK) {
            return LivingPathsConfig.MOSS_THRESHOLD.get();
        }
        if (block == Blocks.ROOTED_DIRT) {
            return LivingPathsConfig.ROOTED_DIRT_THRESHOLD.get();
        }
        if (block == Blocks.COARSE_DIRT) {
            return LivingPathsConfig.COARSE_DIRT_THRESHOLD.get();
        }
        if (block == Blocks.GRAVEL) {
            return LivingPathsConfig.GRAVEL_THRESHOLD.get();
        }
        if (block == Blocks.STONE) {
            return LivingPathsConfig.STONE_THRESHOLD.get();
        }
        return -1;
    }

    private static Block nextBlockFor(ServerLevel level, BlockPos pos, Block block, boolean edgeDominated) {
        BiomePathProfiles.PathProfile profile = BiomePathProfiles.profileFor(level, pos);

        if (block == Blocks.GRASS_BLOCK) {
            if (BiomePathProfiles.usesForestPodzolVariation(level, pos)) {
                return Blocks.PODZOL;
            }
            return Blocks.DIRT_PATH;
        }
        if (block == Blocks.MUD) {
            return Blocks.PACKED_MUD;
        }
        if (block == Blocks.PACKED_MUD) {
            if (profile == BiomePathProfiles.PathProfile.DAMP) {
                return edgeDominated || BiomePathProfiles.usesDampMossVariation(level, pos)
                        ? Blocks.MOSS_BLOCK
                        : Blocks.ROOTED_DIRT;
            }
            return Blocks.COARSE_DIRT;
        }
        if (block == Blocks.PODZOL || block == Blocks.MYCELIUM) {
            return Blocks.DIRT_PATH;
        }
        if (block == Blocks.DIRT_PATH) {
            if (profile == BiomePathProfiles.PathProfile.DAMP) {
                return edgeDominated || BiomePathProfiles.usesDampMossVariation(level, pos)
                        ? Blocks.MOSS_BLOCK
                        : Blocks.ROOTED_DIRT;
            }
            if (profile == BiomePathProfiles.PathProfile.FOREST) {
                return Blocks.ROOTED_DIRT;
            }
            return Blocks.COARSE_DIRT;
        }
        if (block == Blocks.MOSS_BLOCK) {
            return Blocks.ROOTED_DIRT;
        }
        if (block == Blocks.ROOTED_DIRT) {
            return Blocks.COARSE_DIRT;
        }
        if (block == Blocks.COARSE_DIRT) {
            return Blocks.GRAVEL;
        }
        if (block == Blocks.GRAVEL || block == Blocks.STONE) {
            if (profile == BiomePathProfiles.PathProfile.DAMP
                    && (edgeDominated || BiomePathProfiles.usesDampMossyCobblestoneVariation(level, pos))) {
                return Blocks.MOSSY_COBBLESTONE;
            }
            return Blocks.COBBLESTONE;
        }
        return null;
    }

    private static int variationPercent(BlockPos pos, long salt) {
        return Math.floorMod(Long.hashCode(pos.asLong() ^ salt), 100);
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
