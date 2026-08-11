package ch.minenox.livingpaths.debug;

import ch.minenox.livingpaths.LivingPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

        String title = "Living Paths Debug HUD";
        String profile = "Profile: " + snapshot.profile();
        String position = "Position: " + snapshot.position();
        String left = "Left:   " + snapshot.left();
        String centre = "Centre: " + snapshot.centre();
        String right = "Right:  " + snapshot.right();

        int width = Math.max(
                Math.max(font.width(title), font.width(profile)),
                Math.max(
                        Math.max(font.width(position), font.width(left)),
                        Math.max(font.width(centre), font.width(right))
                )
        ) + PADDING * 2;
        int height = PADDING * 2 + LINE_HEIGHT * 6;
        int x = 6;
        int y = 6;

        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        int textX = x + PADDING;
        int textY = y + PADDING;

        graphics.drawString(font, title, textX, textY, TITLE, true);
        graphics.drawString(font, profile, textX, textY + LINE_HEIGHT, TEXT, true);
        graphics.drawString(font, position, textX, textY + LINE_HEIGHT * 2, MUTED, true);
        graphics.drawString(font, left, textX, textY + LINE_HEIGHT * 3, TEXT, true);
        graphics.drawString(font, centre, textX, textY + LINE_HEIGHT * 4, TEXT, true);
        graphics.drawString(font, right, textX, textY + LINE_HEIGHT * 5, TEXT, true);
    }
}
