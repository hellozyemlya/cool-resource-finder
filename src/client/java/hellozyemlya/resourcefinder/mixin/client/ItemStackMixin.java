package hellozyemlya.resourcefinder.mixin.client;

import hellozyemlya.resourcefinder.ItemStackEx;
import hellozyemlya.resourcefinder.ItemStackPreRenderCallback;
import hellozyemlya.resourcefinder.MatrixModifier;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackEx {
    private List<ItemStack> subStacks;
    private MatrixModifier transform;

    private ItemStackPreRenderCallback preRenderCallback;

    @Override
    public void setModelTransform(MatrixModifier transform) {
        this.transform = transform;
    }

    @Override
    public MatrixModifier getModelTransform() {
        return this.transform;
    }

    @NotNull
    @Override
    public List<ItemStack> getSubStacks() {
        if (subStacks == null) {
            subStacks = new ArrayList<>();
        }
        return subStacks;
    }

    @Override
    public boolean hasSubStacks() {
        return subStacks == null || subStacks.size() > 0;
    }

    @Override
    public void setPreRenderCallback(ItemStackPreRenderCallback callback) {
        this.preRenderCallback = callback;
    }

    @Override
    public ItemStackPreRenderCallback getPreRenderCallback() {
        return preRenderCallback;
    }
}
