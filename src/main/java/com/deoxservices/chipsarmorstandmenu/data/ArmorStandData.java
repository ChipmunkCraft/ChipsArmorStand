package com.deoxservices.chipsarmorstandmenu.data;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.deoxservices.chipsarmorstandmenu.ChipsArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.utils.CodecUtil;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandData {
    private UUID owner;
    private final Map<String, Object> data;

    public ArmorStandData(UUID owner, Map<String, Object> data) {
        this.owner = owner;
        this.data = new HashMap<>(data);
    }

    // Getters
    public static boolean isArmorStandLocked(ArmorStand armorStand, UUID playerUUID) {
        ArmorStandData data = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get());
        return data.isLocked() && data.getOwner() != null && !data.getOwner().equals(playerUUID);
    }
    public static boolean isArmorStandInUse(ArmorStand armorStand) {
        ArmorStandData data = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get());
        return data.isInUse();
    }
    public UUID getOwner() {
        Utils.logMsg("Owner: " + owner, "debug");
        return owner;
    }
    public boolean isInUse() { return getBoolean("InUse"); }
    public boolean isInvisible() { return getBoolean("Invisible"); }
    public boolean isLocked() { return getBoolean("Locked"); }
    public boolean showArms() { return getBoolean("ShowArms"); }
    public boolean noBasePlate() { return getBoolean("NoBasePlate"); }
    public boolean isSmall() { return getBoolean("Small"); }
    public float[] getPose(String part) { return getFloatArray(part); }


    public boolean getBoolean(String key) {
        Utils.logMsg("Key " + key + " Exists: " + data.containsKey(key) + " | Value of Key: " + data.get(key), "debug");
        return data.containsKey(key) ? (Boolean) data.get(key) : false;
    }
    public float[] getFloatArray(String key) {
        return data.containsKey(key) ? (float[]) data.get(key) : new float[]{0.0f, 0.0f, 0.0f};
    }

    // Setters
    public static boolean lockArmorStand(ArmorStand armorStand, UUID playerUUID, boolean lock) {
        ArmorStandData data = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA);
        data.set("Locked", lock);
        if (lock) data.set("Owner", playerUUID);
        else data.set("Owner", null);
        armorStand.setData(ChipsArmorStandMenu.ARMOR_STAND_DATA, data);
        Utils.logMsg((lock ? "Locked" : "Unlocked") + " armor stand UUID: " + armorStand.getUUID() + " by player UUID: " + playerUUID, "debug");
        return true;
    }
    public static boolean setArmorStandInUse(ArmorStand armorStand, UUID playerUUID, boolean inUse) {
        ArmorStandData data = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA);
        data.set("InUse", inUse);
        armorStand.setData(ChipsArmorStandMenu.ARMOR_STAND_DATA, data);
        Utils.logMsg("Set InUse to " + inUse + " for armor stand UUID: " + armorStand.getUUID() + " by player UUID: " + playerUUID, "debug");
        return true;
    }
    public static void setSmall(ArmorStand armorStand, boolean small) {
        CompoundTag tag = new CompoundTag();
        armorStand.addAdditionalSaveData(tag); // Write current state to tag
        tag.putBoolean("Small", small);        // Update "Small"
        armorStand.readAdditionalSaveData(tag); // Reload NBT, calls setSmall internally
    }


    public void set(String key, Object value) {
        data.put(key, value);
    }

    // Serialization
    public static final Codec<ArmorStandData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(ad -> Optional.ofNullable(ad.getOwner())),
        Codec.unboundedMap(Codec.STRING, CodecUtil.OBJECT_CODEC).fieldOf("data").forGetter(ad -> ad.data)
    ).apply(instance, (ownerOpt, data) -> new ArmorStandData(ownerOpt.orElse(null), data)));
}
