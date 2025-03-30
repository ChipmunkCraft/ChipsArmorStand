package com.deoxservices.chipscurioslight.network;

import com.deoxservices.chipscurioslight.menu.ArmorStandMenu;
import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenArmorStandMenuPacket(int entityId) implements CustomPacketPayload {
    public static final Type<OpenArmorStandMenuPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "open_armor_stand_menu"));

    public OpenArmorStandMenuPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenArmorStandMenuPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Entity entity = player.level().getEntity(msg.entityId());
            if (entity instanceof ArmorStand armorStand) {
                Utils.logMsg("Server received packet, opening menu for armor stand ID: " + msg.entityId(), "debug");
                player.openMenu(new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return armorStand.getDisplayName();
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                        Utils.logMsg("Creating ArmorStandMenu with ID: " + id, "debug");
                        // Write entity ID to buffer for client sync
                        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                        buf.writeInt(armorStand.getId());
                        return new ArmorStandMenu(id, inv, buf);
                    }
                });
            } else {
                Utils.logMsg("Entity ID " + msg.entityId() + " is not an armor stand", "debug");
            }
        });
    }
}