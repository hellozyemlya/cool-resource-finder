package hellozyemlya.resourcefinder.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hellozyemlya.resourcefinder.ItemStackWithRenderLivingEntityList;
import hellozyemlya.resourcefinder.TracksRenderLivingEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @SuppressWarnings("unused")
    @WrapOperation(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V"))
    private void associateStackWithEntity(ItemRenderer instance, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, Operation<Void> original, @Nullable LivingEntity entity) {
        boolean tracks = TracksRenderLivingEntity.getTracksRenderLivingEntity(stack.getItem());

        if (tracks && entity != null) {
            ObjectArrayList<LivingEntity> renderEntities = ItemStackWithRenderLivingEntityList.getRenderLivingEntityList(stack);
            renderEntities.push(entity);
            original.call(instance, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
            renderEntities.remove(entity);
        } else {
            original.call(instance, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
        }
    }

    @SuppressWarnings("unused")
    @WrapOperation(method = "innerRenderInGui(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItemModel(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/render/model/BakedModel;)V"))
    private void associateStackWithEntityGui(ItemRenderer instance, MatrixStack matrices, ItemStack stack, int x, int y, BakedModel model, Operation<Void> original, MatrixStack makeWrapHappy, @Nullable LivingEntity entity) {
        boolean tracks = TracksRenderLivingEntity.getTracksRenderLivingEntity(stack.getItem());

        if (tracks && entity != null) {
            ObjectArrayList<LivingEntity> renderEntities = ItemStackWithRenderLivingEntityList.getRenderLivingEntityList(stack);
            renderEntities.push(entity);
            original.call(instance, matrices, stack, x, y, model);
            renderEntities.remove(entity);
        } else {
            original.call(instance, matrices, stack, x, y, model);
        }
    }
}
