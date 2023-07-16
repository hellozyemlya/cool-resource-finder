package hellozyemlya.common;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class ClientItem {
    public enum RenderType {
        HELD,
        NO_RENDER
    }
    private RenderType currentRenderType = RenderType.NO_RENDER;

    protected ClientItem(@NotNull Item item) {
        ((ItemHasClientItem) item).setClientItem(this);
    }

    /**
     * Called instead of {@link net.minecraft.client.render.item.HeldItemRenderer#renderItem(
     *net.minecraft.entity.LivingEntity, net.minecraft.item.ItemStack,
     * net.minecraft.client.render.model.json.ModelTransformationMode, boolean,
     * net.minecraft.client.util.math.MatrixStack, net.minecraft.client.render.VertexConsumerProvider, int) renderItem}
     * when engine requires to render itemStack in hand. Default implementation calls {@link #vanillaRenderHeld(LivingEntity,
     * ItemStack, ModelTransformationMode, boolean, MatrixStack, VertexConsumerProvider, int) vanillaRenderHeld}.
     */
    public void renderHeld(@NotNull LivingEntity entity, @NotNull ItemStack itemStack, @NotNull ModelTransformationMode renderMode, boolean leftHanded, @NotNull MatrixStack matrices, @NotNull VertexConsumerProvider vertexConsumers, @NotNull World world, int light, int overlay, int seed) {
        MinecraftClient.getInstance().getItemRenderer().renderItem(entity, itemStack, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
    }

    /**
     * Called before baked model of an itemStack going to be rendered. Matrices already has applied all
     * baked build-in model transformations, hand position transformations, etc.
     *
     * @param stack    itemStack stack, which will be rendered now
     * @param matrices matrices, which will be used for rendering now
     */
    public void transformMatrices(@NotNull ItemStack stack, @NotNull MatrixStack matrices) {

    }

    public void inventoryTick() {

    }

    public RenderType getCurrentRenderType() {
        return currentRenderType;
    }

    public void setCurrentRenderType(RenderType currentRenderType) {
        this.currentRenderType = currentRenderType;
    }
}
