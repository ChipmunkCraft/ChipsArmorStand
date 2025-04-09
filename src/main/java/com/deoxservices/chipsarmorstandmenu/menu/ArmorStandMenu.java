package com.deoxservices.chipsarmorstandmenu.menu;

import java.util.UUID;

import javax.annotation.Nullable;

import com.deoxservices.chipsarmorstandmenu.ChipsArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.data.ArmorStandData;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ArmorStandMenu extends AbstractContainerMenu {

    private final Player player;
    private int entityId = -1;
    private ArmorStand armorStand;
    private UUID owner = null;
    private boolean inUse = false;
    private boolean locked = false;
    private boolean invisible = false;
    private boolean showArms = false;
    private boolean noBasePlate = false;
    private boolean isSmall = false;
    public int ticksSinceInteraction = 0;
    private static final ResourceLocation EMPTY_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SHIELD = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_shield");
    private static final ResourceLocation EMPTY_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");

    public ArmorStandMenu(int id, Inventory playerInv, ArmorStand armorStand, RegistryFriendlyByteBuf clientData) {
        super(ChipsArmorStandMenu.ARMOR_STAND_MENU.get(), id);
        this.player = playerInv.player;
        if (!player.level().isClientSide() && clientData==null) {  // Server-side: Use ArmorStand directly
            this.entityId = armorStand.getId();
            this.armorStand = armorStand;
            this.owner = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get()).getOwner();
            this.inUse = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get()).isInUse();
            this.locked = armorStand.getData(ChipsArmorStandMenu.ARMOR_STAND_DATA.get()).isLocked();
            this.invisible = armorStand.isInvisible();
            this.showArms = armorStand.isShowArms();
            this.noBasePlate = armorStand.isNoBasePlate();
            this.isSmall = armorStand.isSmall();
            Utils.logMsg("Server Initialized ArmorStandMenu ID: "+ id +" |  ArmorStandId: " + this.entityId + ", Owner:  " + this.owner + ", InUse: " + this.inUse + ", Locked:  " + this.locked + ", Invisible:  " + this.invisible + ", ShowArms: " + this.showArms + ", NoBasePlate: " + this.noBasePlate + "Small: " + this.isSmall, "debug");
            initSlots(playerInv, "server");
        } else if (player.level().isClientSide() && clientData!=null) {  // Client-side: Read from packet
            this.entityId = clientData.readInt();
            this.armorStand = null; // Lazy load in initSlots or broadcastChanges
            this.owner = clientData.readUUID();
            this.inUse = clientData.readBoolean();
            this.locked = clientData.readBoolean();
            this.invisible = clientData.readBoolean();
            this.showArms = clientData.readBoolean();
            this.noBasePlate = clientData.readBoolean();
            this.isSmall = clientData.readBoolean();
            if (player.level().getEntity(entityId) instanceof ArmorStand as)
            {
                this.armorStand = as;
            }
            Utils.logMsg("Client Initialized ArmorStandMenu ID: "+ id +" |  ArmorStandId: " + entityId + ", Owner:  " + this.owner + ", InUse: " + this.inUse + ", Locked:  " + this.locked + ", Invisible:  " + this.invisible + ", ShowArms: " + this.showArms + ", NoBasePlate: " + this.noBasePlate + "Small: " + this.isSmall, "debug");
            initSlots(playerInv, "client");
        } else {
            Utils.logMsg("Could not determine client or server side Armor Stand Menu. This is a bug. Report to mod developer.", "warn");
        }
    }
    private void initSlots(Inventory playerInv, String side) {
        Utils.logMsg((side=="server" ? "Server" : "Client") + " attempting to add slots for armorStand: " + (armorStand != null ? armorStand.getId() : "null"), "debug");
        if (armorStand!=null)
        {
            // Armor Stand Armor Slots
            this.addSlot(new SlotArmorStand(this, armorStand, EquipmentSlot.HEAD, 0, 231, 15, EMPTY_HELMET));
            this.addSlot(new SlotArmorStand(this, armorStand, EquipmentSlot.CHEST, 1, 231, 33, EMPTY_CHESTPLATE));
            this.addSlot(new SlotArmorStand(this, armorStand, EquipmentSlot.LEGS, 2, 231, 51, EMPTY_LEGGINGS));
            this.addSlot(new SlotArmorStand(this, armorStand, EquipmentSlot.FEET, 3, 231, 69, EMPTY_BOOTS));
            if (showArms) {
                this.addSlot(new SlotArmorStand(this, armorStand, EquipmentSlot.MAINHAND, 4, 180, 91, EMPTY_SWORD)); // Armor Stand Right Hand
                this.addSlot(new SlotArmorStand(this, armorStand, EquipmentSlot.OFFHAND, 5, 213, 91, EMPTY_SHIELD)); // Armor Stand Left Hand
            }
            // Player inventory (27 slots)
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.addSlot(new Slot(playerInv, col + (row + 1) * 9, 8 + col * 18, 91 + row * 18));
                }
            }
            // Hotbar (9 slots)
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col, 8 + col * 18, 155));
            }
            // Player Armor Slots
            this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.HEAD, 39, 8, 15, EMPTY_HELMET)); // Player Helmet
            this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.CHEST, 38, 8, 33, EMPTY_CHESTPLATE)); // Player Chestplate
            this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.LEGS, 37, 8, 51, EMPTY_LEGGINGS)); // Player Leggings
            this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.FEET, 36, 8, 69, EMPTY_BOOTS)); // Player Boots
            this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.OFFHAND, 40, 77, 69, EMPTY_SHIELD)); // Player offhand
            Utils.logMsg((side=="server" ? "Server" : "Client") + " added slots for armorStand: " + (armorStand != null ? armorStand.getId() : "null"), "debug");
        }
    }

    @SuppressWarnings("null")
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ticksSinceInteraction = 0; // Reset on slot interaction
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ItemStack original = stack.copy();
            int armorSlots = showArms ? 6 : 4;
            if (index < armorSlots) {
                // Move from Armor Stand to player inventory
                if (!moveItemStackTo(stack, armorSlots, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player inventory to Armor Stand
                for (int i = 0; i < armorSlots; i++) {
                    Slot armorSlot = slots.get(i);
                    if (armorSlot.mayPlace(stack)) {
                        if (moveItemStackTo(stack, i, i + 1, false)) {
                            break;
                        }
                    }
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
            return original;
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("null")
    @Override
    public boolean stillValid(Player player) {
        return armorStand != null && !armorStand.isRemoved() && player.distanceToSqr(armorStand) < 64.0;
    }

    @SuppressWarnings("null")
    @Override
    public void removed(Player player) {
        super.removed(player);
        Minecraft mc = Minecraft.getInstance();
        if (armorStand != null && !player.level().isClientSide()) {
            ArmorStandData.setArmorStandInUse(armorStand, mc.player.getUUID(), false);
        }
    }

    @SuppressWarnings("null")
    @Override
    public boolean clickMenuButton(Player player, int id) {
        ticksSinceInteraction = 0;
        return super.clickMenuButton(player, id);
    }

    // Update armorStand when entity syncs
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (armorStand == null && entityId != -1 && player.level().getEntity(entityId) instanceof ArmorStand armor_stand) {
            this.armorStand = armor_stand;
            Utils.logMsg("Broadcast synced armorStand - ID: " + entityId, "debug");
        }
    }

    // Check if item is valid for slot
    public boolean isItemValidForSlot(EquipmentSlot slotType, ItemStack stack) {
        if (slotType == null) { // Hand slots
            return true; // Allow any item in arm slots
        }
        return stack.canEquip(slotType, player); // Use vanilla equip check
    }

    public ArmorStand getArmorStand() { return armorStand; }

    // Nested slot class
    public class ArmorSlot extends Slot {
        private final LivingEntity owner;
        private final EquipmentSlot slot;
        @Nullable
        private final ResourceLocation emptyIcon;

        public ArmorSlot(Container container, LivingEntity owner, EquipmentSlot slot, int slotIndex, int x, int y, @Nullable ResourceLocation emptyIcon) {
            super(container, slotIndex, x, y);
            this.owner = owner;
            this.slot = slot;
            this.emptyIcon = emptyIcon;
        }

        @SuppressWarnings("null")
        @Override
        public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
            this.owner.onEquipItem(this.slot, oldStack, newStack);
            super.setByPlayer(newStack, oldStack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        /**
         * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
         */
        @SuppressWarnings("null")
        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.canEquip(slot, owner);
        }

        /**
         * Return whether this slot's stack can be taken from this slot.
         */
        @SuppressWarnings("null")
        @Override
        public boolean mayPickup(Player player) {
            ItemStack itemstack = this.getItem();
            return !itemstack.isEmpty() && !player.isCreative() && EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
                ? false
                : super.mayPickup(player);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return this.emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon) : super.getNoItemIcon();
        }
    }

    // Nested slot class
    public class SlotArmorStand extends Slot {
        private final ArmorStandMenu menu;
        private final ArmorStand armorStand;
        private final EquipmentSlot slotType;
        @Nullable
        private final ResourceLocation emptyIcon;

        public SlotArmorStand(ArmorStandMenu menu, ArmorStand armorStand, EquipmentSlot slotType, int slotIndex, int x, int y, @Nullable ResourceLocation emptyIcon) {
            super(new ArmorStandContainer(armorStand, slotType), slotIndex, x, y);
            this.menu = menu;
            this.armorStand = armorStand;
            this.slotType = slotType;
            this.emptyIcon = emptyIcon;
        }

        @SuppressWarnings("null")
        @Override
        public boolean mayPlace(ItemStack stack) {
            return armorStand != null && menu.isItemValidForSlot(slotType, stack);
        }

        @Override
        public ItemStack getItem() {
            return armorStand != null ? armorStand.getItemBySlot(slotType) : ItemStack.EMPTY;
        }

        @SuppressWarnings("null")
        @Override
        public void set(ItemStack stack) {
            if (armorStand != null) {
                armorStand.setItemSlot(slotType, stack);
                setChanged();
            }
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, emptyIcon) : super.getNoItemIcon();
        }
    }
    // Nested container class
    private static class ArmorStandContainer extends net.minecraft.world.ContainerHelper implements net.minecraft.world.Container {
        private final ArmorStand armorStand;
        private final EquipmentSlot slotType;

        public ArmorStandContainer(ArmorStand armorStand, EquipmentSlot slotType) {
            this.armorStand = armorStand;
            this.slotType = slotType;
        }

        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return armorStand == null || armorStand.getItemBySlot(slotType).isEmpty();
        }

        @Override
        public ItemStack getItem(int index) {
            return armorStand != null ? armorStand.getItemBySlot(slotType) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            if (armorStand != null) {
                ItemStack stack = armorStand.getItemBySlot(slotType);
                if (!stack.isEmpty()) {
                    if (stack.getCount() <= count) {
                        armorStand.setItemSlot(slotType, ItemStack.EMPTY);
                        return stack;
                    } else {
                        ItemStack split = stack.split(count);
                        armorStand.setItemSlot(slotType, stack);
                        return split;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            if (armorStand != null) {
                ItemStack stack = armorStand.getItemBySlot(slotType);
                armorStand.setItemSlot(slotType, ItemStack.EMPTY);
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @SuppressWarnings("null")
        @Override
        public void setItem(int index, ItemStack stack) {
            if (armorStand != null) {
                armorStand.setItemSlot(slotType, stack);
            }
        }

        @Override
        public void setChanged() {}

        @SuppressWarnings("null")
        @Override
        public boolean stillValid(Player player) { return true; }

        @Override
        public void clearContent() {
            if (armorStand != null) {
                armorStand.setItemSlot(slotType, ItemStack.EMPTY);
            }
        }
    }
}