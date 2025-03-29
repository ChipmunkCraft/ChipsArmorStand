package com.deoxservices.chipscurioslight.menu;

import com.deoxservices.chipscurioslight.ChipsCuriosLight;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
//import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
//import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
//import top.theillusivec4.curios.api.CuriosApi;
//import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class ArmorStandMenu extends AbstractContainerMenu {
    private final ArmorStand armorStand;

    public ArmorStandMenu(int id, Inventory playerInv, ArmorStand armorStand) {
        super(ChipsCuriosLight.ARMOR_STAND_MENU.get(), id);
        this.armorStand = armorStand;
    }

    public ArmorStandMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, getArmorStandFromBuf(playerInv.player, buf));
    }

    private static ArmorStand getArmorStandFromBuf(Player player, FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof ArmorStand armorStand) {
            return armorStand;
        }
        throw new IllegalStateException("No ArmorStand found for ID: " + entityId);
    }

    @Override
    public boolean stillValid(Player player) {
        return !armorStand.isRemoved() && armorStand.distanceToSqr(player) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }
}

// Wrapper for ArmorStand vanilla slots
class ArmorStandContainer implements Container {
    private final ArmorStand armorStand;
    private final NonNullList<ItemStack> items;

    public ArmorStandContainer(ArmorStand armorStand) {
        this.armorStand = armorStand;
        this.items = NonNullList.withSize(4, ItemStack.EMPTY);
        syncItems();
    }

    private void syncItems() {
        items.set(0, armorStand.getItemBySlot(EquipmentSlot.HEAD));
        items.set(1, armorStand.getItemBySlot(EquipmentSlot.CHEST));
        items.set(2, armorStand.getItemBySlot(EquipmentSlot.LEGS));
        items.set(3, armorStand.getItemBySlot(EquipmentSlot.FEET));
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = items.get(slot);
        if (!stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
            armorStand.setItemSlot(EquipmentSlot.values()[slot], ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        armorStand.setItemSlot(EquipmentSlot.values()[slot], stack);
    }

    @Override
    public void setChanged() {
        syncItems();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}

// Custom slot for Curios
class CurioSlot extends Slot {
    private final IDynamicStackHandler stackHandler;

    public CurioSlot(IDynamicStackHandler stackHandler, int index, int x, int y) {
        super(new DummyContainer(), index, x, y); // Dummy container to satisfy Slot
        this.stackHandler = stackHandler;
    }

    @Override
    public ItemStack getItem() {
        return stackHandler.getStackInSlot(getSlotIndex());
    }

    @Override
    public void set(ItemStack stack) {
        stackHandler.setStackInSlot(getSlotIndex(), stack);
        setChanged();
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = stackHandler.getStackInSlot(getSlotIndex());
        if (!stack.isEmpty()) {
            stackHandler.setStackInSlot(getSlotIndex(), ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }
}

// Dummy container to satisfy Slot constructor
class DummyContainer implements Container {
    @Override public int getContainerSize() { return 0; }
    @Override public boolean isEmpty() { return true; }
    @Override public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
    @Override public void setItem(int slot, ItemStack stack) {}
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() {}
}