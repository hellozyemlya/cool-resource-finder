package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.common.BakedModelEx;
import hellozyemlya.common.ClientItem;
import hellozyemlya.common.ItemHasClientItem;
import hellozyemlya.common.MatrixTransform;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
//    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
//            at = @At(value = "TAIL"))
//    private void afterItemRendered(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
//        // TODO check if every argument is really not null
//        ClientItem clientItem = ((ItemHasClientItem) stack.getItem()).getClientItem();
//        if (clientItem != null) {
//            clientItem.afterItemRendered(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
//        }
//    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
    at= @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"))
    private void beforeRenderBakedModel(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        BakedModelEx modelEx = (BakedModelEx)model;
        MatrixTransform matrixTransform = modelEx.getMatrixTransform();

        if(matrixTransform != null) {
            matrixTransform.transform(matrices);
        }
    }


//    @Redirect(
//            method = "renderBakedItemQuads", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/color/item/ItemColors;getColor(Lnet/minecraft/item/ItemStack;I)I")
//
//    )
//    private int redirectGetColor(ItemColors instance, ItemStack item, int tintIndex) {
//
//        return 0xff0000;
//    }
}
