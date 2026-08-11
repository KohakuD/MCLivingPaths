package ch.minenox.livingpaths.client;

import ch.minenox.livingpaths.LivingPaths;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Physical-client entry point for Living Paths user-interface integration.
 */
@Mod(value = LivingPaths.MOD_ID, dist = Dist.CLIENT)
public final class LivingPathsClient {

    public LivingPathsClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
