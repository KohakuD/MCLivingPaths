package ch.minenox.livingpaths.path;

import ch.minenox.livingpaths.config.LivingPathsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public final class PathWearData extends SavedData {

    private static final String DATA_NAME = "livingpaths_wear";
    private static final String WEAR_KEY = "wear";
    private static final String POSITION_KEY = "position";
    private static final String VISITS_KEY = "visits";
    private static final String EDGE_VISITS_KEY = "edgeVisits";
    private static final String LAST_USED_KEY = "lastUsed";
    private static final String ESTABLISHED_KEY = "established";

    private static final long TICKS_PER_MINECRAFT_DAY = 24_000L;
    private static final long CLEANUP_INTERVAL_TICKS = TICKS_PER_MINECRAFT_DAY;

    private final Map<Long, WearEntry> wearByPosition = new HashMap<>();
    private long lastCleanupGameTime = Long.MIN_VALUE;

    public static PathWearData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PathWearData::new, PathWearData::load),
                DATA_NAME
        );
    }

    private static PathWearData load(CompoundTag tag, HolderLookup.Provider registries) {
        PathWearData data = new PathWearData();
        ListTag wearEntries = tag.getList(WEAR_KEY, Tag.TAG_COMPOUND);

        for (int index = 0; index < wearEntries.size(); index++) {
            CompoundTag entry = wearEntries.getCompound(index);
            long position = entry.getLong(POSITION_KEY);
            int visits = entry.getInt(VISITS_KEY);
            int edgeVisits = entry.contains(EDGE_VISITS_KEY, Tag.TAG_INT)
                    ? entry.getInt(EDGE_VISITS_KEY)
                    : 0;
            long lastUsed = entry.contains(LAST_USED_KEY, Tag.TAG_LONG)
                    ? entry.getLong(LAST_USED_KEY)
                    : 0L;
            boolean established = entry.contains(ESTABLISHED_KEY, Tag.TAG_BYTE)
                    && entry.getBoolean(ESTABLISHED_KEY);

            if (visits > 0 || established) {
                data.wearByPosition.put(
                        position,
                        new WearEntry(visits, Math.min(edgeVisits, visits), lastUsed, established)
                );
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag wearEntries = new ListTag();

        for (Map.Entry<Long, WearEntry> wearEntry : wearByPosition.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(POSITION_KEY, wearEntry.getKey());
            entry.putInt(VISITS_KEY, wearEntry.getValue().visits());
            entry.putInt(EDGE_VISITS_KEY, wearEntry.getValue().edgeVisits());
            entry.putLong(LAST_USED_KEY, wearEntry.getValue().lastUsedGameTime());
            entry.putBoolean(ESTABLISHED_KEY, wearEntry.getValue().established());
            wearEntries.add(entry);
        }

        tag.put(WEAR_KEY, wearEntries);
        return tag;
    }

    public int addWear(BlockPos pos, int amount, long gameTime, boolean edgeWear) {
        if (amount <= 0) {
            return getWear(pos, gameTime);
        }

        maybeCleanup(gameTime);

        long key = pos.asLong();
        WearEntry previous = wearByPosition.get(key);
        int currentWear = previous == null ? 0 : effectiveWear(previous, gameTime);
        int currentEdgeWear = previous == null ? 0 : effectiveEdgeWear(previous, gameTime);
        int updatedWear = currentWear + amount;
        int updatedEdgeWear = Math.min(updatedWear, currentEdgeWear + (edgeWear ? amount : 0));
        boolean established = previous != null && previous.established();

        wearByPosition.put(key, new WearEntry(updatedWear, updatedEdgeWear, gameTime, established));
        setDirty();
        return updatedWear;
    }

    public int getWear(BlockPos pos, long gameTime) {
        WearEntry entry = wearByPosition.get(pos.asLong());
        if (entry == null) {
            return 0;
        }

        int effectiveWear = effectiveWear(entry, gameTime);
        if (effectiveWear <= 0) {
            if (!entry.established()) {
                wearByPosition.remove(pos.asLong());
                setDirty();
            }
            return 0;
        }

        return effectiveWear;
    }

    public int getEdgeWear(BlockPos pos, long gameTime) {
        WearEntry entry = wearByPosition.get(pos.asLong());
        if (entry == null) {
            return 0;
        }

        int effectiveWear = effectiveWear(entry, gameTime);
        if (effectiveWear <= 0) {
            if (!entry.established()) {
                wearByPosition.remove(pos.asLong());
                setDirty();
            }
            return 0;
        }

        return Math.min(effectiveWear, effectiveEdgeWear(entry, gameTime));
    }

    public void clearWear(BlockPos pos) {
        if (wearByPosition.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    public void markEstablished(BlockPos pos, long gameTime) {
        wearByPosition.put(pos.asLong(), new WearEntry(0, 0, gameTime, true));
        setDirty();
    }

    public java.util.List<BlockPos> regenerationCandidates(long gameTime, int intervalDays, int limit) {
        long requiredInactiveTicks = (long) intervalDays * TICKS_PER_MINECRAFT_DAY;
        java.util.List<BlockPos> candidates = new java.util.ArrayList<>();

        for (Map.Entry<Long, WearEntry> mapEntry : wearByPosition.entrySet()) {
            WearEntry entry = mapEntry.getValue();
            long inactiveTicks = Math.max(0L, gameTime - entry.lastUsedGameTime());
            if (entry.established() && inactiveTicks >= requiredInactiveTicks) {
                candidates.add(BlockPos.of(mapEntry.getKey()));
                if (candidates.size() >= limit) {
                    break;
                }
            }
        }
        return candidates;
    }

    public void completeRegeneration(BlockPos pos, long gameTime, boolean remainsEstablished) {
        long key = pos.asLong();
        if (remainsEstablished) {
            wearByPosition.put(key, new WearEntry(0, 0, gameTime, true));
        } else {
            wearByPosition.remove(key);
        }
        setDirty();
    }

    public void ageWearForDebug(BlockPos pos, int days) {
        if (days <= 0) {
            return;
        }

        long key = pos.asLong();
        WearEntry entry = wearByPosition.get(key);
        if (entry == null) {
            return;
        }

        long agedLastUsed = entry.lastUsedGameTime() - days * TICKS_PER_MINECRAFT_DAY;
        wearByPosition.put(
                key,
                new WearEntry(entry.visits(), entry.edgeVisits(), agedLastUsed, entry.established())
        );
        setDirty();
    }

    private void maybeCleanup(long gameTime) {
        if (lastCleanupGameTime != Long.MIN_VALUE
                && gameTime - lastCleanupGameTime < CLEANUP_INTERVAL_TICKS) {
            return;
        }

        lastCleanupGameTime = gameTime;
        boolean removedAny = wearByPosition.entrySet().removeIf(
                entry -> !entry.getValue().established()
                        && effectiveWear(entry.getValue(), gameTime) <= 0
        );

        if (removedAny) {
            setDirty();
        }
    }

    private static int effectiveWear(WearEntry entry, long gameTime) {
        long decay = decaySince(entry, gameTime);
        return (int) Math.max(0L, (long) entry.visits() - decay);
    }

    private static int effectiveEdgeWear(WearEntry entry, long gameTime) {
        long decay = decaySince(entry, gameTime);
        return (int) Math.max(0L, (long) entry.edgeVisits() - decay);
    }

    private static long decaySince(WearEntry entry, long gameTime) {
        if (!LivingPathsConfig.WEAR_DECAY_ENABLED.get()) {
            return 0L;
        }

        long inactiveTicks = Math.max(0L, gameTime - entry.lastUsedGameTime());
        long inactiveDays = inactiveTicks / TICKS_PER_MINECRAFT_DAY;
        long decaySteps = inactiveDays / LivingPathsConfig.WEAR_DECAY_INTERVAL_DAYS.get();
        long decayAmount = LivingPathsConfig.WEAR_DECAY_AMOUNT.get();

        if (decaySteps > Long.MAX_VALUE / decayAmount) {
            return Long.MAX_VALUE;
        }
        return decaySteps * decayAmount;
    }

    private record WearEntry(int visits, int edgeVisits, long lastUsedGameTime, boolean established) {
    }
}
