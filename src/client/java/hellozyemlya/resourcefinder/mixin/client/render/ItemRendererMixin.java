package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.common.ClientItem;
import hellozyemlya.common.ItemHasClientItem;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    protected abstract void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices);
    @Redirect(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"))
    private void renderBakedModelRedirect(ItemRenderer instance, BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
        ClientItem clientItem = ((ItemHasClientItem)stack.getItem()).getClientItem();
        if(clientItem != null) {
            clientItem.transformMatrices(matrices);
        }
        renderBakedItemModel(model, stack, light, overlay, matrices, vertices);
    }

    @Redirect(method = "renderBakedItemQuads", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/color/item/ItemColors;getColor(Lnet/minecraft/item/ItemStack;I)I"))
    private int getColorRedirect(ItemColors instance, ItemStack item, int tintIndex) {
        ClientItem clientItem = ((ItemHasClientItem)item.getItem()).getClientItem();
        if(clientItem != null && clientItem.isOverrideModelColors()) {
            return clientItem.getColor(item, tintIndex);
        }
        return instance.getColor(item, tintIndex);
    }


    @Inject(method = "renderGuiItemModel", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"))
    private void onBeforeGuiItemRender(MatrixStack matrices, ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        ClientItem clientItem = ((ItemHasClientItem)stack.getItem()).getClientItem();
        if(clientItem != null) {
            clientItem.startGuiRender();
        }
    }

    @Inject(method = "renderGuiItemModel", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", shift = At.Shift.AFTER))
    private void onAfterGuiItemRender(MatrixStack matrices, ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        ClientItem clientItem = ((ItemHasClientItem)stack.getItem()).getClientItem();
        if(clientItem != null) {
            clientItem.finishRender();
        }
    }
}
