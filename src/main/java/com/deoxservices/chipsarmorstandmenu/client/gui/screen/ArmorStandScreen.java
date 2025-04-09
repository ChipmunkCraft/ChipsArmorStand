package com.deoxservices.chipsarmorstandmenu.client.gui.screen;

import com.deoxservices.chipsarmorstandmenu.ChipsArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.menu.ArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.network.ToggleArmorStandPacket;
import com.deoxservices.chipsarmorstandmenu.utils.Constants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class ArmorStandScreen extends AbstractContainerScreen<ArmorStandMenu> {
    Minecraft minecraft = Minecraft.getInstance();
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/armor_stand_menu.png");

    private final ArmorStand armorStand; // Add this
    private final Player player; // Already available via menu
    private float xMouse;
    private float yMouse;
    private Checkbox lockedCheckbox;
    private Checkbox invisibleCheckbox;
    private Checkbox showArmsCheckbox;
    private Checkbox noBasePlateCheckbox;
    private Checkbox smallCheckBox;
    protected int playerLabelX;
    protected int playerLabelY;
    protected int armorLeftLabelX;
    protected int armorLeftLabelY;
    protected final Component playerInventoryTitle;

    public ArmorStandScreen(ArmorStandMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.armorStand = menu.getArmorStand();
        this.player = playerInv.player;
        this.playerInventoryTitle = player.getDisplayName();
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.playerLabelX = 8;
        this.playerLabelY = 4;
        this.armorLeftLabelX = 180;
        this.armorLeftLabelY = 4;

        lockedCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.locked"), minecraft.font)
            .pos(this.leftPos + 172, this.topPos + 110)
            .maxWidth(60)
            .selected(armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get()).isLocked())
            .onValueChange((checkbox, value) -> {
                if (menu.getArmorStand() != null) {
                    PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "Locked", value));
                }}).build();

        invisibleCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.invisible"), minecraft.font)
            .pos(this.leftPos + 172, this.topPos + 128)
            .maxWidth(60)
            .selected(armorStand.isInvisible())
            .onValueChange((checkbox, value) -> {
                if (menu.getArmorStand() != null) {
                    PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "Invisible", value));
                }}).build();

        showArmsCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.show_arms"), minecraft.font)
            .pos(this.leftPos + 172, this.topPos + 146)
            .maxWidth(60)
            .selected(armorStand.isShowArms())
            .onValueChange((checkbox, value) -> {
                if (menu.getArmorStand() != null) {
                    PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "ShowArms", value));
                }}).build();

        noBasePlateCheckbox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.no_base"), minecraft.font)
            .pos(this.leftPos + 172, this.topPos + 164)
            .maxWidth(60)
            .selected(armorStand.isNoBasePlate())
            .onValueChange((checkbox, value) -> {
                if (menu.getArmorStand() != null) {
                    PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "NoBasePlate", value));
                }}).build();

                smallCheckBox = Checkbox.builder(Component.translatable("gui.chipsarmorstandmenu.is_small"), minecraft.font)
            .pos(this.leftPos + 172, this.topPos + 182)
            .maxWidth(60)
            .selected(armorStand.isSmall())
            .onValueChange((checkbox, value) -> {
                if (menu.getArmorStand() != null) {
                    PacketDistributor.sendToServer(new ToggleArmorStandPacket(menu.getArmorStand().getId(), "Small", value));
                }}).build();

        addRenderableWidget(lockedCheckbox);
        addRenderableWidget(invisibleCheckbox);
        addRenderableWidget(showArmsCheckbox);
        addRenderableWidget(noBasePlateCheckbox);
        addRenderableWidget(smallCheckBox);
    }

    @SuppressWarnings("null")
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    @SuppressWarnings("null")
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, imageHeight);
        // Render player mini and armor stand
        if (this.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + 26, this.topPos + 15, this.leftPos + 75, this.topPos + 85, 30, 0.0625F, this.xMouse, this.yMouse, this.player);
        }
        if (this.armorStand != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + 180, this.topPos + 15, this.leftPos + 229, this.topPos + 85, 30, 0.0625F, this.xMouse, this.yMouse, this.armorStand);
        }
    }

    @SuppressWarnings("null")
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, minecraft.player.getDisplayName(), this.playerLabelX, this.playerLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.title, this.armorLeftLabelX, this.armorLeftLabelY, 4210752, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.chipsarmorstandmenu.player_hot_bar"), 8, 145, 4210752, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.chipsarmorstandmenu.stand_hot_bar"), 8, 173, 4210752, false);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }
}