package com.deoxservices.chipsarmorstandmenu.client;

import java.util.UUID;

import com.deoxservices.chipsarmorstandmenu.ChipsArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.client.config.ClientConfig;
import com.deoxservices.chipsarmorstandmenu.data.ArmorStandData;
import com.deoxservices.chipsarmorstandmenu.network.OpenArmorStandServerPacket;
import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
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
                    UUID owner = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get()).getOwner();
                    boolean locked = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get()).isLocked();
                    if (ArmorStandData.isArmorStandInUse(armorStand)) {
                        Utils.logMsg("Armor stand ID " + armorStand.getId() + " is in use, skipping", "debug");
                        event.setCanceled(true);
                        event.setSwingHand(false);
                        return;
                    }
                    Utils.logMsg("Hit armor stand, sending packet for Armor Stand ID: " + armorStand.getId(), "debug");
                    if (owner==null) {
                        owner = mc.player.getUUID();
                        Utils.logMsg("Armor Stand Owner is null, making " + mc.player.getUUID() + " owner of Armor Stand ID " + armorStand.getId(), "debug");
                    }
                    Utils.logMsg("Armor Stand ID: " + armorStand.getId() + " data | Owner: " + owner + " | InUse: " + true + " | Locked: " + locked + " | Invisible: " + armorStand.isInvisible() + " | Show Arms: " + armorStand.isShowArms() + " | No Base: " + armorStand.isNoBasePlate() + "Is Small: " + armorStand.isSmall(), "debug");
                    PacketDistributor.sendToServer(new OpenArmorStandServerPacket(armorStand.getId(), owner, true, locked, armorStand.isInvisible(), armorStand.isShowArms(), armorStand.isNoBasePlate(), armorStand.isSmall()));
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
}