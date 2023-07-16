package hellozyemlya.common.render.args;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class HeldItemRenderArguments {
    public LivingEntity entity;
    public ItemStack stack;
    public ModelTransformationMode renderMode;
    public boolean leftHanded;
    public MatrixStack matrices;
    public VertexConsumerProvider vertexConsumers;
    public int light;

    public void populate(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        this.entity = entity;
        this.stack = stack;
        this.renderMode = renderMode;
        this.leftHanded = leftHanded;
        this.matrices = matrices;
        this.vertexConsumers = vertexConsumers;
        this.light = light;
    }
}
