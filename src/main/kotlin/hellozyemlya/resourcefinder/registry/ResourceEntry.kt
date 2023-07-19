package hellozyemlya.resourcefinder.registry

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

@JvmRecord
data class ResourceEntry(
    val index: Int,
    val color: Int,
    val targetBlocks: List<Block>,
    val rechargeItems: List<ChargeItem>,
    val displayItem: Item
) {
    fun canBeChargedBy(item: Item): Boolean {
        return rechargeItems.find { it.item == item } != null
    }

    fun findClosest(distance: Int, position: BlockPos?, world: World): Optional<BlockPos> {
        return BlockPos.findClosest(
            position,
            distance,
            distance
        ) { blockPos: BlockPos? ->
            targetBlocks.contains(
                world.getBlockState(
                    blockPos
                ).block
            )
        }
    }

    fun getChargeTicks(item: Item): Int {
        return rechargeItems.first { it.item == item }.ticks
    }
}