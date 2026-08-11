package ch.minenox.livingpaths.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Common server-side configuration for Living Paths.
 *
 * <p>The defaults preserve the original Living Paths behaviour.
 */
public final class LivingPathsConfig {

    private static final int MIN_THRESHOLD = 1;
    private static final int MAX_THRESHOLD = 1_000_000;
    private static final int MAX_DECAY_INTERVAL_DAYS = 365_000;
    private static final int MAX_DECAY_AMOUNT = 1_000_000;
    private static final int MIN_ENTITY_WEIGHT = 1;
    private static final int MAX_ENTITY_WEIGHT = 1_000;
    private static final int MAX_REGENERATION_INTERVAL_DAYS = 365_000;

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
    public static final ModConfigSpec.IntValue COBBLESTONE_THRESHOLD;

    public static final ModConfigSpec.BooleanValue WEAR_DECAY_ENABLED;
    public static final ModConfigSpec.IntValue WEAR_DECAY_INTERVAL_DAYS;
    public static final ModConfigSpec.IntValue WEAR_DECAY_AMOUNT;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> PROTECTED_BLOCKS;

    public static final ModConfigSpec.BooleanValue ENTITY_TRAFFIC_ENABLED;
    public static final ModConfigSpec.BooleanValue VANILLA_MOB_TRAFFIC_ENABLED;
    public static final ModConfigSpec.BooleanValue MINECOLONIES_CITIZEN_TRAFFIC_ENABLED;
    public static final ModConfigSpec.IntValue NORMAL_ENTITY_WEIGHT;
    public static final ModConfigSpec.IntValue HEAVY_ENTITY_WEIGHT;
    public static final ModConfigSpec.IntValue MINECOLONIES_CITIZEN_WEIGHT;

    public static final ModConfigSpec.BooleanValue REGENERATION_ENABLED;
    public static final ModConfigSpec.IntValue REGENERATION_INTERVAL_DAYS;

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
                "Stone -> Smooth Stone.");
        COBBLESTONE_THRESHOLD = threshold(builder, "cobblestone", 1_000,
                "Established Cobblestone created by Living Paths -> Smooth Stone.");

        builder.pop();

        builder.comment(
                "Decay of stored wear after a position has not received traffic.",
                "One Minecraft day contains 24000 game ticks."
        ).push("wear_decay");

        WEAR_DECAY_ENABLED = builder.comment(
                "Whether unused stored wear gradually decreases."
        ).define("enabled", true);
        WEAR_DECAY_INTERVAL_DAYS = builder.comment(
                "Number of inactive Minecraft days between decay steps."
        ).defineInRange("interval_days", 1, 1, MAX_DECAY_INTERVAL_DAYS);
        WEAR_DECAY_AMOUNT = builder.comment(
                "Wear points removed at each decay step."
        ).defineInRange("amount", 1, 1, MAX_DECAY_AMOUNT);

        builder.pop();

        builder.comment(
                "Blocks that Living Paths must never wear or replace.",
                "Use namespaced block IDs such as minecraft:farmland or anothermod:block_name."
        ).push("protected_blocks");

        PROTECTED_BLOCKS = builder.comment(
                "Protected block IDs. Farmland is protected by default."
        ).defineListAllowEmpty(
                "blocks",
                List.of("minecraft:farmland"),
                () -> "minecraft:farmland",
                value -> value instanceof String id && ResourceLocation.tryParse(id) != null
        );

        builder.pop();

        builder.comment(
                "Traffic from non-player entities.",
                "Animals remain excluded so enclosures and grazing areas do not become paths."
        ).push("entity_traffic");

        ENTITY_TRAFFIC_ENABLED = builder.comment(
                "Master switch for all entity traffic wear."
        ).define("enabled", true);
        VANILLA_MOB_TRAFFIC_ENABLED = builder.comment(
                "Whether selected ground-based vanilla non-animal mobs contribute to wear."
        ).define("vanilla_mobs", true);
        MINECOLONIES_CITIZEN_TRAFFIC_ENABLED = builder.comment(
                "Whether MineColonies Citizens contribute to wear when MineColonies is installed."
        ).define("minecolonies_citizens", true);
        NORMAL_ENTITY_WEIGHT = entityWeight(builder, "normal_weight", 1,
                "Wear points from a normal vanilla mob crossing.");
        HEAVY_ENTITY_WEIGHT = entityWeight(builder, "heavy_weight", 2,
                "Wear points from Iron Golems, Ravagers and Wardens.");
        MINECOLONIES_CITIZEN_WEIGHT = entityWeight(builder, "citizen_weight", 1,
                "Wear points from a MineColonies Citizen crossing.");

        builder.pop();

        builder.comment(
                "Slow regeneration of established paths created by Living Paths.",
                "Naturally occurring and player-placed blocks are never registered for regeneration."
        ).push("regeneration");

        REGENERATION_ENABLED = builder.comment(
                "Whether unused established Living Paths gradually return towards natural ground."
        ).define("enabled", true);
        REGENERATION_INTERVAL_DAYS = builder.comment(
                "Inactive Minecraft days between individual regeneration stages."
        ).defineInRange("interval_days", 30, 1, MAX_REGENERATION_INTERVAL_DAYS);

        builder.pop();
        SPEC = builder.build();
    }

    private LivingPathsConfig() {
    }

    private static ModConfigSpec.IntValue entityWeight(
            ModConfigSpec.Builder builder,
            String name,
            int defaultValue,
            String comment
    ) {
        return builder.comment(comment)
                .defineInRange(name, defaultValue, MIN_ENTITY_WEIGHT, MAX_ENTITY_WEIGHT);
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
