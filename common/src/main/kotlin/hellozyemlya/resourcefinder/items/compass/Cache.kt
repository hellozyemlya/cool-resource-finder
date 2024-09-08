package hellozyemlya.resourcefinder.items.compass

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos


@Serializable
data class CompassScanItem(var lifetimeTicks: Int, @Contextual var target: BlockPos? = null, var color: Int = 0)

@Serializable
data class CompassData(
    var scanTimeoutTicks: Int = 0,
    val scanList: MutableMap<@Contextual Item, CompassScanItem> = mutableMapOf()
)