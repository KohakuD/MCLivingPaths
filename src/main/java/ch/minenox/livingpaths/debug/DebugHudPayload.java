package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.LivingPaths;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DebugHudPayload(
        String profile,
        String position,
        String left,
        String centre,
        String right
) implements CustomPacketPayload {

    public static final Type<DebugHudPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(LivingPaths.MOD_ID, "debug_hud")
    );

    public static final StreamCodec<ByteBuf, DebugHudPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DebugHudPayload::profile,
            ByteBufCodecs.STRING_UTF8,
            DebugHudPayload::position,
            ByteBufCodecs.STRING_UTF8,
            DebugHudPayload::left,
            ByteBufCodecs.STRING_UTF8,
            DebugHudPayload::centre,
            ByteBufCodecs.STRING_UTF8,
            DebugHudPayload::right,
            DebugHudPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
