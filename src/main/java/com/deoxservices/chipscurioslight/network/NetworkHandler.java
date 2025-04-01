package com.deoxservices.chipscurioslight.network;

import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArmorStandMenuPacket> OPEN_STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                buf.writeBoolean(packet.showArms());
                buf.writeInt(packet.entityId());
                buf.writeBoolean(packet.ownerOnly());
            },
            OpenArmorStandMenuPacket::new
        );

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleArmorStandPacket> TOGGLE_STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeUtf(packet.toggleType());
                buf.writeBoolean(packet.value());
            },
            ToggleArmorStandPacket::new
        );

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playToServer(
            OpenArmorStandMenuPacket.TYPE,
            OPEN_STREAM_CODEC,
            new DirectionalPayloadHandler<>(OpenArmorStandMenuPacket::clientHandle, OpenArmorStandMenuPacket::serverHandle)
        );
        registrar.playToServer(
            ToggleArmorStandPacket.TYPE,
            TOGGLE_STREAM_CODEC,
            new DirectionalPayloadHandler<>(ToggleArmorStandPacket::clientHandle, ToggleArmorStandPacket::serverHandle)
        );
        Utils.logMsg("Registered network packet handler", "debug");
    }
}