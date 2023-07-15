package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.resourcefinder.ItemStackEx;
import hellozyemlya.resourcefinder.ItemStackPreRenderCallback;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Accessor
    protected abstract ItemRenderer getItemRenderer();

    @Inject(
            method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    private void renderHeldItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ItemStackEx stackEx = (ItemStackEx) (Object) stack;
        @SuppressWarnings("DataFlowIssue")
        ItemStackPreRenderCallback preRenderCallback = stackEx.getPreRenderCallback();

        if (preRenderCallback != null) {
            preRenderCallback.preRender(stack);
        }

        if (stackEx.hasSubStacks()) {
            for (ItemStack subStack : stackEx.getSubStacks()) {
                getItemRenderer().renderItem(entity, subStack, renderMode, leftHanded, matrices, vertexConsumers, entity.world, light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
            }
        }
    }
}
