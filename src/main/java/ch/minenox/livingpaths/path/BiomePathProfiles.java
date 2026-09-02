package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class BiomePathProfiles {

    private static final TagKey<Biome> FOREST_PATH_BIOMES = TagKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(LivingPaths.MOD_ID, "forest_path")
    );

    private static final TagKey<Biome> DAMP_PATH_BIOMES = TagKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(LivingPaths.MOD_ID, "damp_path")
    );

    private static final int FOREST_PODZOL_VARIATION_PERCENT = 35;
    private static final int DAMP_MOSS_VARIATION_PERCENT = 30;
    private static final int DAMP_MOSSY_COBBLESTONE_VARIATION_PERCENT = 45;

    private static final long MOSS_VARIATION_SALT = 0x4D4F5353L;
    private static final long MOSSY_COBBLESTONE_VARIATION_SALT = 0x434F4242L;

    private BiomePathProfiles() {
    }

    public static PathProfile profileFor(ServerLevel level, BlockPos pos) {
        var biome = level.getBiome(pos);

        // Damp is intentionally checked first because some damp biomes may also be forest biomes.
        if (biome.is(DAMP_PATH_BIOMES)) {
            return PathProfile.DAMP;
        }
        if (biome.is(FOREST_PATH_BIOMES)) {
            return PathProfile.FOREST;
        }
        return PathProfile.OPEN_LAND;
    }

    /**
     * Forest paths deliberately keep some lightly travelled ground as Podzol before it becomes
     * a visible Dirt Path. The choice is derived from the block position so it is stable across
     * reloads and never rerolls just because the block is checked again.
     */
    public static boolean usesForestPodzolVariation(ServerLevel level, BlockPos pos) {
        return profileFor(level, pos) == PathProfile.FOREST
                && variationPercent(pos, 0L) < FOREST_PODZOL_VARIATION_PERCENT;
    }

    /**
     * Damp paths occasionally retain a mossy intermediate surface instead of immediately becoming
     * Rooted Dirt. This is deterministic per block position.
     */
    public static boolean usesDampMossVariation(ServerLevel level, BlockPos pos) {
        return profileFor(level, pos) == PathProfile.DAMP
                && variationPercent(pos, MOSS_VARIATION_SALT) < DAMP_MOSS_VARIATION_PERCENT;
    }

    /**
     * Old, heavily travelled damp paths may end as Mossy Cobblestone rather than plain Cobblestone.
     */
    public static boolean usesDampMossyCobblestoneVariation(ServerLevel level, BlockPos pos) {
        return profileFor(level, pos) == PathProfile.DAMP
                && variationPercent(pos, MOSSY_COBBLESTONE_VARIATION_SALT)
                < DAMP_MOSSY_COBBLESTONE_VARIATION_PERCENT;
    }

    private static int variationPercent(BlockPos pos, long salt) {
        return Math.floorMod(Long.hashCode(pos.asLong() ^ salt), 100);
    }

    public enum PathProfile {
        OPEN_LAND,
        FOREST,
        DAMP
    }
}
