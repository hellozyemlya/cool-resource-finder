package hellozyemlya.common;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ClientItem {
    protected ClientItem(@NotNull Item item) {
        ((ItemHasClientItem) item).setClientItem(this);
    }


    public void afterHeldItemRendered(@NotNull LivingEntity entity, @NotNull ItemStack stack,
                                      @NotNull ModelTransformationMode renderMode, boolean leftHanded,
                                      @NotNull MatrixStack matrices, @NotNull VertexConsumerProvider vertexConsumers,
                                      int light) {

    }

    public void afterGuiItemRendered(@NotNull GuiItemRenderer itemRenderer, @NotNull MatrixStack matrices, @Nullable LivingEntity entity,
                                     @Nullable World world, @NotNull ItemStack stack, int x, int y, int seed,
                                     int depth) {

    }

    protected void inventoryTick() {

    }
}
