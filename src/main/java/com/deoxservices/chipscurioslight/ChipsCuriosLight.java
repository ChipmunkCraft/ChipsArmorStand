/**
 * ChipsCuriosLight
 * 
 * @Created 03/20/2025 21:24
 * @author Chipmunk
 * @Copyright (c) 2025 DeoX Services
 */
package com.deoxservices.chipscurioslight;

import com.deoxservices.chipscurioslight.client.config.ClientConfig;
import com.deoxservices.chipscurioslight.compat.curios.CuriosCompat;
import com.deoxservices.chipscurioslight.compat.curios.DynamicRenderer;
import com.deoxservices.chipscurioslight.compat.ryoamiclights.RyoamicLightHandler;
import com.deoxservices.chipscurioslight.menu.ArmorStandMenu;
import com.deoxservices.chipscurioslight.network.NetworkHandler;
import com.deoxservices.chipscurioslight.utils.Constants;
import com.deoxservices.chipscurioslight.utils.Utils;

import java.util.ArrayList;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class ChipsCuriosLight {
    private static final ArrayList<String> ITEMS = new ArrayList<>();

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
    public static final Supplier<MenuType<ArmorStandMenu>> ARMOR_STAND_MENU = MENUS.register("armor_stand", () -> 
    new MenuType<ArmorStandMenu>((id, inv, buf) -> {
        int entityId = buf.readInt();
        Entity entity = inv.player.level().getEntity(entityId);
        if (entity instanceof ArmorStand armorStand) {
            return new ArmorStandMenu(id, inv, armorStand);
        }
        throw new IllegalStateException("No ArmorStand found for ID: " + entityId);
    }, FeatureFlags.DEFAULT_FLAGS));

    public ChipsCuriosLight(IEventBus modEventBus, ModContainer container) {
        MENUS.register(modEventBus);
        modEventBus.addListener(NetworkHandler::register);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        /*CuriosCompat curiosCompat = */new CuriosCompat();

        // Register DynamicLightHandler for players on client-side only
        if (Constants.RYOAMICLIGHTS_LOADED && FMLEnvironment.dist.isClient()) {
            RyoamicLightHandler.registerHandlers();
            Utils.logMsg("Registered RyoamicLights DynamicLightHandler for Curios slots.", "info");
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModContainer container = ModList.get().getModContainerById(Constants.MOD_ID).orElseThrow();
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        });

        ITEMS.add("minecraft:lantern");
        ITEMS.add("minecraft:soul_lantern");
        ITEMS.add("minecraft:jack_o_lantern");
        for (String name : ITEMS) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(name));
            CuriosRendererRegistry.register(item, () -> new DynamicRenderer(item));
        }
    }
}