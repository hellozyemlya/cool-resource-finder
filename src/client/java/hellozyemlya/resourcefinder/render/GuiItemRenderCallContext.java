package hellozyemlya.resourcefinder.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiItemRenderCallContext implements ItemRenderCallContext {
    @NotNull
    private final MatrixStack matrices;
    @Nullable
    private final LivingEntity entity;
    @Nullable
    private final World world;
    @NotNull
    private final ItemStack stack;
    private final int x;
    private final int y;
    private final int seed;
    private final int depth;


    public GuiItemRenderCallContext(@NotNull MatrixStack matrices, @Nullable LivingEntity entity, @Nullable World world, @NotNull ItemStack stack, int x, int y, int seed, int depth) {
        this.matrices = matrices;
        this.entity = entity;
        this.world = world;
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.seed = seed;
        this.depth = depth;
    }

    @NotNull
    public MatrixStack getMatrices() {
        return matrices;
    }

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    @NotNull
    public ItemStack getStack() {
        return stack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSeed() {
        return seed;
    }

    public int getDepth() {
        return depth;
    }
}
