/**
 * ChipsArmorStand
 * 
 * @Created 03/20/2025 21:24
 * @author Chipmunk
 * @Copyright (c) 2025 DeoX Services
 */
package com.deoxservices.chipsarmorstand;

import com.deoxservices.chipsarmorstand.client.config.ClientConfig;
import com.deoxservices.chipsarmorstand.compat.curios.CuriosCompat;
import com.deoxservices.chipsarmorstand.compat.curios.DynamicRenderer;
import com.deoxservices.chipsarmorstand.compat.ryoamiclights.RyoamicLightHandler;
import com.deoxservices.chipsarmorstand.data.ArmorStandData;
import com.deoxservices.chipsarmorstand.menu.ArmorStandMenu;
import com.deoxservices.chipsarmorstand.network.NetworkHandler;
import com.deoxservices.chipsarmorstand.server.config.ServerConfig;
import com.deoxservices.chipsarmorstand.utils.Constants;
import com.deoxservices.chipsarmorstand.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class ChipsArmorStand {
    private static final ArrayList<String> ITEMS = new ArrayList<>();

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Constants.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<ArmorStandMenu>> ARMOR_STAND_MENU = MENUS.register("armor_stand_menu", () -> IMenuTypeExtension.create((id, inv, buf) -> new ArmorStandMenu(id, inv, null, buf)));

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ArmorStandData>> ARMOR_STAND_DATA = ATTACHMENTS.register("armor_stand_data", 
        () -> AttachmentType.builder(() -> new ArmorStandData(null, new HashMap<>(Map.of())))
            .serialize(ArmorStandData.CODEC)
            .build());

    public ChipsArmorStand(IEventBus modEventBus, ModContainer container) {
        MENUS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        modEventBus.addListener(NetworkHandler::register);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.CONFIG_SPEC);
        new CuriosCompat();
        // Register DynamicLightHandler for players on client-side only
        if (Constants.RYOAMICLIGHTS_LOADED && FMLEnvironment.dist.isClient()) {
            RyoamicLightHandler.registerHandlers();
            Utils.logMsg("Registered RyoamicLights DynamicLightHandler for Curios slots.", "info");
        }
        Utils.logMsg("Initialized mod with ArmorStandData attachment", "debug");
    }
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        //List<?> itemsList = ClientConfig.CONFIG.CURIOS_ITEMS.get();
        ITEMS.add("minecraft:lantern");
        ITEMS.add("minecraft:soul_lantern");
        ITEMS.add("minecraft:jack_o_lantern");
        for (String name : ITEMS) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(name));
            CuriosRendererRegistry.register(item, () -> new DynamicRenderer(item));
        }
    }
}