package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adds path wear from selected vanilla mobs without scanning all loaded entities.
 *
 * <p>The entity tick event supplies only the entity that already ticked. Tracking its last ground
 * position lets us count real block-to-block movement while keeping stationary mobs free of wear.
 * Animals are deliberately excluded so livestock enclosures keep their natural ground.
 */
@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class EntityTrafficEvents {

    private static final Map<UUID, StepLocation> LAST_STEP = new HashMap<>();

    private EntityTrafficEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)
                || !(mob.level() instanceof ServerLevel level)
                || !isSelectedVanillaMob(mob)
                || !mob.isAlive()
                || !mob.onGround()
                || mob.isPassenger()) {
            return;
        }

        BlockPos groundPos = mob.getOnPos().immutable();
        StepLocation currentStep = new StepLocation(level.dimension(), groundPos);
        StepLocation previousStep = LAST_STEP.put(mob.getUUID(), currentStep);

        if (previousStep == null
                || previousStep.dimension() != currentStep.dimension()
                || previousStep.pos().equals(currentStep.pos())) {
            return;
        }

        PathWearEvents.addWear(level, groundPos, 1);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        LAST_STEP.remove(event.getEntity().getUUID());
    }

    private static boolean isSelectedVanillaMob(PathfinderMob mob) {
        return !(mob instanceof Animal)
                && "minecraft".equals(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace());
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
