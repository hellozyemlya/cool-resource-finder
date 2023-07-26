package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.FinderItemClientSide
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object ResourceFinderClient : ClientModInitializer {
    override fun onInitializeClient() {
        ResourceFinder.RESOURCE_FINDER_ITEM.setClientPart(::FinderItemClientSide)
    }
}