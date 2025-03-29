package com.deoxservices.chipscurioslight.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final Config CONFIG;
    private static List<String> defaultItemsList = List.of("minecraft:lantern", "minecraft:soul_lantern");

    static {
        Pair<Config, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public static class Config {
        public final ModConfigSpec.ConfigValue<List<? extends String>> curioItems;

        Config(ModConfigSpec.Builder builder) {
            builder.push("general"); // Start a section
            curioItems = builder
                .comment("#List of items to add as curios.")
                .translation("gui.chipscurioslight.config.curioItems")
                .defineListAllowEmpty("curioItems", defaultItemsList, () -> "", o -> o instanceof String);

            builder.pop(); // End the section
        }
    }
}