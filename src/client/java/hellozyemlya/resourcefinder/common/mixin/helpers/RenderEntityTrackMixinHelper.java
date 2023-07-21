package hellozyemlya.resourcefinder.common.mixin.helpers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import hellozyemlya.resourcefinder.common.mixin.Item$TracksRenderLivingEntity;
import hellozyemlya.resourcefinder.common.mixin.ItemStack$WithRenderLivingEntityList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;


public class RenderEntityTrackMixinHelper {
    /**
     * Adds {@link LivingEntity} instance(if not null) to currently rendered {@link ItemStack}. Later it will be
     * possible to retrieve this instance in {@link net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer},
     * for example.
     */
    public static void addEntityToStack(ItemRenderer instance, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, Operation<Void> original, @Nullable LivingEntity entity) {
        boolean tracks = Item$TracksRenderLivingEntity.getTracksRenderLivingEntity(stack.getItem());

        if (tracks && entity != null) {
            ObjectArrayList<LivingEntity> renderEntities = ItemStack$WithRenderLivingEntityList.getRenderLivingEntityList(stack);
            renderEntities.push(entity);
            original.call(instance, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
            renderEntities.remove(entity);
        } else {
            original.call(instance, stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
        }
    }
}
