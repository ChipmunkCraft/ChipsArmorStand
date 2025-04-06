package com.deoxservices.chipsarmorstandmenu.network;

import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenArmorStandClientPacket(int entityId, boolean showArms, boolean ownerOnly) implements CustomPacketPayload {

    public OpenArmorStandClientPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static final Type<OpenArmorStandClientPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "open_armor_stand_menu_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArmorStandClientPacket> OPEN_STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeBoolean(packet.ownerOnly());
                buf.writeBoolean(packet.showArms());
            },
            OpenArmorStandClientPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void clientHandle(OpenArmorStandClientPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            // Create a RegistryFriendlyByteBuf with the packet data
            @SuppressWarnings("null")
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), mc.level.registryAccess(), null);
            buf.writeInt(msg.entityId());
            buf.writeBoolean(msg.showArms());
            buf.writeBoolean(msg.ownerOnly());
            Utils.logMsg("Buf check: " + buf.readableBytes(), "debug");
        });
    }

    public static void serverHandle(OpenArmorStandClientPacket msg, IPayloadContext ctx) {}
}