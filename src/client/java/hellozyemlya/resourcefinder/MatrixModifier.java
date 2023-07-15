package hellozyemlya.resourcefinder;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface MatrixModifier {
    void modify(@NotNull ItemStack itemStack, @NotNull MatrixStack matrices);
}
