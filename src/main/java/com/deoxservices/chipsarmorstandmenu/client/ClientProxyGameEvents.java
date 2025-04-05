package com.deoxservices.chipsarmorstandmenu.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.deoxservices.chipsarmorstandmenu.client.config.ClientConfig;
import com.deoxservices.chipsarmorstandmenu.data.LockSavedData;
import com.deoxservices.chipsarmorstandmenu.network.OpenArmorStandMenuServerPacket;
import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientProxyGameEvents {

    @SuppressWarnings("null")
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        Utils.logMsg("Interaction event fired, key: " + event.getKeyMapping().getName(), "debug");
        ClientConfig.ModifierKey modifier = ClientConfig.CONFIG.ARMOR_STAND_MENU_MODIFIER.get();
        if (event.getKeyMapping() == mc.options.keyUse && modifier.isActive() && !event.isCanceled()) {
            Utils.logMsg("Shift + Use (Right Click) detected", "debug");
            if (Minecraft.getInstance().hitResult instanceof EntityHitResult entityHitResult) {
                Entity target = entityHitResult.getEntity();
                if (target instanceof ArmorStand armorStand) {
                    if (isArmorStandLocked(armorStand.getId(), mc.player.getUUID())) {
                        Utils.logMsg("Armor stand ID " + armorStand.getId() + " is locked, skipping", "debug");
                        event.setCanceled(true);
                        event.setSwingHand(false);
                        return;
                    }
                    Utils.logMsg("Hit armor stand, sending packet for ID: " + armorStand.getId(), "debug");
                    PacketDistributor.sendToServer(new OpenArmorStandMenuServerPacket(armorStand.getId(), armorStand.isShowArms(), false));
                    event.setCanceled(true);
                    event.setSwingHand(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        Utils.logMsg("Chips Armor Stand Menu started", "info");
    }

    private static LockSavedData lockData;
    private static final String NBT_KEY = "ChipsArmorStandMenuLocks";

    public static class LockInfo {
        private final UUID ownerUUID;
        private boolean inUse;
        private final boolean ownerOnly;
        private final List<UUID> allowedPlayers;

        public LockInfo(UUID ownerUUID, boolean ownerOnly) {
            this.ownerUUID = ownerUUID;
            this.inUse = false;
            this.ownerOnly = ownerOnly;
            this.allowedPlayers = new ArrayList<>();
        }

        public UUID getOwnerUUID() { return ownerUUID; }
        public boolean isInUse() { return inUse; }
        public void setInUse(boolean inUse) { this.inUse = inUse; }
        public boolean isOwnerOnly() { return ownerOnly; }
        public List<UUID> getAllowedPlayers() { return allowedPlayers; }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Owner", ownerUUID);
            tag.putBoolean("InUse", inUse);
            tag.putBoolean("OwnerOnly", ownerOnly);
            ListTag allowedList = new ListTag();
            for (UUID uuid : allowedPlayers) {
                allowedList.add(StringTag.valueOf(uuid.toString()));
            }
            tag.put("AllowedPlayers", allowedList);
            return tag;
        }

        public static LockInfo load(CompoundTag tag) {
            UUID ownerUUID = tag.getUUID("Owner");
            LockInfo info = new LockInfo(ownerUUID, tag.getBoolean("OwnerOnly"));
            info.inUse = tag.getBoolean("InUse");
            ListTag allowedList = tag.getList("AllowedPlayers", net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < allowedList.size(); i++) {
                info.allowedPlayers.add(UUID.fromString(allowedList.getString(i)));
            }
            return info;
        }
    }

    public static boolean lockArmorStand(int entityId, UUID playerUUID, boolean ownerOnly) {
        Map<Integer, LockInfo> locks = lockData.getLockedArmorStands();
        LockInfo existing = locks.get(entityId);
        if (existing != null) {
            if (existing.isInUse() || (existing.isOwnerOnly() && !existing.getOwnerUUID().equals(playerUUID))) {
                return false;
            }
            existing.setInUse(true);
            lockData.setDirty(); // Mark for save
            Utils.logMsg("Armor stand ID: " + entityId + " now in use by: " + playerUUID, "debug");
            return true;
        }
        LockInfo info = new LockInfo(playerUUID, ownerOnly);
        info.setInUse(true);
        locks.put(entityId, info);
        lockData.setDirty();
        Utils.logMsg("Locked armor stand ID: " + entityId + " by player: " + playerUUID, "debug");
        return true;
    }

    public static void unlockArmorStand(int entityId) {
        Map<Integer, LockInfo> locks = lockData.getLockedArmorStands();
        LockInfo info = locks.get(entityId);
        if (info != null) {
            info.setInUse(false);
            if (!info.isOwnerOnly()) {
                locks.remove(entityId);
                Utils.logMsg("Unlocked and removed armor stand ID: " + entityId, "debug");
            } else {
                Utils.logMsg("Unlocked armor stand ID: " + entityId + ", remains owner-locked", "debug");
            }
            lockData.setDirty();
        }
    }

    public static boolean isArmorStandLocked(int entityId, UUID playerUUID) {
        LockInfo info = lockData.getLockedArmorStands().get(entityId);
        if (info == null) return false;
        if (!info.isInUse()) return false;
        if (info.getOwnerUUID().equals(playerUUID) || info.getAllowedPlayers().contains(playerUUID)) return false;
        return true;
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            lockData = serverLevel.getDataStorage().computeIfAbsent(LockSavedData.FACTORY, NBT_KEY);
            Utils.logMsg("Loaded lock data for world", "debug");
        }
    }
}