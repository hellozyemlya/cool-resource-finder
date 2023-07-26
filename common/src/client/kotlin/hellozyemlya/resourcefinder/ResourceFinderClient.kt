package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompassClient
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.network.ClientPlayerEntity

@Suppress("unused")
object ResourceFinderClient : ClientModInitializer {
    val RESOURCE_FINDER_COMPASS_CLIENT = ResourceFinderCompassClient()

    override fun onInitializeClient() {

    }
}