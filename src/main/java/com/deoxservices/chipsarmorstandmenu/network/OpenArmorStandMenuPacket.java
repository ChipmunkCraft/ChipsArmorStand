package com.deoxservices.chipsarmorstandmenu.network;

import com.deoxservices.chipsarmorstandmenu.client.ClientProxyGameEvents;
import com.deoxservices.chipsarmorstandmenu.menu.ArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenArmorStandMenuPacket(int entityId, boolean showArms, boolean ownerOnly) implements CustomPacketPayload {
    public static final Type<OpenArmorStandMenuPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "open_armor_stand_menu"));

    public OpenArmorStandMenuPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void serverHandle(OpenArmorStandMenuPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Entity entity = player.level().getEntity(msg.entityId());
            if (entity instanceof ArmorStand armorStand) {
                if (ClientProxyGameEvents.isArmorStandLocked(msg.entityId(), player.getUUID())) {
                    Utils.logMsg("Armor stand ID " + msg.entityId() + " is locked, denying player: " + player.getUUID(), "debug");
                    return;
                }

                if (ClientProxyGameEvents.lockArmorStand(msg.entityId(), player.getUUID(), msg.ownerOnly())) {
                    Utils.logMsg("Server received packet, opening menu for armor stand ID: " + msg.entityId(), "debug");

                    player.openMenu(new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return armorStand.getDisplayName();
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player p) {
                            Utils.logMsg("Creating ArmorStandMenu with ID: " + id, "debug");
                            return new ArmorStandMenu(id, inv, armorStand, msg.showArms());
                        }
                    }, buf -> {
                        buf.writeBoolean(msg.showArms());
                        buf.writeInt(msg.entityId());
                        buf.writeBoolean(msg.ownerOnly());
                    });
                }
            } else {
                Utils.logMsg("Entity ID " + msg.entityId() + " is not an armor stand", "debug");
            }
        });
    }

    public static void clientHandle(OpenArmorStandMenuPacket msg, IPayloadContext ctx) {}
}