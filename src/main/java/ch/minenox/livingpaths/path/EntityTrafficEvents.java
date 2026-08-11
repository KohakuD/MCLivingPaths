package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
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
 * Adds path wear from selected traffic mobs without scanning all loaded entities.
 *
 * <p>The entity tick event supplies only the entity that already ticked. Tracking its last ground
 * position lets us count real block-to-block movement while keeping stationary mobs free of wear.
 * Vanilla animals are deliberately excluded so livestock enclosures keep their natural ground.
 * MineColonies Citizens are detected by their registered entity id, keeping MineColonies optional.
 */
@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class EntityTrafficEvents {

    private static final String MINECOLONIES_NAMESPACE = "minecolonies";
    private static final String MINECOLONIES_CITIZEN_PATH = "citizen";
    private static final Map<UUID, StepLocation> LAST_STEP = new HashMap<>();
    private static long countedCrossings;
    private static long appliedWear;

    private EntityTrafficEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)
                || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        if (!isSelectedTrafficMob(mob)
                || !mob.isAlive()
                || !mob.onGround()
                || mob.isPassenger()) {
            LAST_STEP.remove(mob.getUUID());
            return;
        }

        BlockPos groundPos = mob.getOnPos().immutable();
        StepLocation currentStep = new StepLocation(level.dimension(), groundPos);
        StepLocation previousStep = LAST_STEP.put(mob.getUUID(), currentStep);

        if (previousStep == null
                || previousStep.dimension() != currentStep.dimension()
                || previousStep.pos().equals(currentStep.pos())
                || !isAdjacentStep(previousStep.pos(), currentStep.pos())) {
            return;
        }

        int wearWeight = wearWeightFor(mob);
        PathWearEvents.addWear(level, groundPos, wearWeight);
        PathWearEvents.addOrganicEdgeWear(
                level,
                previousStep.pos(),
                currentStep.pos(),
                wearWeight
        );
        countedCrossings++;
        appliedWear += wearWeight;
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        LAST_STEP.remove(event.getEntity().getUUID());
    }

    public static String debugSummary() {
        return LAST_STEP.size()
                + " tracked | "
                + countedCrossings
                + " crossings | "
                + appliedWear
                + " wear";
    }

    private static boolean isSelectedTrafficMob(PathfinderMob mob) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if ("minecraft".equals(entityId.getNamespace())) {
            return !(mob instanceof Animal);
        }
        return MINECOLONIES_NAMESPACE.equals(entityId.getNamespace())
                && MINECOLONIES_CITIZEN_PATH.equals(entityId.getPath());
    }

    private static boolean isAdjacentStep(BlockPos previous, BlockPos current) {
        return Math.abs(current.getX() - previous.getX()) <= 1
                && Math.abs(current.getY() - previous.getY()) <= 1
                && Math.abs(current.getZ() - previous.getZ()) <= 1;
    }

    private static int wearWeightFor(PathfinderMob mob) {
        EntityType<?> type = mob.getType();
        if (type == EntityType.IRON_GOLEM
                || type == EntityType.RAVAGER
                || type == EntityType.WARDEN) {
            return 2;
        }
        return 1;
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
