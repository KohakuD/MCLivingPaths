package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.LivingPaths;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class DebugHudNetworking {

    private DebugHudNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                DebugHudPayload.TYPE,
                DebugHudPayload.STREAM_CODEC,
                (payload, context) -> DebugHudState.update(payload)
        );
    }
}
