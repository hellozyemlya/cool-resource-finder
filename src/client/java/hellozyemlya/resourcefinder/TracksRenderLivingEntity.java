package hellozyemlya.resourcefinder;

import net.minecraft.item.Item;

public interface TracksRenderLivingEntity {
    void cool_resource_finder$setTracksRenderLivingEntity(boolean value);

    boolean cool_resource_finder$getTracksRenderLivingEntity();

    static void setTracksRenderLivingEntity(Item item, boolean value) {
        ((TracksRenderLivingEntity) item).cool_resource_finder$setTracksRenderLivingEntity(value);
    }

    static boolean getTracksRenderLivingEntity(Item item) {
        return ((TracksRenderLivingEntity) item).cool_resource_finder$getTracksRenderLivingEntity();
    }
}
