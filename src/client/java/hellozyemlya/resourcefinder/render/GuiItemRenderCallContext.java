package hellozyemlya.resourcefinder.render;

import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Arguments captured when {@link ItemStack} rendered as item.
 * <p>
 * Arguments captured from {@link ItemRenderer#innerRenderInGui(MatrixStack, LivingEntity, World, ItemStack, int, int,
 * int, int)} method.
 *
 * @param matrices matrices used to render item
 * @param entity entity associated with given render operation
 * @param world world associated with given render operation
 * @param stack stack to render
 * @param x screen x coordinate
 * @param y screen y coordinate
 * @param seed ?
 * @param depth ?
 */
public record GuiItemRenderCallContext(@NotNull MatrixStack matrices, @Nullable LivingEntity entity,
                                       @Nullable World world, @NotNull ItemStack stack, int x, int y, int seed,
                                       int depth) implements ItemRenderCallContext {
}
