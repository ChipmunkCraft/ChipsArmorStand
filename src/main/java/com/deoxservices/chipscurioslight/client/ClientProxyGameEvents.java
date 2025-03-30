package com.deoxservices.chipscurioslight.client;

import com.deoxservices.chipscurioslight.network.OpenArmorStandMenuPacket;
import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientProxyGameEvents {

    @SubscribeEvent
    @SuppressWarnings("null")
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Utils.logMsg("Mouse button event fired, button: " + event.getButton() + ", modifiers: " + event.getModifiers(), "debug");
        Minecraft mc = Minecraft.getInstance();
        boolean isShiftDown = mc.options.keyShift.isDown();
        Utils.logMsg("Shift key active: " + isShiftDown, "debug");

        boolean keybindMatch = ClientProxy.OPEN_ARMOR_STAND_MENU.isActiveAndMatches(InputConstants.getKey(event.getButton(), -1));
        Utils.logMsg("Keybind isActiveAndMatches: " + keybindMatch + ", expected: Shift + Right Click", "debug");

        if (event.getButton() == InputConstants.MOUSE_BUTTON_RIGHT && isShiftDown) {
            Utils.logMsg("Manual check: Shift + Right Click detected", "debug");
            if (mc.player == null || mc.level == null || mc.hitResult == null) {
                Utils.logMsg("No player, level, or hit result available", "debug");
                return;
            }

            if (mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                Entity entity = ((net.minecraft.world.phys.EntityHitResult) mc.hitResult).getEntity();
                if (entity instanceof ArmorStand armorStand) {
                    Utils.logMsg("Hit armor stand, sending packet for ID: " + armorStand.getId(), "debug");
                    PacketDistributor.sendToServer(new OpenArmorStandMenuPacket(armorStand.getId()));
                    event.setCanceled(true);
                } else {
                    Utils.logMsg("Hit entity is not an armor stand: " + entity.getType(), "debug");
                }
            } else {
                Utils.logMsg("No entity hit with manual check, hit type: " + (mc.hitResult != null ? mc.hitResult.getType() : "null"), "debug");
            }
        } else {
            Utils.logMsg("Keybind check failed: button=" + event.getButton() + ", shift=" + isShiftDown, "debug");
        }
    }
}