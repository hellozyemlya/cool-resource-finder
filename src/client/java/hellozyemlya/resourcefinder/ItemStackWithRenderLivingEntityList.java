package hellozyemlya.resourcefinder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface ItemStackWithRenderLivingEntityList {
    ObjectArrayList<LivingEntity> cool_resource_finder$getRenderLivingEntityList();

    static ObjectArrayList<LivingEntity> getRenderLivingEntityList(ItemStack itemStack) {
        return ((ItemStackWithRenderLivingEntityList)(Object)itemStack).cool_resource_finder$getRenderLivingEntityList();
    }
}
