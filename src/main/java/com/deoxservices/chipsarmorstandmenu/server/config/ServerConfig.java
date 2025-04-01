package com.deoxservices.chipsarmorstandmenu.server.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

public class ServerConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final Config CONFIG;

    static {
        Pair<Config, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public static class Config {
        public final ModConfigSpec.ConfigValue<Integer> TIMEOUT_SECONDS;

        Config(ModConfigSpec.Builder builder) {
            builder.push("general"); // Start Section
            TIMEOUT_SECONDS = builder
                .comment("Armor Stand Menu Timeout in seconds. Default: 30 seconds")
                .translation("gui.chipsarmorstandmenu.config.TIMEOUT_SECONDS")
                .define("TIMEOUT_SECONDS", 30);
            builder.pop(); // End the section

        }
    }
}