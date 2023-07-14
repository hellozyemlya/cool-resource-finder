package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompassRenderer
import hellozyemlya.resourcefinder.render.HeldItemRenderRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry

object ResourceFinderClient : ClientModInitializer {
    override fun onInitializeClient() {
        HeldItemRenderRegistry.INSTANCE.register(ResourceFinder.RESOURCE_FINDER_ITEM, ResourceFinderCompassRenderer())
        ColorProviderRegistry.ITEM.register(
            ResourceFinderCompassRenderer::getColor,
            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM
        )
    }
}