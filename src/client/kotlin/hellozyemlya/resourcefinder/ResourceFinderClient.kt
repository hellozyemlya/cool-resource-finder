package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompassArrowClientItem
import hellozyemlya.resourcefinder.items.ResourceFinderCompassClientItem
import hellozyemlya.resourcefinder.items.ResourceFinderCompassRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry

object ResourceFinderClient : ClientModInitializer {
    val RESOURCE_FINDER_CLIENT_ITEM = ResourceFinderCompassClientItem(ResourceFinder.RESOURCE_FINDER_ITEM)
    val RESOURCE_FINDER_ARROW_CLIENT_ITEM = ResourceFinderCompassArrowClientItem(ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
    override fun onInitializeClient() {
        ColorProviderRegistry.ITEM.register(
            ResourceFinderCompassRenderer::getColor,
            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM
        )

        ResourceFinder.RESOURCE_FINDER_ITEM.clientInventoryTick =
            ResourceFinderCompassRenderer.resourceFinderCompassInventoryTick
    }
}