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
     * A path edge receives one wear point for every four valid player steps. This keeps the directly
     * travelled line dominant while allowing softer shoulders to emerge over long-term repeated use.
     */
    private static final int EDGE_WEAR_STEP_INTERVAL = 4;

    /**
     * Runtime-only movement state. Wear itself is stored persistently in {@link PathWearData}.
     * Each player is tracked independently so multiple players can contribute to the same path.
     */
    private static final Map<UUID, StepLocation> LAST_STEP = new HashMap<>();
    private static final Map<UUID, Integer> EDGE_STEP_COUNTER = new HashMap<>();

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
        addOrganicEdgeWear(level, player.getUUID(), previousStep, currentStep);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        LAST_STEP.remove(playerId);
        EDGE_STEP_COUNTER.remove(playerId);
    }

    private static void addOrganicEdgeWear(
            ServerLevel level,
            UUID playerId,
            StepLocation previousStep,
            StepLocation currentStep
    ) {
        if (previousStep == null || previousStep.dimension() != currentStep.dimension()) {
            return;
        }

        int dx = currentStep.pos().getX() - previousStep.pos().getX();
        int dz = currentStep.pos().getZ() - previousStep.pos().getZ();
        int dy = currentStep.pos().getY() - previousStep.pos().getY();

        // Ignore teleports and other large position changes. Normal walking changes X/Z by at most one block.
        if ((dx == 0 && dz == 0)
                || Math.abs(dx) > 1
                || Math.abs(dz) > 1
                || Math.abs(dy) > 1) {
            return;
        }

        int stepCount = EDGE_STEP_COUNTER.merge(playerId, 1, Integer::sum);
        if (stepCount % EDGE_WEAR_STEP_INTERVAL != 0) {
            return;
        }

        // A perpendicular vector points to the path shoulder. Alternate sides to avoid a permanent bias.
        int sideX = -Integer.signum(dz);
        int sideZ = Integer.signum(dx);
        if (((stepCount / EDGE_WEAR_STEP_INTERVAL) & 1) == 0) {
            sideX = -sideX;
            sideZ = -sideZ;
        }

        BlockPos edgePos = currentStep.pos().offset(sideX, 0, sideZ);
        addWear(level, edgePos, 1);
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

    private static Block nextBlockFor(ServerLevel level, BlockPos pos, Block block) {
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
                return BiomePathProfiles.usesDampMossVariation(level, pos)
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
                return BiomePathProfiles.usesDampMossVariation(level, pos)
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
                    && BiomePathProfiles.usesDampMossyCobblestoneVariation(level, pos)) {
                return Blocks.MOSSY_COBBLESTONE;
            }
            return Blocks.COBBLESTONE;
        }
        return null;
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
