package com.deoxservices.chipsarmorstand.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.ModList;

public class Constants {
    public static final String MOD_ID     = "chipsarmorstand";
    public static final String MOD_PREFIX = MOD_ID + ":";
    public static final String MOD_NAME   = "Chips Armor Stand";
    public static final Logger LOGGER     = LogManager.getLogger(MOD_ID);
    public static final boolean RYOAMICLIGHTS_LOADED = ModList.get().isLoaded("ryoamiclights");
}
