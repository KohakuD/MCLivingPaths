package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.LivingPaths;
import ch.minenox.livingpaths.config.LivingPathsClientConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = LivingPaths.MOD_ID, value = Dist.CLIENT)
public final class DebugHudOverlay {

    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int BACKGROUND = 0xA0000000;
    private static final int TITLE = 0xFFFFAA00;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int MUTED = 0xFFCCCCCC;

    private DebugHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!LivingPathsClientConfig.DEBUG_HUD_ENABLED.get()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        DebugHudPayload snapshot = DebugHudState.latest();
        if (snapshot == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        var font = minecraft.font;
        var positionData = snapshot.position();
        var trafficTotals = snapshot.entityTraffic().totals();
        var integrationTraffic = snapshot.entityTraffic().integrations();

        Component title = Component.translatable("debug.livingpaths.hud.title");
        Component profile = Component.translatable(
                "debug.livingpaths.hud.profile",
                Component.translatable("debug.livingpaths.hud.profile." + snapshot.profile())
        );
        Component position = Component.translatable(
                "debug.livingpaths.hud.position",
                positionData.x(),
                positionData.y(),
                positionData.z(),
                Component.translatable("direction.minecraft." + positionData.direction())
        );
        Component entityTraffic = Component.translatable(
                "debug.livingpaths.hud.entity_traffic",
                trafficTotals.trackedEntities(),
                trafficTotals.countedCrossings(),
                trafficTotals.appliedWear(),
                integrationTraffic.trackedCitizens(),
                integrationTraffic.citizenCrossings(),
                integrationTraffic.trackedPlayerTwoCompanions(),
                integrationTraffic.playerTwoCompanionCrossings()
        );
        Component left = blockLine("debug.livingpaths.hud.left", snapshot.left());
        Component centre = blockLine("debug.livingpaths.hud.centre", snapshot.centre());
        Component right = blockLine("debug.livingpaths.hud.right", snapshot.right());

        int width = Math.max(
                Math.max(
                        Math.max(font.width(title), font.width(profile)),
                        Math.max(font.width(position), font.width(entityTraffic))
                ),
                Math.max(
                        Math.max(font.width(left), font.width(centre)),
                        font.width(right)
                )
        ) + PADDING * 2;
        int height = PADDING * 2 + LINE_HEIGHT * 7;
        int x = 6;
        int y = 6;

        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        int textX = x + PADDING;
        int textY = y + PADDING;

        graphics.drawString(font, title, textX, textY, TITLE, true);
        graphics.drawString(font, profile, textX, textY + LINE_HEIGHT, TEXT, true);
        graphics.drawString(font, position, textX, textY + LINE_HEIGHT * 2, MUTED, true);
        graphics.drawString(font, entityTraffic, textX, textY + LINE_HEIGHT * 3, MUTED, true);
        graphics.drawString(font, left, textX, textY + LINE_HEIGHT * 4, TEXT, true);
        graphics.drawString(font, centre, textX, textY + LINE_HEIGHT * 5, TEXT, true);
        graphics.drawString(font, right, textX, textY + LINE_HEIGHT * 6, TEXT, true);
    }

    private static Component blockLine(String translationKey, DebugHudPayload.BlockSnapshot snapshot) {
        var pos = snapshot.position();
        ResourceLocation blockId = ResourceLocation.parse(snapshot.blockId());
        Component blockName = Component.translatable(Util.makeDescriptionId("block", blockId));
        return Component.translatable(
                translationKey,
                pos.x(),
                pos.y(),
                pos.z(),
                blockName,
                snapshot.wear(),
                snapshot.threshold(),
                snapshot.edgeWear()
        );
    }
}
