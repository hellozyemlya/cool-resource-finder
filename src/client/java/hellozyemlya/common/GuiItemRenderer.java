package hellozyemlya.common;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface GuiItemRenderer {
    void render(@NotNull MatrixStack matrices, @Nullable LivingEntity entity,
                @Nullable World world, @NotNull ItemStack stack, int x, int y, int seed,
                int depth);
}
