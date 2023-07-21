package hellozyemlya.resourcefinder.common.mixin;

import net.minecraft.item.Item;

/**
 * Mixin interface for {@link Item} that allows to turn on/off tracking of {@link net.minecraft.entity.LivingEntity}
 * instances used to render stacks of given item.
 */
public interface Item$TracksRenderLivingEntity {
    void cool_resource_finder$setTracksRenderLivingEntity(boolean value);

    boolean cool_resource_finder$getTracksRenderLivingEntity();

    static void setTracksRenderLivingEntity(Item item, boolean value) {
        ((Item$TracksRenderLivingEntity) item).cool_resource_finder$setTracksRenderLivingEntity(value);
    }

    static boolean getTracksRenderLivingEntity(Item item) {
        return ((Item$TracksRenderLivingEntity) item).cool_resource_finder$getTracksRenderLivingEntity();
    }
}
