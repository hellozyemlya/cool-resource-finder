package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.resourcefinder.render.HeldItemRenderer;
import hellozyemlya.resourcefinder.render.HeldItemRenderRegistry;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private @Nullable LivingEntity currentLivingEntity;
    private @Nullable ClientWorld currentWorld;
    private @Nullable HeldItemRenderer currentAdditionalHeldItemRenderer;
    private int currentSeed;

    /**
     * @author hellozmlya
     * @reason to apply AdditionalHeldItemRenderer during rendering process
     */
    @Overwrite
    public void renderItem(@Nullable LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed) {
        HeldItemRenderer renderer = HeldItemRenderRegistry.Companion.getINSTANCE().get(item.getItem());
        if (item.isEmpty()) {
            return;
        }

        BakedModel bakedModel = ((ItemRenderer) ((Object) this)).getModel(item, world, entity, seed);

        LivingEntity previousLivingEntity;
        ClientWorld previousWorld;
        HeldItemRenderer previousAdditionalHeldItemRenderer;
        int previousSeed;

        if (renderer != null) {
            previousLivingEntity = currentLivingEntity;
            previousWorld = currentWorld;
            previousAdditionalHeldItemRenderer = currentAdditionalHeldItemRenderer;
            previousSeed = currentSeed;

            currentLivingEntity = entity;
            currentWorld = (ClientWorld) world;
            currentAdditionalHeldItemRenderer = renderer;
            currentSeed = seed;

            ((ItemRenderer) ((Object) this)).renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, bakedModel);

            currentLivingEntity = previousLivingEntity;
            currentWorld = previousWorld;
            currentAdditionalHeldItemRenderer = previousAdditionalHeldItemRenderer;
            currentSeed = previousSeed;
        } else {
            ((ItemRenderer) ((Object) this)).renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, bakedModel);
        }
    }

    @Inject(method = "renderBakedItemModel", at = @At("TAIL"))
    private void renderAdditionalModels(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices, CallbackInfo ci) {
        if (currentAdditionalHeldItemRenderer != null) {
            HeldItemRenderer renderer = currentAdditionalHeldItemRenderer;
            currentAdditionalHeldItemRenderer = null;
            renderer.render((BakedItemModelRenderer) this, currentLivingEntity, currentWorld, stack, light, overlay, currentSeed, matrices, vertices);
        }
    }
}