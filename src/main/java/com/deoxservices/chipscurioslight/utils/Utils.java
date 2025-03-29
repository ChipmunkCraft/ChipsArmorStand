package com.deoxservices.chipscurioslight.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class Utils {
    static String modVersion = "";
    
    /**
     * Send message to chat
     * @param player Player
     * @param msg Message to send
     */
    public static void sendMessage(String msg) {
        Player player = Minecraft.getInstance().player;
        if (player==null) return;
        player.displayClientMessage(Component.literal(msg), false);
    }

    /**
     * Log Message
     * @param msg Message to log
     * @param type Log Level Type
     */
    public static void logMsg(String msg, String level) {
        String fullMsg = "[" + Constants.MOD_NAME + "] " + msg;
        switch(level.toLowerCase()) {
            case "info" -> Constants.LOGGER.info(fullMsg);
            case "warn" -> Constants.LOGGER.warn(fullMsg);
            case "debug" -> Constants.LOGGER.debug(fullMsg);
            case "fatal" -> Constants.LOGGER.fatal(fullMsg);
            case "error" -> Constants.LOGGER.error(fullMsg);
            default -> Constants.LOGGER.info(fullMsg);
        }
    }
}
