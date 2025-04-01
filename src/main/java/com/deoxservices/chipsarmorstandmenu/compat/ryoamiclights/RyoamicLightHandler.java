package com.deoxservices.chipsarmorstandmenu.compat.ryoamiclights;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.thinkingstudio.ryoamiclights.api.DynamicLightHandler;
import org.thinkingstudio.ryoamiclights.api.DynamicLightHandlers;
import org.thinkingstudio.ryoamiclights.api.item.ItemLightSources;

//import com.deoxservices.chipsarmorstandmenu.utils.Utils;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

public class RyoamicLightHandler {

    public static void registerHandlers() {
        DynamicLightHandlers.registerDynamicLightHandler(EntityType.PLAYER, createHandler());
        DynamicLightHandlers.registerDynamicLightHandler(EntityType.ARMOR_STAND, createHandler());
    }

    // Core handler logic for any LivingEntity
    private static final DynamicLightHandler<LivingEntity> BASE_HANDLER = entity -> {
        //Utils.logMsg("[ "+entity.getType().getDescriptionId()+" ] Checking entity: " + entity.getType().getDescriptionId(), "debug");
        int totalLuminance = 0;
        var curiosInventory = CuriosApi.getCuriosInventory(entity);
        if (curiosInventory.isPresent()) {
            ICuriosItemHandler handler = curiosInventory.get();
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            //Utils.logMsg("[ "+entity.getType().getDescriptionId()+" ] Entity " + entity.getType().getDescriptionId() + " has " + curios.size() + " Curios slots", "debug");
            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                ICurioStacksHandler stacksHandler = entry.getValue();
                //Utils.logMsg("[ "+entity.getType().getDescriptionId()+" ] Slot " + entry.getKey() + " has " + stacksHandler.getSlots() + " slots", "debug");
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    //Utils.logMsg("[ "+entity.getType().getDescriptionId()+" ] Curios itemStack in slot " + entry.getKey() + "[" + i + "] | Stack empty? " + stack.isEmpty(), "debug");
                    if (!stack.isEmpty()) {
                        int luminance = ItemLightSources.getLuminance(stack, entity.isUnderWater());
                        if (luminance > 0) {
                            totalLuminance = Math.max(totalLuminance, luminance);
                            //Utils.logMsg("[ "+entity.getType().getDescriptionId()+" ] Emitting light: " + stack.getItem().getDescriptionId() + ", luminance: " + luminance, "debug");
                        }
                    }
                }
            }
        }
        return totalLuminance;
    };

    // Type-safe adapter for specific entity types
    @SuppressWarnings("unchecked")
    public static <T extends Entity> DynamicLightHandler<T> createHandler() {
        return (DynamicLightHandler<T>) DynamicLightHandler.makeLivingEntityHandler(BASE_HANDLER);
    }
}