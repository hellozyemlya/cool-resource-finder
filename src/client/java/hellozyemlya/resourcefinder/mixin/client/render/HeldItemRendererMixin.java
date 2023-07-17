package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.common.ClientItem;
import hellozyemlya.common.ItemHasClientItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
    at = @At("HEAD"),
    cancellable = true)
    private void onItemRender(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(stack.isEmpty()) {
            ci.cancel();
        }
        ClientItem clientItem = ((ItemHasClientItem)stack.getItem()).getClientItem();
        if(clientItem != null) {
            clientItem.startHeldRender(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
            clientItem.renderHeld(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light);
            clientItem.finishRender();
            ci.cancel();
        }
    }

//    @Redirect(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
//    at=@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V"))
//    private void renderItemRedirect(ItemRenderer instance, LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, int light, int overlay, int seed) {
//        ClientItem clientItem = ((ItemHasClientItem)item.getItem()).getClientItem();
//        if(clientItem != null) {
//            clientItem.startHeldRender(entity, item, renderMode, leftHanded, matrices, vertexConsumers, light);
//            clientItem.renderHeld(entity, item, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
//            clientItem.finishRender();
//        } else {
//            instance.renderItem(entity, item, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed);
//        }
//    }
}
