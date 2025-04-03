package com.deoxservices.chipsarmorstandmenu.client.gui.components;

import javax.annotation.Nullable;

import com.deoxservices.chipsarmorstandmenu.utils.Constants;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomCheckbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/checkbox");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/checkbox_selected_highlighted");
    private static final int TEXT_COLOR = 14737632;
    private boolean selected;
    private final CustomCheckbox.OnValueChange onValueChange;
    private final MultiLineTextWidget textWidget;

    public CustomCheckbox(int x, int y, int maxWidth, Component message, Font font, boolean selected, CustomCheckbox.OnValueChange onValueChange) {
        super(x, y, 0, 0, message);
        this.width = this.getAdjustedWidth(maxWidth, message, font);
        this.textWidget = new MultiLineTextWidget(message, font).setMaxWidth(this.width).setColor(TEXT_COLOR);
        this.height = this.getAdjustedHeight(font);
        this.selected = selected;
        this.onValueChange = onValueChange;
    }

    private int getAdjustedWidth(int maxWidth, Component message, Font font) {
        return Math.min(getDefaultWidth(message, font), maxWidth);
    }

    private int getAdjustedHeight(Font font) {
        return Math.max(getBoxSize(font), this.textWidget.getHeight());
    }

    static int getDefaultWidth(Component message, Font font) {
        return getBoxSize(font) + 4 + font.width(message);
    }

    public static CustomCheckbox.Builder builder(Component message, Font font) {
        return new CustomCheckbox.Builder(message, font);
    }

    public static int getBoxSize(Font font) {
        return 9 + 8;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @SuppressWarnings("null")
    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @SuppressWarnings("null")
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        Font font = minecraft.font;
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourcelocation = CHECKBOX_SPRITE;
        if (this.selected()) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }
        int i = getBoxSize(font) / 2;
        guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), i, i);
        int j = this.getX() + i + 4;
        int k = this.getY() + i / 2 - this.textWidget.getHeight() / 2;
        this.textWidget.setPosition(j, k);
        this.textWidget.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    // Add a setter for state updates
    public void setSelected(boolean selected) {
        // Use reflection or direct field access if needed—Checkbox has no public setter
        // For now, simulate click to toggle if different
        if (this.selected() != selected) {
            this.onPress();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private int maxWidth;
        private int x = 0;
        private int y = 0;
        private CustomCheckbox.OnValueChange onValueChange = CustomCheckbox.OnValueChange.NOP;
        @SuppressWarnings("unused")
        private boolean selected = false;
        private boolean defaultValue = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component message, Font font) {
            this.message = message;
            this.font = font;
            this.maxWidth = CustomCheckbox.getDefaultWidth(message, font);
        }

        public CustomCheckbox.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public CustomCheckbox.Builder onValueChange(CustomCheckbox.OnValueChange onValueChange) {
            this.onValueChange = onValueChange;
            return this;
        }

        public CustomCheckbox.Builder selected(boolean selected) {
            this.selected = selected;
            this.option = null;
            return this;
        }

        public CustomCheckbox.Builder selected(OptionInstance<Boolean> option) {
            this.option = option;
            this.selected = option.get();
            return this;
        }

        public CustomCheckbox.Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CustomCheckbox.Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        @SuppressWarnings("unused")
        public CustomCheckbox build() {
            CustomCheckbox.OnValueChange checkbox$onvaluechange = this.option == null ? this.onValueChange : (p_309064_, p_308939_) -> {
                this.option.set(p_308939_);
                this.onValueChange.onValueChange(p_309064_, p_308939_);
            };
            CustomCheckbox checkbox = new CustomCheckbox(this.x, this.y, this.maxWidth, this.message, this.font, this.defaultValue, this.onValueChange);
            checkbox.setTooltip(this.tooltip);
            return checkbox;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        CustomCheckbox.OnValueChange NOP = (p_309046_, p_309014_) -> {
        };
        void onValueChange(CustomCheckbox checkbox, boolean value);
    }
}