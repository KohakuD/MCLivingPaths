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

    private BiomePathProfiles() {
    }

    public static PathProfile profileFor(ServerLevel level, BlockPos pos) {
        if (level.getBiome(pos).is(FOREST_PATH_BIOMES)) {
            return PathProfile.FOREST;
        }
        return PathProfile.OPEN_LAND;
    }

    public enum PathProfile {
        OPEN_LAND,
        FOREST
    }
}
