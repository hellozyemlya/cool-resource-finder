package hellozyemlya.resourcefinder.common;

import net.minecraft.item.ItemStack;

/**
 * Used to get {@link net.minecraft.client.render.model.BakedQuad} color.
 */
@FunctionalInterface
public interface BakedQuadColorProvider {
    int getColor(ItemStack stack, int colorIndex);
}
