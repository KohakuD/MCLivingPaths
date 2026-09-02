package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.LivingPaths;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugHudPayload(
        String profile,
        PositionSnapshot position,
        EntityTrafficSnapshot entityTraffic,
        BlockSnapshot left,
        BlockSnapshot centre,
        BlockSnapshot right
) implements CustomPacketPayload {

    public static final Type<DebugHudPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LivingPaths.MOD_ID, "debug_hud")
    );

    public static final StreamCodec<ByteBuf, DebugHudPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DebugHudPayload::profile,
            PositionSnapshot.STREAM_CODEC,
            DebugHudPayload::position,
            EntityTrafficSnapshot.STREAM_CODEC,
            DebugHudPayload::entityTraffic,
            BlockSnapshot.STREAM_CODEC,
            DebugHudPayload::left,
            BlockSnapshot.STREAM_CODEC,
            DebugHudPayload::centre,
            BlockSnapshot.STREAM_CODEC,
            DebugHudPayload::right,
            DebugHudPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record PositionSnapshot(int x, int y, int z, String direction) {

        private static final StreamCodec<ByteBuf, PositionSnapshot> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                PositionSnapshot::x,
                ByteBufCodecs.VAR_INT,
                PositionSnapshot::y,
                ByteBufCodecs.VAR_INT,
                PositionSnapshot::z,
                ByteBufCodecs.STRING_UTF8,
                PositionSnapshot::direction,
                PositionSnapshot::new
        );
    }

    public record EntityTrafficSnapshot(TrafficTotals totals, IntegrationTraffic integrations) {

        private static final StreamCodec<ByteBuf, EntityTrafficSnapshot> STREAM_CODEC = StreamCodec.composite(
                TrafficTotals.STREAM_CODEC,
                EntityTrafficSnapshot::totals,
                IntegrationTraffic.STREAM_CODEC,
                EntityTrafficSnapshot::integrations,
                EntityTrafficSnapshot::new
        );
    }

    public record TrafficTotals(int trackedEntities, long countedCrossings, long appliedWear) {

        private static final StreamCodec<ByteBuf, TrafficTotals> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                TrafficTotals::trackedEntities,
                ByteBufCodecs.VAR_LONG,
                TrafficTotals::countedCrossings,
                ByteBufCodecs.VAR_LONG,
                TrafficTotals::appliedWear,
                TrafficTotals::new
        );
    }

    public record IntegrationTraffic(
            int trackedCitizens,
            long citizenCrossings,
            int trackedPlayerTwoCompanions,
            long playerTwoCompanionCrossings
    ) {

        private static final StreamCodec<ByteBuf, IntegrationTraffic> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                IntegrationTraffic::trackedCitizens,
                ByteBufCodecs.VAR_LONG,
                IntegrationTraffic::citizenCrossings,
                ByteBufCodecs.VAR_INT,
                IntegrationTraffic::trackedPlayerTwoCompanions,
                ByteBufCodecs.VAR_LONG,
                IntegrationTraffic::playerTwoCompanionCrossings,
                IntegrationTraffic::new
        );
    }

    public record BlockSnapshot(BlockPosition position, String blockId, int wear, int edgeWear, int threshold) {

        private static final StreamCodec<ByteBuf, BlockSnapshot> STREAM_CODEC = StreamCodec.composite(
                BlockPosition.STREAM_CODEC,
                BlockSnapshot::position,
                ByteBufCodecs.STRING_UTF8,
                BlockSnapshot::blockId,
                ByteBufCodecs.VAR_INT,
                BlockSnapshot::wear,
                ByteBufCodecs.VAR_INT,
                BlockSnapshot::edgeWear,
                ByteBufCodecs.VAR_INT,
                BlockSnapshot::threshold,
                BlockSnapshot::new
        );
    }

    public record BlockPosition(int x, int y, int z) {

        private static final StreamCodec<ByteBuf, BlockPosition> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                BlockPosition::x,
                ByteBufCodecs.VAR_INT,
                BlockPosition::y,
                ByteBufCodecs.VAR_INT,
                BlockPosition::z,
                BlockPosition::new
        );
    }
}
