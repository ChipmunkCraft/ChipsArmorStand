package com.deoxservices.chipscurioslight.client;

import com.deoxservices.chipscurioslight.ChipsCuriosLight;
import com.deoxservices.chipscurioslight.client.screen.ArmorStandScreen;
import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientProxy {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ChipsCuriosLight.ARMOR_STAND_MENU.get(), ArmorStandScreen::new);
        Utils.logMsg("Registered ArmorStandScreen for menu type: " + Constants.MOD_ID + ":armor_stand", "debug");
    }
}