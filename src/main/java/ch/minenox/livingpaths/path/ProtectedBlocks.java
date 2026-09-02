package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.config.LivingPathsConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * Resolves configured protected block IDs against Minecraft's block registry.
 */
public final class ProtectedBlocks {

    private ProtectedBlocks() {
    }

    public static boolean contains(Block block) {
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        return LivingPathsConfig.PROTECTED_BLOCKS.get().stream()
                .map(Identifier::tryParse)
                .anyMatch(blockId::equals);
    }
}
