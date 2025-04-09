package com.deoxservices.chipsarmorstand.compat.curios;

import com.deoxservices.chipsarmorstand.utils.Constants;
import com.deoxservices.chipsarmorstand.utils.Utils;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.decoration.ArmorStand;
//import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
//import top.theillusivec4.curios.api.CuriosApi;
//import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArmorStandCuriosHandler implements ICuriosItemHandler {
    private final Map<String, ICurioStacksHandler> curioMap = Collections.emptyMap();

    static {
        Utils.logMsg("ArmorStandCuriosHandler class loaded", "debug");
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        Utils.logMsg("EntityInteractSpecific event fired, target: " + event.getTarget().getType(), "debug");
        if (event.getTarget().getType() != EntityType.ARMOR_STAND) {
            Utils.logMsg("Not an armor stand, skipping", "debug");
            return;
        }

        //Player player = event.getEntity();
        //ArmorStand armorStand = (ArmorStand) event.getTarget();
        //InteractionHand hand = event.getHand();
        //ItemStack heldItem = player.getItemInHand(hand);

        if (event.getLevel().isClientSide()) {
            Utils.logMsg("Client-side interaction, skipping", "debug");
            return;
        }

        //ICuriosItemHandler curiosHandler = CuriosApi.getCuriosInventory(armorStand)
        //.orElseThrow(() -> new IllegalStateException("No Curios inventory for armor stand"));

        //Set<String> slotIds = curiosHandler.getCurios().keySet(); // Dynamic slot IDs
        
        /*if (!heldItem.isEmpty()) {
            Utils.logMsg("Trying to equip item: " + heldItem.getItem().getDescriptionId(), "debug");
            for (String slot : slotIds) {
                IDynamicStackHandler stackHandler = curiosHandler.getStacksHandler(slot)
                    .map(ICurioStacksHandler::getStacks)
                    .orElse(null);
                if (stackHandler != null) {
                    SlotContext slotContext = new SlotContext(slot, armorStand, 0, false, true);
                    if (CuriosApi.isStackValid(slotContext, heldItem)) {
                        for (int i = 0; i < stackHandler.getSlots(); i++) {
                            if (stackHandler.getStackInSlot(i).isEmpty()) {
                                stackHandler.setStackInSlot(i, heldItem.copy());
                                if (!player.isCreative()) player.setItemInHand(hand, ItemStack.EMPTY);
                                Utils.logMsg("Equipped " + heldItem.getItem().getDescriptionId() + " to " + slot + " on armor stand", "debug");
                                event.setCancellationResult(InteractionResult.SUCCESS);
                                event.setCanceled(true);
                                return;
                            }
                        }
                    }
                }
            }
        } else {
            Utils.logMsg("Trying to unequip item", "debug");
            for (String slot : slotIds) {
                IDynamicStackHandler stackHandler = curiosHandler.getStacksHandler(slot)
                    .map(ICurioStacksHandler::getStacks)
                    .orElse(null);
                if (stackHandler != null) {
                    for (int i = stackHandler.getSlots() - 1; i >= 0; i--) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            player.setItemInHand(hand, stack.copy());
                            stackHandler.setStackInSlot(i, ItemStack.EMPTY);
                            Utils.logMsg("Unequipped " + stack.getItem().getDescriptionId() + " from " + slot + " on armor stand", "debug");
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
            }
        }*/
    }

    @Override public Map<String, ICurioStacksHandler> getCurios() { return curioMap; }
    @Override public void setCurios(Map<String, ICurioStacksHandler> map) { }
    @Override public int getSlots() { return curioMap.values().stream().mapToInt(ICurioStacksHandler::getSlots).sum(); }
    @Override public void reset() { }
    @Override public Optional<ICurioStacksHandler> getStacksHandler(String identifier) { return Optional.ofNullable(curioMap.get(identifier)); }
    @Override public void growSlotType(String identifier, int amount) { }
    @Override public void shrinkSlotType(String identifier, int amount) { }
    @Override public Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter) { return Optional.empty(); }
    @Override public Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter, String identifier) { return Optional.empty(); }
    @Override public Optional<SlotResult> findFirstCurio(Item item) { return Optional.empty(); }
    @Override public Optional<SlotResult> findCurio(String identifier, int index) { return Optional.empty(); }
    @Override public void addTransientSlotModifiers(Multimap<String, AttributeModifier> modifiers) { }
    @Override public void clearSlotModifiers() { }
    @Override public int getLootingLevel(@Nullable LootContext context) { return 0; }
    @Override public void readTag(Tag tag) { }
    @Override public Tag writeTag() { return new ListTag(); }
    @Override public void setEquippedCurio(String identifier, int index, ItemStack stack) { }
    @Override public LivingEntity getWearer() { return null; }
    @Override public ListTag saveInventory(boolean clear) { return null; }
    @Override public Set<ICurioStacksHandler> getUpdatingInventories() { return null; }
    @Override public List<SlotResult> findCurios(String... identifiers) { return Collections.emptyList(); }
    @Override public List<SlotResult> findCurios(Predicate<ItemStack> filter) { return Collections.emptyList(); }
    @Override public List<SlotResult> findCurios(Item item) { return Collections.emptyList(); }
    @Override public void handleInvalidStacks() { }
    @Override public IDynamicStackHandler getEquippedCurios() { return null; }
    @Override public void clearCachedSlotModifiers() { }
    @Override public void addPermanentSlotModifiers(Multimap<String, AttributeModifier> modifiers) { }
    @Override public Multimap<String, AttributeModifier> getModifiers() { return null; }
    @Override public void loseInvalidStack(ItemStack stack) { }
    @Override public int getFortuneLevel(@Nullable LootContext context) { return 0; }
    @Override public void loadInventory(ListTag list) { }
    @Override public void removeSlotModifiers(Multimap<String, AttributeModifier> modifiers) { }
}