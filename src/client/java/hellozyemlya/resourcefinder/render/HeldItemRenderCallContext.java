package hellozyemlya.resourcefinder.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HeldItemRenderCallContext implements ItemRenderCallContext {
    @NotNull
    private final LivingEntity entity;
    @NotNull
    private final ItemStack stack;
    @NotNull
    private final ModelTransformationMode renderMode;
    private final boolean leftHanded;
    @NotNull
    private final MatrixStack matrices;
    @NotNull
    private final VertexConsumerProvider vertexConsumers;
    private final int light;

    public HeldItemRenderCallContext(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull ModelTransformationMode renderMode, boolean leftHanded, @NotNull MatrixStack matrices, @NotNull VertexConsumerProvider vertexConsumers, int light) {
        this.entity = entity;
        this.stack = stack;
        this.renderMode = renderMode;
        this.leftHanded = leftHanded;
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
        this.light = light;
    }

    @NotNull
    public LivingEntity getEntity() {
        return entity;
    }

    @NotNull
    public ItemStack getStack() {
        return stack;
    }

    @NotNull
    public ModelTransformationMode getRenderMode() {
        return renderMode;
    }

    public boolean isLeftHanded() {
        return leftHanded;
    }

    @NotNull
    public MatrixStack getMatrices() {
        return matrices;
    }

    @NotNull
    public VertexConsumerProvider getVertexConsumers() {
        return vertexConsumers;
    }

    public int getLight() {
        return light;
    }
}
