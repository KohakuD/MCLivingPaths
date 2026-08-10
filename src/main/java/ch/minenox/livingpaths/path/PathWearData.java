package ch.minenox.livingpaths.path;

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
    private static final String LAST_USED_KEY = "lastUsed";

    private static final long TICKS_PER_MINECRAFT_DAY = 24_000L;
    private static final int WEAR_DECAY_PER_INACTIVE_DAY = 1;
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
            long lastUsed = entry.contains(LAST_USED_KEY, Tag.TAG_LONG)
                    ? entry.getLong(LAST_USED_KEY)
                    : 0L;

            if (visits > 0) {
                data.wearByPosition.put(position, new WearEntry(visits, lastUsed));
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
            entry.putLong(LAST_USED_KEY, wearEntry.getValue().lastUsedGameTime());
            wearEntries.add(entry);
        }

        tag.put(WEAR_KEY, wearEntries);
        return tag;
    }

    public int addWear(BlockPos pos, int amount, long gameTime) {
        if (amount <= 0) {
            return getWear(pos, gameTime);
        }

        maybeCleanup(gameTime);

        long key = pos.asLong();
        WearEntry previous = wearByPosition.get(key);
        int currentWear = previous == null ? 0 : effectiveWear(previous, gameTime);
        int updatedWear = currentWear + amount;

        wearByPosition.put(key, new WearEntry(updatedWear, gameTime));
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
            wearByPosition.remove(pos.asLong());
            setDirty();
            return 0;
        }

        return effectiveWear;
    }

    public void clearWear(BlockPos pos) {
        if (wearByPosition.remove(pos.asLong()) != null) {
            setDirty();
        }
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
        wearByPosition.put(key, new WearEntry(entry.visits(), agedLastUsed));
        setDirty();
    }

    private void maybeCleanup(long gameTime) {
        if (lastCleanupGameTime != Long.MIN_VALUE
                && gameTime - lastCleanupGameTime < CLEANUP_INTERVAL_TICKS) {
            return;
        }

        lastCleanupGameTime = gameTime;
        boolean removedAny = wearByPosition.entrySet().removeIf(
                entry -> effectiveWear(entry.getValue(), gameTime) <= 0
        );

        if (removedAny) {
            setDirty();
        }
    }

    private static int effectiveWear(WearEntry entry, long gameTime) {
        long inactiveTicks = Math.max(0L, gameTime - entry.lastUsedGameTime());
        long inactiveDays = inactiveTicks / TICKS_PER_MINECRAFT_DAY;
        long decay = inactiveDays * WEAR_DECAY_PER_INACTIVE_DAY;
        return (int) Math.max(0L, (long) entry.visits() - decay);
    }

    private record WearEntry(int visits, long lastUsedGameTime) {
    }
}
