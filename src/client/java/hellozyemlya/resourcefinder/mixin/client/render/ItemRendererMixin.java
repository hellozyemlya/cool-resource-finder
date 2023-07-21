package hellozyemlya.resourcefinder.mixin.client.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hellozyemlya.resourcefinder.common.BakedQuadColorOverride;
import hellozyemlya.resourcefinder.common.BakedQuadColorProvider;
import hellozyemlya.resourcefinder.common.mixin.helpers.RenderEntityTrackMixinHelper;
import net.minecraft.client.color.item.ItemColors;
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
        RenderEntityTrackMixinHelper.addEntityToStack(instance, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model, original, entity);
    }


    /**
     * Overrides {@link net.minecraft.client.render.model.BakedQuad} color if {@link BakedQuadColorProvider} provided
     * globally.
     */
    @SuppressWarnings("unused")
    @WrapOperation(method = "renderBakedItemQuads",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/item/ItemColors;getColor(Lnet/minecraft/item/ItemStack;I)I"))
    private int getColoredModelColor(ItemColors instance, ItemStack item, int tintIndex, Operation<Integer> original) {
        BakedQuadColorProvider override = BakedQuadColorOverride.getOverride();
        if (override != null) {
            return override.getColor(item, tintIndex);
        }
        return original.call(instance, item, tintIndex);
    }
}
