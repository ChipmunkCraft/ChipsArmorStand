package com.deoxservices.chipsarmorstandmenu.network;

import com.deoxservices.chipsarmorstandmenu.client.gui.screen.ArmorStandScreen;
import com.deoxservices.chipsarmorstandmenu.menu.ArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenArmorStandMenuClientPacket(int entityId, boolean showArms, boolean ownerOnly) implements CustomPacketPayload {

    public OpenArmorStandMenuClientPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static final Type<OpenArmorStandMenuClientPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "open_armor_stand_menu_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenArmorStandMenuClientPacket> OPEN_STREAM_CODEC =
        StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.entityId());
                buf.writeBoolean(packet.showArms());
                buf.writeBoolean(packet.ownerOnly());
            },
            OpenArmorStandMenuClientPacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void clientHandle(OpenArmorStandMenuClientPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = (LocalPlayer) ctx.player();
            //Entity entity = player.level().getEntity(msg.entityId());
            //if (entity instanceof ArmorStand armorStand) {
                // Create a RegistryFriendlyByteBuf with the packet data
                @SuppressWarnings("null")
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), mc.level.registryAccess(), null);
                buf.writeBoolean(msg.ownerOnly());
                buf.writeBoolean(msg.showArms());
                buf.writeInt(msg.entityId());
                Inventory playerInv = player.getInventory();
                Utils.logMsg("Creating Client ArmorStandMenu with ID: 0", "debug");
                Minecraft.getInstance().setScreen(new ArmorStandScreen(
                    new ArmorStandMenu(0, playerInv, null, msg.showArms(), msg.ownerOnly(), buf),
                    playerInv,
                    Component.literal("Armor Stand Menu")
            ));
            //}
        });
    }

    public static void serverHandle(OpenArmorStandMenuClientPacket msg, IPayloadContext ctx) {}
}