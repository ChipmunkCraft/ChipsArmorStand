package com.deoxservices.chipsarmorstand.network;

import com.deoxservices.chipsarmorstand.utils.Utils;
import com.deoxservices.chipsarmorstand.data.ArmorStandData;
import com.deoxservices.chipsarmorstand.utils.Constants;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleArmorStandPacket(int entityId, String toggleType, boolean value) implements CustomPacketPayload {

    public ToggleArmorStandPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(), buf.readBoolean());
    }

    public static final Type<ToggleArmorStandPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_armor_stand"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleArmorStandPacket> TOGGLE_STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeUtf(packet.toggleType());
                buf.writeBoolean(packet.value());
            },
            ToggleArmorStandPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void serverHandle(ToggleArmorStandPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Entity entity = player.level().getEntity(msg.entityId());
            if (entity instanceof ArmorStand armorStand) {
                Utils.logMsg("Server toggling " + msg.toggleType() + " to " + msg.value() + " for ID: " + msg.entityId(), "debug");
                switch (msg.toggleType()) {
                    case "Locked" -> ArmorStandData.lockArmorStand(armorStand, player.getUUID(), msg.value());
                    case "Invisible" -> armorStand.setInvisible(msg.value());
                    case "ShowArms" -> armorStand.setShowArms(msg.value());
                    case "NoBasePlate" -> armorStand.setNoBasePlate(msg.value());
                    case "Small" -> ArmorStandData.setSmall(armorStand, msg.value());
                }
            }
        });
    }

    public static void clientHandle(ToggleArmorStandPacket msg, IPayloadContext ctx) {}
}