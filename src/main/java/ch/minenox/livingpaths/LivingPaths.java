package ch.minenox.livingpaths;

import ch.minenox.livingpaths.config.LivingPathsConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(LivingPaths.MOD_ID)
public final class LivingPaths {

    public static final String MOD_ID = "livingpaths";

    public LivingPaths(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, LivingPathsConfig.SPEC);
    }
}
