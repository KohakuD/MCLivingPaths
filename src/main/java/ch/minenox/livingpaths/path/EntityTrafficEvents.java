package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.LivingPaths;
import ch.minenox.livingpaths.config.LivingPathsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Adds path wear from selected traffic mobs without scanning all loaded entities.
 *
 * <p>The entity tick event supplies only the entity that already ticked. Tracking its last ground
 * position lets us count real block-to-block movement while keeping stationary mobs free of wear.
 * Vanilla animals are excluded by default so livestock enclosures keep their natural ground, but
 * can be enabled explicitly in the common configuration. MineColonies Citizens and the Player Two
 * companion are detected by their registered entity ids, keeping both integrations optional.
 */
@EventBusSubscriber(modid = LivingPaths.MOD_ID)
public final class EntityTrafficEvents {

    private static final String MINECOLONIES_NAMESPACE = "minecolonies";
    private static final String MINECOLONIES_CITIZEN_PATH = "citizen";
    private static final String PLAYER_TWO_NAMESPACE = "playertwo";
    private static final String PLAYER_TWO_COMPANION_PATH = "nox";
    private static final Map<UUID, StepLocation> LAST_STEP = new HashMap<>();
    private static final Set<UUID> TRACKED_CITIZENS = new HashSet<>();
    private static final Set<UUID> TRACKED_PLAYER_TWO_COMPANIONS = new HashSet<>();
    private static long countedCrossings;
    private static long appliedWear;
    private static long citizenCrossings;
    private static long playerTwoCompanionCrossings;

    private EntityTrafficEvents() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)
                || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        UUID mobId = mob.getUUID();
        boolean mineColoniesCitizen = isMineColoniesCitizen(mob);
        boolean playerTwoCompanion = isPlayerTwoCompanion(mob);
        if (!isSelectedTrafficMob(mob)
                || !mob.isAlive()
                || !mob.onGround()
                || mob.isPassenger()) {
            LAST_STEP.remove(mobId);
            TRACKED_CITIZENS.remove(mobId);
            TRACKED_PLAYER_TWO_COMPANIONS.remove(mobId);
            return;
        }

        if (mineColoniesCitizen) {
            TRACKED_CITIZENS.add(mobId);
        } else {
            TRACKED_CITIZENS.remove(mobId);
        }
        if (playerTwoCompanion) {
            TRACKED_PLAYER_TWO_COMPANIONS.add(mobId);
        } else {
            TRACKED_PLAYER_TWO_COMPANIONS.remove(mobId);
        }

        BlockPos groundPos = mob.getOnPos().immutable();
        StepLocation currentStep = new StepLocation(level.dimension(), groundPos);
        StepLocation previousStep = LAST_STEP.put(mobId, currentStep);

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
        if (mineColoniesCitizen) {
            citizenCrossings++;
        }
        if (playerTwoCompanion) {
            playerTwoCompanionCrossings++;
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        UUID entityId = event.getEntity().getUUID();
        LAST_STEP.remove(entityId);
        TRACKED_CITIZENS.remove(entityId);
        TRACKED_PLAYER_TWO_COMPANIONS.remove(entityId);
    }

    public static DebugSnapshot debugSnapshot() {
        return new DebugSnapshot(
                LAST_STEP.size(),
                countedCrossings,
                appliedWear,
                TRACKED_CITIZENS.size(),
                citizenCrossings,
                TRACKED_PLAYER_TWO_COMPANIONS.size(),
                playerTwoCompanionCrossings
        );
    }

    private static boolean isSelectedTrafficMob(PathfinderMob mob) {
        if (!LivingPathsConfig.ENTITY_TRAFFIC_ENABLED.get()) {
            return false;
        }

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if ("minecraft".equals(entityId.getNamespace())) {
            if (mob instanceof Animal) {
                return LivingPathsConfig.ANIMAL_TRAFFIC_ENABLED.get();
            }
            return LivingPathsConfig.VANILLA_MOB_TRAFFIC_ENABLED.get();
        }
        if (isMineColoniesCitizen(entityId)) {
            return LivingPathsConfig.MINECOLONIES_CITIZEN_TRAFFIC_ENABLED.get();
        }
        return isPlayerTwoCompanion(entityId)
                && LivingPathsConfig.PLAYER_TWO_COMPANION_TRAFFIC_ENABLED.get();
    }

    private static boolean isMineColoniesCitizen(PathfinderMob mob) {
        return isMineColoniesCitizen(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }

    private static boolean isMineColoniesCitizen(Identifier entityId) {
        return MINECOLONIES_NAMESPACE.equals(entityId.getNamespace())
                && MINECOLONIES_CITIZEN_PATH.equals(entityId.getPath());
    }

    private static boolean isPlayerTwoCompanion(PathfinderMob mob) {
        return isPlayerTwoCompanion(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }

    private static boolean isPlayerTwoCompanion(Identifier entityId) {
        return PLAYER_TWO_NAMESPACE.equals(entityId.getNamespace())
                && PLAYER_TWO_COMPANION_PATH.equals(entityId.getPath());
    }

    private static boolean isAdjacentStep(BlockPos previous, BlockPos current) {
        return Math.abs(current.getX() - previous.getX()) <= 1
                && Math.abs(current.getY() - previous.getY()) <= 1
                && Math.abs(current.getZ() - previous.getZ()) <= 1;
    }

    private static int wearWeightFor(PathfinderMob mob) {
        if (isPlayerTwoCompanion(mob)) {
            return LivingPathsConfig.PLAYER_TWO_COMPANION_WEIGHT.get();
        }
        if (isMineColoniesCitizen(mob)) {
            return LivingPathsConfig.MINECOLONIES_CITIZEN_WEIGHT.get();
        }
        if (mob instanceof Animal) {
            return LivingPathsConfig.ANIMAL_ENTITY_WEIGHT.get();
        }

        EntityType<?> type = mob.getType();
        if (type == EntityTypes.IRON_GOLEM
                || type == EntityTypes.RAVAGER
                || type == EntityTypes.WARDEN) {
            return LivingPathsConfig.HEAVY_ENTITY_WEIGHT.get();
        }
        return LivingPathsConfig.NORMAL_ENTITY_WEIGHT.get();
    }

    private record StepLocation(ResourceKey<Level> dimension, BlockPos pos) {
    }

    public record DebugSnapshot(
            int trackedEntities,
            long countedCrossings,
            long appliedWear,
            int trackedCitizens,
            long citizenCrossings,
            int trackedPlayerTwoCompanions,
            long playerTwoCompanionCrossings
    ) {
    }
}
