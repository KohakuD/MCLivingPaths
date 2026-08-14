package ch.minenox.livingpaths.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-only configuration for Living Paths visual helpers.
 */
public final class LivingPathsClientConfig {

    public static final ModConfigSpec.BooleanValue DEBUG_HUD_ENABLED;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                "Client-side debug and testing helpers.",
                "These settings do not affect path creation or saved world data."
        ).translation("config.livingpaths.section.debug")
                .push("debug");

        DEBUG_HUD_ENABLED = builder.comment(
                "Whether the Living Paths debug HUD is visible."
        ).translation("config.livingpaths.debug.hud_enabled")
                .define("hud_enabled", false);

        builder.pop();
        SPEC = builder.build();
    }

    private LivingPathsClientConfig() {
    }
}
