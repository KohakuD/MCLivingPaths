package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class BiomePathProfiles {

    private static final TagKey<Biome> FOREST_PATH_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(LivingPaths.MOD_ID, "forest_path")
    );

    private static final int FOREST_PODZOL_VARIATION_PERCENT = 35;

    private BiomePathProfiles() {
    }

    public static PathProfile profileFor(ServerLevel level, BlockPos pos) {
        if (level.getBiome(pos).is(FOREST_PATH_BIOMES)) {
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
        if (profileFor(level, pos) != PathProfile.FOREST) {
            return false;
        }

        int variation = Math.floorMod(Long.hashCode(pos.asLong()), 100);
        return variation < FOREST_PODZOL_VARIATION_PERCENT;
    }

    public enum PathProfile {
        OPEN_LAND,
        FOREST
    }
}
