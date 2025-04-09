package com.deoxservices.chipsarmorstand.compat.curios;

import com.deoxservices.chipsarmorstand.utils.Constants;
import com.deoxservices.chipsarmorstand.utils.Utils;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class CuriosCompat {

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        Utils.logMsg("On Register Capabilities fired...", "debug");
        event.registerEntity(
            CuriosCapability.INVENTORY,
            EntityType.ARMOR_STAND,
            (entity, context) -> new ArmorStandCuriosHandler()
        );
    }
}