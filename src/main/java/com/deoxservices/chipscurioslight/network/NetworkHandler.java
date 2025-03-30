package com.deoxservices.chipscurioslight.network;

import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArmorStandMenuPacket> STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.entityId()), // Encode
            OpenArmorStandMenuPacket::new                     // Decode
        );

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playToServer(
            OpenArmorStandMenuPacket.TYPE,
            STREAM_CODEC,
            OpenArmorStandMenuPacket::handle
        );
        Utils.logMsg("Registered network packet handler", "debug");
    }
}