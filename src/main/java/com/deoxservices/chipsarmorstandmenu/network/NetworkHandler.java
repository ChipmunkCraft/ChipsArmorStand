package com.deoxservices.chipsarmorstandmenu.network;

import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static void register(final RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playToServer(
            OpenArmorStandMenuServerPacket.TYPE,
            OpenArmorStandMenuServerPacket.OPEN_STREAM_CODEC,
            new DirectionalPayloadHandler<>(OpenArmorStandMenuServerPacket::clientHandle, OpenArmorStandMenuServerPacket::serverHandle)
        );
        registrar.playToClient(
            OpenArmorStandMenuClientPacket.TYPE,
            OpenArmorStandMenuClientPacket.OPEN_STREAM_CODEC,
            new DirectionalPayloadHandler<>(OpenArmorStandMenuClientPacket::clientHandle, OpenArmorStandMenuClientPacket::serverHandle)
        );
        registrar.playToServer(
            ToggleArmorStandPacket.TYPE,
            ToggleArmorStandPacket.TOGGLE_STREAM_CODEC,
            new DirectionalPayloadHandler<>(ToggleArmorStandPacket::clientHandle, ToggleArmorStandPacket::serverHandle)
        );
        Utils.logMsg("Registered network packet handler", "debug");
    }
}