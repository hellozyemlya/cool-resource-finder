package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.common.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements GuiItemRenderer {
    @Shadow
    protected abstract void innerRenderInGui(MatrixStack matrices, @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int depth);

    @Override
    public void render(@NotNull MatrixStack matrices, @Nullable LivingEntity entity, @Nullable World world, @NotNull ItemStack stack, int x, int y, int seed, int depth) {
        innerRenderInGui(matrices, entity, world, stack, x, y, seed, depth);
    }

    @Inject(method = "innerRenderInGui(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
            at = @At(value = "TAIL"))
    private void afterGuiRendered(MatrixStack matrices, LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int depth, CallbackInfo ci) {
        // TODO check if every argument is really not null
        ClientItem clientItem = ((ItemHasClientItem) stack.getItem()).getClientItem();
        if (clientItem != null) {
            clientItem.afterGuiItemRendered(this, matrices, entity, world, stack, x, y, seed, depth);
        }
    }


    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderBakedItemModel(Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;)V"))
    private void beforeRenderBakedModel(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        BakedModelEx modelEx = (BakedModelEx) model;
        MatrixTransform matrixTransform = modelEx.getMatrixTransform();

        if (matrixTransform != null) {
            matrixTransform.transform(matrices);
        }
    }
}
