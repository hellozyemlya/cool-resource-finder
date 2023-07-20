package hellozyemlya.resourcefinder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemStackToLivingEntity {
    public static void addEntityToStack(ItemRenderer instance, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, Operation<Void> original, @Nullable LivingEntity entity) {
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
}
