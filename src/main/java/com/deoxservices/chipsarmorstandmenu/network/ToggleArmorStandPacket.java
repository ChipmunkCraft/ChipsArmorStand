package com.deoxservices.chipsarmorstandmenu.network;

import com.deoxservices.chipsarmorstandmenu.utils.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleArmorStandPacket(int entityId, String toggleType, boolean value) implements CustomPacketPayload {
    public static final Type<ToggleArmorStandPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("chipsarmorstandmenu", "toggle_armor_stand"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleArmorStandPacket> TOGGLE_STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeUtf(packet.toggleType());
                buf.writeBoolean(packet.value());
            },
            ToggleArmorStandPacket::new
        );

    public ToggleArmorStandPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readUtf(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void clientHandle(ToggleArmorStandPacket msg, IPayloadContext ctx) {
        // No OP
    }

    public static void serverHandle(ToggleArmorStandPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Entity entity = player.level().getEntity(msg.entityId());
            if (entity instanceof ArmorStand armorStand) {
                Utils.logMsg("Server toggling " + msg.toggleType() + " to " + msg.value() + " for ID: " + msg.entityId(), "debug");
                switch (msg.toggleType()) {
                    case "arms" -> armorStand.setShowArms(msg.value());
                    case "base" -> armorStand.setNoBasePlate(!msg.value()); // Inverted
                    case "stand" -> armorStand.setInvisible(!msg.value());
                }
            }
        });
    }
}