package com.deoxservices.chipsarmorstand.compat.curios;

import com.deoxservices.chipsarmorstand.utils.Constants;
import com.deoxservices.chipsarmorstand.utils.Utils;
//import com.deoxservices.chipsarmorstand.world.item.ItemDisplayContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class DynamicRenderer extends ArmorStandRenderer implements ICurioRenderer {
    private final Item item;
    private float xScale = 1.0f;
    private float yScale = 1.0f;
    private float zScale = 1.0f;
    private float xOffset = 0.0f;
    private float yOffset = 0.0f;
    private float zOffset = 0.0f;
    private float xRot = 0.0f;
    private float yRot = 0.0f;
    private float zRot = 0.0f;
    private boolean is3d = false;

    public DynamicRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.item = null; // For armor stands—item-specific data comes from Curios
    }

    public DynamicRenderer(Item item) {
        super(new EntityRendererProvider.Context(
            Minecraft.getInstance().getEntityRenderDispatcher(),
            Minecraft.getInstance().getItemRenderer(),
            Minecraft.getInstance().getBlockRenderer(),
            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(),
            Minecraft.getInstance().getResourceManager(),
            Minecraft.getInstance().getEntityModels(),
            Minecraft.getInstance().font
        ));
        this.item = item;
        loadRenderData();
    }

    private void loadRenderData() {
        if (item == null) return; // Skip for armor stand renderer
        String itemName = item.getDescriptionId().replace("item.", "").replace(".", ":");
        String shortName = itemName.split(":")[2]; // e.g., "lantern" from "minecraft:lantern"
        ResourceLocation jsonLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "models/curios/" + shortName + ".json");
        Utils.logMsg("Loaded: " + jsonLocation, "debug");

        try {
            JsonObject json = loadJsonWithParents(jsonLocation);
            applyJsonData(json);
        } catch (Exception e) {
            Utils.logMsg("Failed to load render data for " + itemName + ": " + e.getMessage(), "error");
        }
    }

    private JsonObject loadJsonWithParents(ResourceLocation location) throws Exception {
        var resource = Minecraft.getInstance().getResourceManager().getResource(location).orElse(null);
        if (resource == null) {
            Utils.logMsg("No custom render JSON found for " + location + ", using defaults", "debug");
            return new JsonObject();
        }
        try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("parent")) {
                String parentPath = json.get("parent").getAsString();
                if (!parentPath.endsWith(".json")) parentPath += ".json";
                ResourceLocation parentLocation = ResourceLocation.parse(parentPath);
                JsonObject parentJson = loadJsonWithParents(parentLocation);
                parentJson.entrySet().forEach(entry -> {
                    if (!json.has(entry.getKey())) json.add(entry.getKey(), entry.getValue());
                });
            }
            return json;
        }
    }

    private void applyJsonData(JsonObject json) {
        if (json.has("scale")) {
            JsonObject scale = json.get("scale").getAsJsonObject();
            this.xScale = scale.has("x") ? scale.get("x").getAsFloat() : 1.0f;
            this.yScale = scale.has("y") ? scale.get("y").getAsFloat() : 1.0f;
            this.zScale = scale.has("z") ? scale.get("z").getAsFloat() : 1.0f;
        }
        if (json.has("offsets")) {
            JsonObject offsets = json.get("offsets").getAsJsonObject();
            this.xOffset = offsets.has("x") ? offsets.get("x").getAsFloat() : 0.0f;
            this.yOffset = offsets.has("y") ? offsets.get("y").getAsFloat() : 0.0f;
            this.zOffset = offsets.has("z") ? offsets.get("z").getAsFloat() : 0.0f;
        }
        if (json.has("rotations")) {
            JsonObject rotations = json.get("rotations").getAsJsonObject();
            this.xRot = rotations.has("x") ? rotations.get("x").getAsFloat() : 0.0f;
            this.yRot = rotations.has("y") ? rotations.get("y").getAsFloat() : 0.0f;
            this.zRot = rotations.has("z") ? rotations.get("z").getAsFloat() : 0.0f;
        }
        if (json.has("is3d")) {
            this.is3d = json.get("is3d").getAsBoolean();
        }
    }

    @Override
    public <T extends net.minecraft.world.entity.LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        poseStack.pushPose();

        poseStack.translate(xOffset, yOffset, zOffset);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
        poseStack.scale(xScale, yScale, zScale);
        if (is3d && item instanceof BlockItem blockItem) {
            // 3D Block Rendering
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            Block block = blockItem.getBlock();
            BlockState state = block.defaultBlockState();
            ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath(
                    BuiltInRegistries.ITEM.getKey(item).getNamespace(), "block/" + BuiltInRegistries.ITEM.getKey(item).getPath());
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                    new ModelResourceLocation(modelLoc, ""));

            if (model == null || model == Minecraft.getInstance().getModelManager().getMissingModel()) {
                //Utils.logMsg("No block model found for " + modelLoc + ", falling back to item model", "warn");
                renderItem(stack, slotContext, poseStack, buffer, light);
            } else {
                blockRenderer.renderSingleBlock(state, poseStack, buffer, light, light, ModelData.EMPTY, RenderType.SOLID);
            }
        } else {
            // 2D Item Rendering
            renderItem(stack, slotContext, poseStack, buffer, light);
        }

        poseStack.popPose();
    }

    // ArmorStandRenderer for armor stands
    @SuppressWarnings("null")
    @Override
    public void render(ArmorStand entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
            for (String slot : handler.getCurios().keySet()) {
                ItemStack stack = handler.getStacksHandler(slot)
                    .map(h -> h.getStacks().getStackInSlot(0))
                    .orElse(ItemStack.EMPTY);
                if (!stack.isEmpty()) {
                    poseStack.pushPose();
                    poseStack.translate(xOffset, yOffset + 1.0f, zOffset); // Adjust for armor stand
                    poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
                    poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
                    poseStack.scale(xScale, yScale, zScale);

                    Item renderItem = stack.getItem();
                    if (is3d && renderItem instanceof BlockItem blockItem) {
                        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                        Block block = blockItem.getBlock();
                        BlockState state = block.defaultBlockState();
                        ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath(
                            BuiltInRegistries.ITEM.getKey(renderItem).getNamespace(),
                            "block/" + BuiltInRegistries.ITEM.getKey(renderItem).getPath()
                        );
                        BakedModel model = Minecraft.getInstance().getModelManager().getModel(
                            new ModelResourceLocation(modelLoc, "")
                        );
                        if (model == null || model == Minecraft.getInstance().getModelManager().getMissingModel()) {
                            renderItem(stack, new SlotContext(slot, entity, 0, false, true), poseStack, buffer, packedLight);
                        } else {
                            blockRenderer.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
                        }
                    } else {
                        renderItem(stack, new SlotContext(slot, entity, 0, false, true), poseStack, buffer, packedLight);
                    }
                    poseStack.popPose();
                }
            }
        });
    }

    private void renderItem(ItemStack stack, SlotContext slotContext, PoseStack poseStack, MultiBufferSource buffer, int light) {
        //ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        //BakedModel model = itemRenderer.getModel(stack, slotContext.entity().level(), slotContext.entity(), 0);
        //itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, light, OverlayTexture.NO_OVERLAY, model);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.ARMOR_STAND, DynamicRenderer::new);
    }
}