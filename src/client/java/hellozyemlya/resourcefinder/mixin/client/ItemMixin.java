package hellozyemlya.resourcefinder.mixin.client;

import hellozyemlya.resourcefinder.common.mixin.Item$TracksRenderLivingEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class ItemMixin implements Item$TracksRenderLivingEntity {
    @Unique
    private boolean tracksLivingEntity = false;

    @Override
    public void cool_resource_finder$setTracksRenderLivingEntity(boolean value) {
        tracksLivingEntity = value;
    }

    @Override
    public boolean cool_resource_finder$getTracksRenderLivingEntity() {
        return tracksLivingEntity;
    }
}
