package hellozyemlya.common;

import hellozyemlya.common.render.args.HeldItemRenderArguments;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class ClientItem {
    private enum RenderType {
        HELD,
        GUI,
        NO_RENDER
    }

    private final HeldItemRenderArguments heldRenderArgs = new HeldItemRenderArguments();
    private RenderType currentRender = RenderType.NO_RENDER;

    protected ClientItem(@NotNull Item item) {
        ((ItemHasClientItem) item).setClientItem(this);
    }

    /**
     * Called instead of {@link net.minecraft.client.render.item.HeldItemRenderer#renderItem(
     * net.minecraft.entity.LivingEntity, net.minecraft.item.ItemStack,
     * net.minecraft.client.render.model.json.ModelTransformationMode, boolean,
     * net.minecraft.client.util.math.MatrixStack, net.minecraft.client.render.VertexConsumerProvider, int) renderItem}
     * when engine requires to render itemStack in hand. Default implementation render item in same way as vanilla
     * method.
     * @param entity
     * @param stack
     * @param renderMode
     * @param leftHanded
     * @param matrices
     * @param vertexConsumers
     * @param light
     */
    public void renderHeld(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull ModelTransformationMode renderMode, boolean leftHanded, @NotNull MatrixStack matrices, @NotNull VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient.getInstance().getItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.world, light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
    }

    /**
     * Called before baked model of an itemStack going to be rendered. Matrices already has applied all
     * baked build-in model transformations, hand position transformations, etc.
     *
     * @param matrices matrices, which will be used for rendering now
     */
    public final void transformMatrices(@NotNull MatrixStack matrices) {
        switch (currentRender) {
            case HELD -> {
                transformHeldMatrices(heldRenderArgs.entity, heldRenderArgs.stack, heldRenderArgs.renderMode, heldRenderArgs.leftHanded, matrices, heldRenderArgs.light);
            }
            case GUI -> {

            }
            default -> {
                throw new IllegalStateException("Expected pending render");
            }
        }
    }

    protected void transformHeldMatrices(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull ModelTransformationMode renderMode, boolean leftHanded, @NotNull MatrixStack matrices, int light) {

    }

    /**
     * Returned color used for baked model rendering. Color will be applied only if {@link #isOverrideModelColors()} returns
     * true.
     * <p>
     * Must return hex color, for example 0xffffff for white color.
     *
     * @param stack      item stack to get color for
     * @param colorIndex color index from baked model
     * @return color
     */
    public int getColor(@NotNull ItemStack stack, int colorIndex) {
        return -1;
    }

    /**
     * Indicates if this item must override its model color.
     *
     * @return true, if color must be overridden
     */
    public boolean isOverrideModelColors() {
        return false;
    }

    protected void inventoryTick() {

    }

    public final void startHeldRender(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        ensureNoRender();
        currentRender = RenderType.HELD;
        heldRenderArgs.populate(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
    }

    public final void startGuiRender() {
        ensureNoRender();
        currentRender = RenderType.GUI;
    }

    public final void finishRender() {
        if (currentRender == RenderType.NO_RENDER) {
            throw new IllegalStateException("Expected pending render");
        }
        currentRender = RenderType.NO_RENDER;
    }

    private final void ensureNoRender() {
        if (currentRender != RenderType.NO_RENDER) {
            throw new IllegalStateException("No pending render expected");
        }
    }
}
