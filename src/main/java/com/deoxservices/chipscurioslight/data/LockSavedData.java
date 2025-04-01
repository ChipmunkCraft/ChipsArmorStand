package com.deoxservices.chipscurioslight.data;

import com.deoxservices.chipscurioslight.client.ClientProxyGameEvents;
import com.deoxservices.chipscurioslight.utils.Utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;

public class LockSavedData extends SavedData {
    private final Map<Integer, ClientProxyGameEvents.LockInfo> lockedArmorStands;

    public LockSavedData() {
        this.lockedArmorStands = new HashMap<>();
    }

    public LockSavedData(Map<Integer, ClientProxyGameEvents.LockInfo> lockedArmorStands) {
        this.lockedArmorStands = lockedArmorStands;
    }

    public Map<Integer, ClientProxyGameEvents.LockInfo> getLockedArmorStands() {
        return lockedArmorStands;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag locks = new CompoundTag();
        for (Map.Entry<Integer, ClientProxyGameEvents.LockInfo> entry : lockedArmorStands.entrySet()) {
            locks.put(String.valueOf(entry.getKey()), entry.getValue().save());
        }
        tag.put("Locks", locks);
        Utils.logMsg("Saved armor stand locks to NBT", "debug");
        return tag;
    }

    public static LockSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        Map<Integer, ClientProxyGameEvents.LockInfo> lockedArmorStands = new HashMap<>();
        if (tag.contains("Locks")) {
            CompoundTag locks = tag.getCompound("Locks");
            for (String key : locks.getAllKeys()) {
                int id = Integer.parseInt(key);
                lockedArmorStands.put(id, ClientProxyGameEvents.LockInfo.load(locks.getCompound(key)));
                Utils.logMsg("Loaded lock for armor stand ID: " + id, "debug");
            }
        }
        return new LockSavedData(lockedArmorStands);
    }

    public static final Factory<LockSavedData> FACTORY = new Factory<LockSavedData>(
        LockSavedData::new,
        LockSavedData::load
    );
}