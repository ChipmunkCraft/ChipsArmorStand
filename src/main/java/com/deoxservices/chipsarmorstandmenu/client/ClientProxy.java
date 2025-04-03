package com.deoxservices.chipsarmorstandmenu.client;

import com.deoxservices.chipsarmorstandmenu.ChipsArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.client.gui.screen.ArmorStandScreen;
import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientProxy {
    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ChipsArmorStandMenu.ARMOR_STAND_MENU.get(), ArmorStandScreen::new);
        Utils.logMsg("Registered ArmorStandScreen for menu type: " + Constants.MOD_ID + ":armor_stand", "debug");
    }
}