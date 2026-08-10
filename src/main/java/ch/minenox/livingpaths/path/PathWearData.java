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

    private final Map<Long, Integer> wearByPosition = new HashMap<>();

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
            data.wearByPosition.put(entry.getLong(POSITION_KEY), entry.getInt(VISITS_KEY));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag wearEntries = new ListTag();

        for (Map.Entry<Long, Integer> wearEntry : wearByPosition.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(POSITION_KEY, wearEntry.getKey());
            entry.putInt(VISITS_KEY, wearEntry.getValue());
            wearEntries.add(entry);
        }

        tag.put(WEAR_KEY, wearEntries);
        return tag;
    }

    public int addWear(BlockPos pos, int amount) {
        if (amount <= 0) {
            return getWear(pos);
        }

        long key = pos.asLong();
        int visits = wearByPosition.merge(key, amount, Integer::sum);
        setDirty();
        return visits;
    }

    public int getWear(BlockPos pos) {
        return wearByPosition.getOrDefault(pos.asLong(), 0);
    }

    public void clearWear(BlockPos pos) {
        if (wearByPosition.remove(pos.asLong()) != null) {
            setDirty();
        }
    }
}
