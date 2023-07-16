package hellozyemlya.common;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ClientItem {
    public ClientItem(@NotNull Item item) {
    }

    /**
     * Renders give item stack in vanilla style without any transformations.
     *
     * @param entity          entity, which holds item
     * @param stack           stack of given item
     * @param renderMode      render mode, usually used to pick transformation for model
     * @param leftHanded      if held in left hand
     * @param matrices        matrices to transform model rendering
     * @param vertexConsumers where to put model data
     * @param light           light
     */
    protected void vanillaRenderHeld(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode,
                                     boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                     int light) {
        if (stack.isEmpty()) {
            return;
        }
        MinecraftClient.getInstance().getItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices,
                vertexConsumers, entity.world, light, OverlayTexture.DEFAULT_UV,
                entity.getId() + renderMode.ordinal()
        );
    }

    /**
     * Called instead of {@link net.minecraft.client.render.item.HeldItemRenderer#renderItem(
     * net.minecraft.entity.LivingEntity, net.minecraft.item.ItemStack,
     * net.minecraft.client.render.model.json.ModelTransformationMode, boolean,
     * net.minecraft.client.util.math.MatrixStack, net.minecraft.client.render.VertexConsumerProvider, int) renderItem}
     * when engine requires to render item in hand. Default implementation calls {@link #vanillaRenderHeld(LivingEntity,
     * ItemStack, ModelTransformationMode, boolean, MatrixStack, VertexConsumerProvider, int) vanillaRenderHeld}.
     *
     * @param entity          entity, which holds item
     * @param stack           stack of given item
     * @param renderMode      render mode, usually used to pick transformation for model
     * @param leftHanded      if held in left hand
     * @param matrices        matrices to transform model rendering
     * @param vertexConsumers where to put model data
     * @param light           light
     */
    protected void renderHeld(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        vanillaRenderHeld(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
    }

    protected abstract void inventoryTick();
}
