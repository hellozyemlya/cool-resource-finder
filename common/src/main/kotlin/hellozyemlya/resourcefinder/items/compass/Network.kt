package hellozyemlya.resourcefinder.items.compass

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.network.packet.s2cPacket

@Serializable
data class ScanItemChange(
    val compassId: Int,
    @Contextual val chargeItem: Item,
    val ticks: Int,
    @Contextual val target: BlockPos? = null,
    val color: Int = 0
)

@OptIn(ExperimentalSerializationApi::class)
object Packets {
    public val CACHE_PACKET = s2cPacket<ResourceFinderCompassCache>(Identifier(MOD_NAMESPACE, "cache"), NETWORK_CBOR)
    public val SCAN_ITEM_CHANGE_PACKET =
        s2cPacket<List<ScanItemChange>>(Identifier(MOD_NAMESPACE, "scan_item_change"), NETWORK_CBOR)

}