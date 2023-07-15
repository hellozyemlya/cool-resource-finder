package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompassRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry

object ResourceFinderClient : ClientModInitializer {
    override fun onInitializeClient() {
        ColorProviderRegistry.ITEM.register(
            ResourceFinderCompassRenderer::getColor,
            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM
        )

        ResourceFinder.RESOURCE_FINDER_ITEM.clientInventoryTick =
            ResourceFinderCompassRenderer.resourceFinderCompassInventoryTick
    }
}