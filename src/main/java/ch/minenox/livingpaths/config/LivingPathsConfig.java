package ch.minenox.livingpaths.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common server-side configuration for Living Paths.
 *
 * <p>Thresholds describe the additional wear required for the block at its current stage.
 * The defaults preserve the original Living Paths behaviour.
 */
public final class LivingPathsConfig {

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 1_000_000;

    public static final ModConfigSpec.IntValue GRASS_THRESHOLD;
    public static final ModConfigSpec.IntValue MUD_THRESHOLD;
    public static final ModConfigSpec.IntValue PACKED_MUD_THRESHOLD;
    public static final ModConfigSpec.IntValue PODZOL_THRESHOLD;
    public static final ModConfigSpec.IntValue MYCELIUM_THRESHOLD;
    public static final ModConfigSpec.IntValue DIRT_PATH_THRESHOLD;
    public static final ModConfigSpec.IntValue MOSS_THRESHOLD;
    public static final ModConfigSpec.IntValue ROOTED_DIRT_THRESHOLD;
    public static final ModConfigSpec.IntValue COARSE_DIRT_THRESHOLD;
    public static final ModConfigSpec.IntValue GRAVEL_THRESHOLD;
    public static final ModConfigSpec.IntValue STONE_THRESHOLD;

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                "Wear thresholds in ground-block crossings.",
                "Each value is the additional traffic required while a block is in that stage."
        ).push("wear_thresholds");

        GRASS_THRESHOLD = threshold(builder, "grass_block", 25,
                "Grass Block -> Dirt Path or the biome-specific alternative.");
        MUD_THRESHOLD = threshold(builder, "mud", 50,
                "Mud -> Packed Mud.");
        PACKED_MUD_THRESHOLD = threshold(builder, "packed_mud", 75,
                "Packed Mud -> the biome-specific compacted path block.");
        PODZOL_THRESHOLD = threshold(builder, "podzol", 75,
                "Podzol -> Dirt Path.");
        MYCELIUM_THRESHOLD = threshold(builder, "mycelium", 75,
                "Mycelium -> Dirt Path.");
        DIRT_PATH_THRESHOLD = threshold(builder, "dirt_path", 50,
                "Dirt Path -> the biome-specific established path block.");
        MOSS_THRESHOLD = threshold(builder, "moss_block", 75,
                "Moss Block -> Rooted Dirt.");
        ROOTED_DIRT_THRESHOLD = threshold(builder, "rooted_dirt", 75,
                "Rooted Dirt -> Coarse Dirt.");
        COARSE_DIRT_THRESHOLD = threshold(builder, "coarse_dirt", 100,
                "Coarse Dirt -> Gravel.");
        GRAVEL_THRESHOLD = threshold(builder, "gravel", 200,
                "Gravel -> Cobblestone or Mossy Cobblestone.");
        STONE_THRESHOLD = threshold(builder, "stone", 500,
                "Stone -> Cobblestone or Mossy Cobblestone.");

        builder.pop();
        SPEC = builder.build();
    }

    private LivingPathsConfig() {
    }

    private static ModConfigSpec.IntValue threshold(
            ModConfigSpec.Builder builder,
            String name,
            int defaultValue,
            String comment
    ) {
        return builder.comment(comment)
                .defineInRange(name, defaultValue, MIN_THRESHOLD, MAX_THRESHOLD);
    }
}
