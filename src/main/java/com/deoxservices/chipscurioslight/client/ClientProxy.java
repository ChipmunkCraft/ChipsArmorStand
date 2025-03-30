package com.deoxservices.chipscurioslight.client;

import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientProxy {

    public static KeyMapping OPEN_ARMOR_STAND_MENU = new KeyMapping("key.chipscurioslight.open_armor_stand_menu", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, "key.categories." + Constants.MOD_ID);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_ARMOR_STAND_MENU);
        Utils.logMsg("Registered key mapping for Shift + Right Click", "debug");
    }
}