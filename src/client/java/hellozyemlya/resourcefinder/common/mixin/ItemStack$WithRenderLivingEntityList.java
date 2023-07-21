package hellozyemlya.resourcefinder.common.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Mixin interface for {@link ItemStack} that holds {@link LivingEntity} that currently used to render given stack.
 */
public interface ItemStack$WithRenderLivingEntityList {
    ObjectArrayList<LivingEntity> cool_resource_finder$getRenderLivingEntityList();

    /**
     * Returns {@link LivingEntity} that used to render current item stack.
     * <p>
     * Usually, there is only one {@link LivingEntity} instance, but some modded environment may call render functions
     * multiple times with different or same entity instances.
     *
     * @param itemStack stack that currently rendered
     * @return list of entities used to render current item stack
     */
    static ObjectArrayList<LivingEntity> getRenderLivingEntityList(ItemStack itemStack) {
        return ((ItemStack$WithRenderLivingEntityList) (Object) itemStack).cool_resource_finder$getRenderLivingEntityList();
    }
}
