package com.deoxservices.chipscurioslight.menu;

import java.util.stream.StreamSupport;

import com.deoxservices.chipscurioslight.ChipsCuriosLight;
import com.deoxservices.chipscurioslight.client.ClientProxyGameEvents;
import com.deoxservices.chipscurioslight.utils.Utils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ArmorStandMenu extends AbstractContainerMenu {

    private final ArmorStand armorStand;
    private final boolean showArms;
    private boolean showBase = true;  // Default on
    private boolean showStand = true; // Default on
    public int ticksSinceInteraction = 0;

    public ArmorStandMenu(int id, Inventory playerInv, ArmorStand armorStand, boolean showArms) {
        super(ChipsCuriosLight.ARMOR_STAND_MENU.get(), id);
        this.armorStand = armorStand;
        this.showArms = showArms;
        if (armorStand != null) {
            Utils.logMsg("Initialized ArmorStandMenu with ArmorStand ID: " + armorStand.getId() + ", showArms: " + showArms, "debug");
            this.showBase = armorStand.isNoBasePlate();
            this.showStand = !armorStand.isInvisible();
        } else {
            Utils.logMsg("Initialized ArmorStandMenu with null ArmorStand (client-side placeholder), showArms: " + showArms, "debug");
        }
        initSlots(playerInv);
    }

    public ArmorStandMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        super(ChipsCuriosLight.ARMOR_STAND_MENU.get(), id);
        this.showArms = extraData.readBoolean();
        int entityId = extraData.readInt();
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
        if (armorStand != null) {
            addSlot(new ArmorSlot(armorStand, EquipmentSlot.HEAD, 0, 8, 8));
            addSlot(new ArmorSlot(armorStand, EquipmentSlot.CHEST, 1, 8, 30));
            addSlot(new ArmorSlot(armorStand, EquipmentSlot.LEGS, 2, 8, 52));
            addSlot(new ArmorSlot(armorStand, EquipmentSlot.FEET, 3, 8, 74));
            if (showArms) {
                addSlot(new ArmorSlot(armorStand, EquipmentSlot.MAINHAND, 4, 8, 96));
                addSlot(new ArmorSlot(armorStand, EquipmentSlot.OFFHAND, 5, 8, 118));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

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

    @Override
    public boolean stillValid(Player player) {
        return armorStand != null && !armorStand.isRemoved() && player.distanceToSqr(armorStand) < 64.0;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (armorStand != null && !player.level().isClientSide()) {
            ClientProxyGameEvents.unlockArmorStand(armorStand.getId());
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ticksSinceInteraction = 0;
        return super.clickMenuButton(player, id);
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

    // Custom slot for Armor Stand equipment
    private static class ArmorSlot extends Slot {
        private final EquipmentSlot slotType;

        public ArmorSlot(ArmorStand armorStand, EquipmentSlot slotType, int index, int x, int y) {
            super(new ArmorStandContainer(armorStand), index, x, y);
            this.slotType = slotType;
        }

        @SuppressWarnings("null")
        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.canEquip(slotType, null);
        }

        @Override
        public ItemStack getItem() { return ((ArmorStandContainer) container).getItem(slotType); }

        @Override
        public void set(ItemStack stack) {
            ((ArmorStandContainer) container).setItem(slotType, stack);
            setChanged();
        }
    }

    // Wrapper for Armor Stand inventory
    private static class ArmorStandContainer extends net.minecraft.world.ContainerHelper implements net.minecraft.world.Container {
        private final ArmorStand armorStand;

        public ArmorStandContainer(ArmorStand armorStand) { this.armorStand = armorStand; }

        @Override
        public int getContainerSize() {
            return 6; // Helmet, Chest, Legs, Feet, Main, Off
        }

        @Override
        public boolean isEmpty() {
            return StreamSupport.stream(armorStand.getAllSlots().spliterator(), false)
                .allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> armorStand.getItemBySlot(EquipmentSlot.HEAD);
                case 1 -> armorStand.getItemBySlot(EquipmentSlot.CHEST);
                case 2 -> armorStand.getItemBySlot(EquipmentSlot.LEGS);
                case 3 -> armorStand.getItemBySlot(EquipmentSlot.FEET);
                case 4 -> armorStand.getItemBySlot(EquipmentSlot.MAINHAND);
                case 5 -> armorStand.getItemBySlot(EquipmentSlot.OFFHAND);
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack stack = getItem(index);
            if (!stack.isEmpty() && count > 0) {
                ItemStack split = stack.split(count);
                setItem(index, stack);
                return split;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = getItem(index);
            if (!stack.isEmpty()) {
                setItem(index, ItemStack.EMPTY);
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            switch (index) {
                case 0 -> armorStand.setItemSlot(EquipmentSlot.HEAD, stack);
                case 1 -> armorStand.setItemSlot(EquipmentSlot.CHEST, stack);
                case 2 -> armorStand.setItemSlot(EquipmentSlot.LEGS, stack);
                case 3 -> armorStand.setItemSlot(EquipmentSlot.FEET, stack);
                case 4 -> armorStand.setItemSlot(EquipmentSlot.MAINHAND, stack);
                case 5 -> armorStand.setItemSlot(EquipmentSlot.OFFHAND, stack);
            }
            setChanged();
        }

        @Override
        public void setChanged() { ((Container) armorStand).setChanged(); }

        public ItemStack getItem(EquipmentSlot slot) { return armorStand.getItemBySlot(slot); }

        public void setItem(EquipmentSlot slot, ItemStack stack) {
            armorStand.setItemSlot(slot, stack);
            setChanged();
        }

        @Override
        public int getMaxStackSize() { return 1; }

        @Override
        public boolean stillValid(Player player) { return !armorStand.isRemoved(); }

        @Override
        public void clearContent() { armorStand.getAllSlots().forEach(stack -> stack.setCount(0)); }
    }
}