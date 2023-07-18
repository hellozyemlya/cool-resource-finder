package hellozyemlya.resourcefinder.render;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Arguments captured when {@link ItemStack} rendered as and item in player hands.
 * </p>
 * Arguments captured from {@link HeldItemRenderer#renderItem(LivingEntity, ItemStack, ModelTransformationMode,
 * boolean, MatrixStack, VertexConsumerProvider, int)} method.
 *
 * @param entity          entity associated with given render operation
 * @param stack           stack to render
 * @param renderMode      entity render mode
 * @param leftHanded      entity rendered in left hand
 * @param matrices        matrices used to render item
 * @param vertexConsumers vertex consumer provider
 * @param light           light level
 */
public record HeldItemRenderCallContext(@NotNull LivingEntity entity, @NotNull ItemStack stack,
                                        @NotNull ModelTransformationMode renderMode, boolean leftHanded,
                                        @NotNull MatrixStack matrices, @NotNull VertexConsumerProvider vertexConsumers,
                                        int light) implements ItemRenderCallContext {
}
