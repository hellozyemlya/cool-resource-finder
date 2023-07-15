package hellozyemlya.resourcefinder;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ItemStackEx {
    void setModelTransform(MatrixModifier transform);

    MatrixModifier getModelTransform();

    @NotNull
    List<ItemStack> getSubStacks();

    boolean hasSubStacks();

    void setPreRenderCallback(ItemStackPreRenderCallback callback);

    ItemStackPreRenderCallback getPreRenderCallback();

}
