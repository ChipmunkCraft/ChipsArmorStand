package com.deoxservices.chipsarmorstandmenu.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.ModList;

public class Constants {
    public static final String MOD_ID     = "chipsarmorstandmenu";
    public static final String MOD_PREFIX = MOD_ID + ":";
    public static final String MOD_NAME   = "Chips Armor Stand Menu";
    public static final Logger LOGGER     = LogManager.getLogger(MOD_ID);
    public static final boolean RYOAMICLIGHTS_LOADED = ModList.get().isLoaded("ryoamiclights");
}
