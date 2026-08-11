package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
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

    private static final int GRASS_THRESHOLD = 25;
    private static final int MUD_THRESHOLD = 50;
    private static final int PACKED_MUD_THRESHOLD = 75;
    private static final int PODZOL_THRESHOLD = 75;
    private static final int MYCELIUM_THRESHOLD = 75;
    private static final int DIRT_PATH_THRESHOLD = 50;
    private static final int MOSS_THRESHOLD = 75;
    private static final int ROOTED_DIRT_THRESHOLD = 75;
    private static final int COARSE_DIRT_THRESHOLD = 100;
    private static final int GRAVEL_THRESHOLD = 200;

    /**
     * Roughly one quarter of travelled positions contribute wear to a neighbouring shoulder.
     * The decision and side are derived from the block position so path width stays stable across reloads.
     */
    private static final int EDGE_WEAR_POSITION_PERCENT = 25;
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
        addOrganicEdgeWear(level, previousStep, currentStep);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_STEP.remove(event.getEntity().getUUID());
    }

    private static void addOrganicEdgeWear(
            ServerLevel level,
            StepLocation previousStep,
            StepLocation currentStep
    ) {
        if (previousStep == null || previousStep.dimension() != currentStep.dimension()) {
            return;
        }

        int dx = currentStep.pos().getX() - previousStep.pos().getX();
        int dz = currentStep.pos().getZ() - previousStep.pos().getZ();
        int dy = currentStep.pos().getY() - previousStep.pos().getY();

        if ((dx == 0 && dz == 0)
                || Math.abs(dx) > 1
                || Math.abs(dz) > 1
                || Math.abs(dy) > 1) {
            return;
        }

        if (variationPercent(currentStep.pos(), EDGE_WEAR_SALT) >= EDGE_WEAR_POSITION_PERCENT) {
            return;
        }

        int sideX = -Integer.signum(dz);
        int sideZ = Integer.signum(dx);

        // Canonicalise the perpendicular vector so walking the same path in reverse selects the same shoulder.
        if (sideX < 0 || (sideX == 0 && sideZ < 0)) {
            sideX = -sideX;
            sideZ = -sideZ;
        }

        if (variationPercent(currentStep.pos(), EDGE_SIDE_SALT) >= 50) {
            sideX = -sideX;
            sideZ = -sideZ;
        }

        BlockPos edgePos = currentStep.pos().offset(sideX, 0, sideZ);
        addWear(level, edgePos, 1, true);
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
            return GRASS_THRESHOLD;
        }
        if (block == Blocks.MUD) {
            return MUD_THRESHOLD;
        }
        if (block == Blocks.PACKED_MUD) {
            return PACKED_MUD_THRESHOLD;
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
        if (block == Blocks.MOSS_BLOCK) {
            return MOSS_THRESHOLD;
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
        if (block == Blocks.GRAVEL) {
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
