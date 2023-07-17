package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompassArrowClientItem
import hellozyemlya.resourcefinder.items.ResourceFinderCompassClientItem
import net.fabricmc.api.ClientModInitializer

object ResourceFinderClient : ClientModInitializer {
    val RESOURCE_FINDER_CLIENT_ITEM = ResourceFinderCompassClientItem(ResourceFinder.RESOURCE_FINDER_ITEM)
    val RESOURCE_FINDER_ARROW_CLIENT_ITEM = ResourceFinderCompassArrowClientItem(ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
    override fun onInitializeClient() {
    }
}