package com.deoxservices.chipscurioslight.gui;

import com.deoxservices.chipscurioslight.client.ClientProxy;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ConfigScreen {
    protected Runnable canceller;

    public ConfigScreen(Screen parent) {
        ImmutableMap.Builder<KeyMapping, InputConstants.Key> keyMapBuilder = ImmutableMap.builder();
        keyMapBuilder.put(ClientProxy.OPEN_ARMOR_STAND_MENU, ClientProxy.OPEN_ARMOR_STAND_MENU.getKey());

        var keyMap = keyMapBuilder.build();
        canceller = () -> {
            keyMap.forEach(KeyMapping::setKey);
            Minecraft.getInstance().options.save();
        };
    }
}