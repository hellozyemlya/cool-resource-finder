package hellozyemlya.resourcefinder.mixin.client;

import hellozyemlya.resourcefinder.ItemStackWithRenderLivingEntityList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackWithRenderLivingEntityList {
    @Unique
    private final ObjectArrayList<LivingEntity> renderEntities = new ObjectArrayList<>();

    @Override
    public ObjectArrayList<LivingEntity> cool_resource_finder$getRenderLivingEntityList() {
        if (renderEntities.size() > 50) {
            throw new IllegalStateException("leak!");
        }
        return renderEntities;
    }
}
