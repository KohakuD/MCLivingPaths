package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.path.BiomePathProfiles;
import ch.minenox.livingpaths.path.EntityTrafficEvents;
import ch.minenox.livingpaths.path.PathWearEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DebugHudSync {

    private static final int UPDATE_INTERVAL_TICKS = 5;

    private DebugHudSync() {
    }

    public static void sendIfDue(ServerLevel level, ServerPlayer player) {
        if (player.tickCount % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }

        BlockPos centre = player.getOnPos().immutable();
        Direction forward = player.getDirection();
        BlockPos leftColumn = centre.relative(forward.getCounterClockWise());
        BlockPos rightColumn = centre.relative(forward.getClockWise());
        BlockPos left = surfaceOrColumn(level, leftColumn);
        BlockPos right = surfaceOrColumn(level, rightColumn);
        var traffic = EntityTrafficEvents.debugSnapshot();

        PacketDistributor.sendToPlayer(player, new DebugHudPayload(
                BiomePathProfiles.profileFor(level, centre).name().toLowerCase(java.util.Locale.ROOT),
                new DebugHudPayload.PositionSnapshot(
                        centre.getX(), centre.getY(), centre.getZ(), forward.getName()
                ),
                new DebugHudPayload.EntityTrafficSnapshot(
                        new DebugHudPayload.TrafficTotals(
                                traffic.trackedEntities(),
                                traffic.countedCrossings(),
                                traffic.appliedWear()
                        ),
                        new DebugHudPayload.IntegrationTraffic(
                                traffic.trackedCitizens(),
                                traffic.citizenCrossings(),
                                traffic.trackedPlayerTwoCompanions(),
                                traffic.playerTwoCompanionCrossings()
                        )
                ),
                describe(level, left),
                describe(level, centre),
                describe(level, right)
        ));
    }

    private static BlockPos surfaceOrColumn(ServerLevel level, BlockPos referencePos) {
        BlockPos surface = PathWearEvents.findWearableSurface(level, referencePos);
        return surface != null ? surface : referencePos;
    }

    private static DebugHudPayload.BlockSnapshot describe(ServerLevel level, BlockPos pos) {
        var block = level.getBlockState(pos).getBlock();
        return new DebugHudPayload.BlockSnapshot(
                new DebugHudPayload.BlockPosition(pos.getX(), pos.getY(), pos.getZ()),
                BuiltInRegistries.BLOCK.getKey(block).toString(),
                PathWearEvents.getWear(level, pos),
                PathWearEvents.getEdgeWear(level, pos),
                PathWearEvents.getThreshold(level, pos)
        );
    }
}
