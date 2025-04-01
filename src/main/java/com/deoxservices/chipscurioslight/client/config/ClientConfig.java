package com.deoxservices.chipscurioslight.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.client.settings.KeyModifier;

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
        public final ModConfigSpec.EnumValue<ModifierKey> ARMOR_STAND_MENU_MODIFIER;
        public final ModConfigSpec.ConfigValue<List<? extends String>> CURIOS_ITEMS;

        Config(ModConfigSpec.Builder builder) {
            builder.push("general"); // Start Section
            ARMOR_STAND_MENU_MODIFIER = builder
                .comment("Modifier key to open the Armor Stand menu with the Use key (Default: Right Click). Default: SHIFT")
                .translation("gui.chipscurioslight.config.ARMOR_STAND_MENU_MODIFIER")
                .defineEnum("ARMOR_STAND_MENU_MODIFIER", ModifierKey.SHIFT);
            builder.pop(); // End the section

            builder.push("curios"); // Start Section
            CURIOS_ITEMS = builder
            .comment("List of items to add as curios.")
            .translation("gui.chipscurioslight.config.CURIOS_ITEMS")
            .defineListAllowEmpty("CURIOS_ITEMS", defaultItemsList, () -> "", o -> o instanceof String);
            builder.pop(); // End the section
        }
    }

    public enum ModifierKey {
        SHIFT(KeyModifier.SHIFT),
        CONTROL(KeyModifier.CONTROL),
        ALT(KeyModifier.ALT);

        private final KeyModifier keyModifier;

        ModifierKey(KeyModifier keyModifier) {
            this.keyModifier = keyModifier;
        }

        public KeyModifier getKeyModifier() {
            return keyModifier;
        }

        @SuppressWarnings("null")
        public boolean isActive() {
            return keyModifier.isActive(null); // Null context checks default key state
        }
    }
}