package com.deoxservices.chipsarmorstandmenu.menu;

import javax.annotation.Nullable;

import com.deoxservices.chipsarmorstandmenu.ChipsArmorStandMenu;
import com.deoxservices.chipsarmorstandmenu.client.ClientProxyGameEvents;
import com.deoxservices.chipsarmorstandmenu.utils.Utils;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
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

    private ArmorStand armorStand;
    private final boolean showArms;
    private boolean showBase = true;  // Default on
    private boolean showStand = true; // Default on
    public int ticksSinceInteraction = 0;
    private final int entityId; // Store for later lookup
    private final Player player; // Add player field
    private static final ResourceLocation EMPTY_HELMET = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_helmet");
    private static final ResourceLocation EMPTY_CHESTPLATE = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_chestplate");
    private static final ResourceLocation EMPTY_LEGGINGS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_leggings");
    private static final ResourceLocation EMPTY_BOOTS = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_boots");
    private static final ResourceLocation EMPTY_SHIELD = ResourceLocation.withDefaultNamespace("item/empty_armor_slot_shield");
    private static final ResourceLocation EMPTY_SWORD = ResourceLocation.withDefaultNamespace("item/empty_slot_sword");

    public ArmorStandMenu(int id, Inventory playerInv, ArmorStand armorStand, boolean showArms) {
        super(ChipsArmorStandMenu.ARMOR_STAND_MENU.get(), id);
        this.armorStand = armorStand;
        this.showArms = showArms;
        this.entityId = armorStand != null ? armorStand.getId() : -1;
        this.player = playerInv.player;
        if (armorStand != null) {
            Utils.logMsg("Initialized ArmorStandMenu with ArmorStand ID: " + armorStand.getId() + ", showArms: " + showArms, "debug");
            this.showBase = armorStand.isNoBasePlate();
            this.showStand = !armorStand.isInvisible();
        }
        initSlots(playerInv);
    }

    // Client constructor with entity lookup
    public ArmorStandMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ChipsArmorStandMenu.ARMOR_STAND_MENU.get(), id);
        this.showArms = extraData.readBoolean();
        this.entityId = extraData.readInt();
        this.player = playerInv.player;
        this.armorStand = (ArmorStand) playerInv.player.level().getEntity(entityId);
        if (armorStand != null) {
            Utils.logMsg("Client initialized ArmorStandMenu with ArmorStand ID: " + armorStand.getId() + ", showArms: " + showArms, "debug");
            this.showBase = armorStand.isNoBasePlate();
            this.showStand = !armorStand.isInvisible();
        } else {
            Utils.logMsg("Client initialized ArmorStandMenu with null ArmorStand (pending sync), showArms: " + showArms, "debug");
        }
        initSlots(playerInv);
    }

    private void initSlots(Inventory playerInv) {
        // Always add slots—client/server must match

        // Armor Stand Armor Slots
        this.addSlot(new SlotArmorStand(this, armorStand, playerInv.player, EquipmentSlot.HEAD, 0, 231, 14, EMPTY_HELMET));
        this.addSlot(new SlotArmorStand(this, armorStand, playerInv.player, EquipmentSlot.CHEST, 1, 231, 32, EMPTY_CHESTPLATE));
        this.addSlot(new SlotArmorStand(this, armorStand, playerInv.player, EquipmentSlot.LEGS, 2, 231, 50, EMPTY_LEGGINGS));
        this.addSlot(new SlotArmorStand(this, armorStand, playerInv.player, EquipmentSlot.FEET, 3, 231, 68, EMPTY_BOOTS));
        if (showArms) {
            this.addSlot(new SlotArmorStand(this, armorStand, playerInv.player, EquipmentSlot.MAINHAND, 4, 180, 90, EMPTY_SWORD)); // Armor Stand Right Hand
            this.addSlot(new SlotArmorStand(this, armorStand, playerInv.player, EquipmentSlot.OFFHAND, 5, 213, 90, EMPTY_SHIELD)); // Armor Stand Left Hand
        }

        // Player inventory (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + (row + 1) * 9, 8 + col * 18, 90 + row * 18));
            }
        }

        // Hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 148));
        }

        // Player Armor Slots
        this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.HEAD, 39, 8, 14, EMPTY_HELMET)); // Player Helmet
        this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.CHEST, 38, 8, 32, EMPTY_CHESTPLATE)); // Player Chestplate
        this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.LEGS, 37, 8, 50, EMPTY_LEGGINGS)); // Player Leggings
        this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.FEET, 36, 8, 68, EMPTY_BOOTS)); // Player Boots
        this.addSlot(new ArmorSlot(playerInv, playerInv.player, EquipmentSlot.OFFHAND, 40, 77, 68, EMPTY_SHIELD)); // Player offhand
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
        if (armorStand != null && !player.level().isClientSide()) {
            ClientProxyGameEvents.unlockArmorStand(armorStand.getId());
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
        if (armorStand == null && !player.level().isClientSide) {
            Player player = this.player;
            if (player.level().getEntity(entityId) instanceof ArmorStand stand) {
                this.armorStand = stand;
                Utils.logMsg("Updated ArmorStandMenu with ArmorStand ID: " + armorStand.getId(), "debug");
            }
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

    public boolean getShowArms() { return showArms; }

    public void setShowArms(boolean value) { if (armorStand != null) armorStand.setShowArms(value); }

    public boolean getShowBase() { return showBase; }

    public void setShowBase(boolean value) {
        this.showBase = value;
        if (armorStand != null) armorStand.setNoBasePlate(!value); // Inverted
    }

    public boolean getShowStand() { return showStand; }

    public void setShowStand(boolean value) {
        this.showStand = value;
        if (armorStand != null) armorStand.setInvisible(!value);
    }

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
        @SuppressWarnings("unused") // Suppress unused warning for now
        private final Player player; // Keep for future use (e.g., permission checks)
        private final EquipmentSlot slotType;
        @Nullable
        private final ResourceLocation emptyIcon;

        public SlotArmorStand(ArmorStandMenu menu, ArmorStand armorStand, Player player, EquipmentSlot slotType, int slotIndex, int x, int y, @Nullable ResourceLocation emptyIcon) {
            super(new ArmorStandContainer(armorStand, slotType), slotIndex, x, y);
            this.menu = menu;
            this.player = player;
            this.slotType = slotType;
            this.emptyIcon = emptyIcon;
        }

        @SuppressWarnings("null")
        @Override
        public boolean mayPlace(ItemStack stack) {
            return armorStand != null && menu.isItemValidForSlot(this.slotType, stack);
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
            return this.emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon) : super.getNoItemIcon();
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