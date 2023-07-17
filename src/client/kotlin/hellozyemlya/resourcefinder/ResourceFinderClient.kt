package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompassClientItem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.client.util.ModelIdentifier

object ResourceFinderClient : ClientModInitializer {
    final val RESOURCE_FINDER_CLIENT_ITEM = ResourceFinderCompassClientItem(ResourceFinder.RESOURCE_FINDER_ITEM)
    final val ARROW_MODEL_ID = ModelIdentifier("cool-resource-finder", "resource_finder_compass_arrow", "inventory")
    override fun onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerModelProvider { _, out ->
            out.accept(ARROW_MODEL_ID)
        }
    }
}