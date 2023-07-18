package hellozyemlya.resourcefinder.mixin.client.render;

import hellozyemlya.common.GuiItemRenderer;
import hellozyemlya.resourcefinder.render.GuiItemRenderCallContext;
import hellozyemlya.resourcefinder.render.ItemRenderCallStack;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements GuiItemRenderer {

    @Inject(method = "innerRenderInGui(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
            at = @At(value = "HEAD"))
    private void startGuiItemRenderContext(MatrixStack matrices, LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int depth, CallbackInfo ci) {
        ItemRenderCallStack.INSTANCE.withNewContext(new GuiItemRenderCallContext(matrices, entity, world, stack, x, y, seed, depth));
    }

    @Inject(method = "innerRenderInGui(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
            at = @At(value = "RETURN"))
    private void finishGuiItemRenderContext(MatrixStack matrices, LivingEntity entity, World world, ItemStack stack, int x, int y, int seed, int depth, CallbackInfo ci) {
        @SuppressWarnings("unused")
        GuiItemRenderCallContext unused = ItemRenderCallStack.INSTANCE.finishContext();
    }
}
