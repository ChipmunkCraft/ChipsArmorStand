package com.deoxservices.chipsarmorstandmenu.client.screen;

import com.deoxservices.chipsarmorstandmenu.menu.ArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.network.ToggleArmorStandPacket;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ArmorStandScreen extends AbstractContainerScreen<ArmorStandMenu> {
    //private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("chipsarmorstandmenu", "textures/gui/armor_stand_menu.png");
    private Checkbox showArmsCheckbox;
    private Checkbox showBaseCheckbox;
    private Checkbox showStandCheckbox;

    public ArmorStandScreen(ArmorStandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @SuppressWarnings("null")
    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

    showArmsCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.show_arms"), minecraft.font)
        .pos(x + 100, y + 20)
        .maxWidth(60)
        .selected(menu.getShowArms())
        .onValueChange((checkbox, value) -> {
            if (menu.getArmorStand() != null) {
                PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "arms", value));
            }}).build();

    showBaseCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.show_base"), minecraft.font)
        .pos(x + 100, y + 50)
        .maxWidth(60)
        .selected(menu.getShowBase())
        .onValueChange((checkbox, value) -> {
            if (menu.getArmorStand() != null) {
                PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "base", value));
            }}).build();

    showStandCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.show_stand"), minecraft.font)
        .pos(x + 100, y + 80)
        .maxWidth(60)
        .selected(menu.getShowStand())
        .onValueChange((checkbox, value) -> {
            if (menu.getArmorStand() != null) {
                PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "stand", value));
            }}).build();

    addRenderableWidget(showArmsCheckbox);
    addRenderableWidget(showBaseCheckbox);
    addRenderableWidget(showStandCheckbox);
}

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(InventoryScreen.INVENTORY_LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        /*if (menu.getArmorStand() != null) {
            showArmsCheckbox.selected(menu.getShowArms());
            showBaseCheckbox.setValue(menu.getShowBase());
            showStandCheckbox.setValue(menu.getShowStand());
        }*/
    }
}