package hellozyemlya.resourcefinder.registry

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

@JvmRecord
data class ResourceEntry(
    val group: Item,
    val color: Int,
    val targetBlocks: List<Block>,
    val rechargeItems: List<ChargeItem>
) {

    fun findClosest(distance: Int, position: BlockPos, world: World): Optional<BlockPos> {
        return BlockPos.findClosest(
            position,
            distance,
            distance
        ) { blockPos: BlockPos ->
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