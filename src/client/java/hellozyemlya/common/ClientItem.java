package hellozyemlya.common;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ClientItem {
    protected ClientItem(@NotNull Item item) {
        ((ItemHasClientItem) item).setClientItem(this);
    }


    public void afterHeldItemRenderer(@NotNull LivingEntity entity,
                                      @NotNull ItemStack stack,
                                      @NotNull ModelTransformationMode renderMode,
                                      boolean leftHanded,
                                      @NotNull MatrixStack matrices,
                                      @NotNull VertexConsumerProvider vertexConsumers,
                                      int light) {

    }
    public void afterItemRendered(@NotNull ItemStack stack,
                                  @NotNull ModelTransformationMode renderMode,
                                  boolean leftHanded,
                                  @NotNull MatrixStack matrices,
                                  @NotNull VertexConsumerProvider vertexConsumers,
                                  int light,
                                  int overlay,
                                  @NotNull BakedModel model) {

    }

    protected void inventoryTick() {

    }
}
