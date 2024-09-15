package hellozyemlya.resourcefinder.registry

import hellozyemlya.resourcefinder.items.ScanMode
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate

@JvmRecord
data class ResourceEntry(
    val group: Item,
    val color: Int,
    val targetBlocks: List<Block>,
    val rechargeItems: List<ChargeItem>
) {
    companion object {
        private val cylinderPointsCache: MutableMap<Triple<Int, Int, Int>, List<BlockPos>> = mutableMapOf()

        private fun isWithinCylinder(
            center: BlockPos,
            point: BlockPos,
            radius: Int,
            minHeight: Int,
            maxHeight: Int
        ): Boolean {
            val dx = point.x - center.x
            val dz = point.z - center.z
            val distSquared = dx * dx + dz * dz

            // Check if point is within radius in the XZ plane and within height bounds
            return distSquared <= radius * radius && point.y in minHeight..maxHeight
        }

        fun cylinderPoints(yDeltaDown: Int, yDeltaUp: Int, radius: Int): List<BlockPos> {
            val key = Triple(yDeltaDown, yDeltaUp, radius)
            if (cylinderPointsCache.containsKey(key)) {
                return cylinderPointsCache[key]!!
            } else {
                val center = BlockPos(0, 0, 0)
                val visited = mutableSetOf<BlockPos>()
                val queue = mutableListOf<BlockPos>()
                val result = mutableListOf<BlockPos>()
                val minHeight = center.y - yDeltaDown
                val maxHeight = center.y + yDeltaUp

                queue.add(center)
                visited.add(center)

                val directions = listOf(
                    BlockPos(1, 0, 0), BlockPos(-1, 0, 0),
                    BlockPos(0, 0, 1), BlockPos(0, 0, -1),
                    BlockPos(0, 1, 0), BlockPos(0, -1, 0)
                )

                while (queue.isNotEmpty()) {
                    val current = queue.removeAt(0)

                    result.add(current)

                    for (dir in directions) {
                        val neighbor = BlockPos(current.x + dir.x, current.y + dir.y, current.z + dir.z)

                        if (isWithinCylinder(
                                center,
                                neighbor,
                                radius,
                                minHeight,
                                maxHeight
                            ) && neighbor !in visited
                        ) {
                            visited.add(neighbor)
                            queue.add(neighbor)
                        }
                    }
                }
                cylinderPointsCache[key] = result
                return result
            }
        }
    }

    private fun findClosestSpherical(
        horizontalDistance: Int,
        verticalDistance: Int,
        position: BlockPos,
        world: World
    ): Optional<BlockPos> {
        return BlockPos.findClosest(
            position,
            horizontalDistance,
            verticalDistance
        ) { blockPos: BlockPos ->
            targetBlocks.contains(
                world.getBlockState(
                    blockPos
                ).block
            )
        }
    }

    private fun findClosestCircular(
        distance: Int,
        yDeltaDown: Int,
        yDeltaUp: Int,
        position: BlockPos,
        world: World
    ): Optional<BlockPos> {
        val predicate: Predicate<BlockPos> = Predicate { blockPos: BlockPos ->
            targetBlocks.contains(
                world.getBlockState(
                    blockPos
                ).block
            )
        }

        cylinderPoints(yDeltaDown, yDeltaUp, distance).forEach {
            val blockPos = position.add(it)
            if (predicate.test(blockPos)) {
                return Optional.of(blockPos)
            }
        }

        return Optional.empty()
    }

    fun findClosest(radius: Int, position: BlockPos, world: World, mode: ScanMode): Optional<BlockPos> {
        return if (mode == ScanMode.CIRCULAR) {
            findClosestCircular(radius, 2, 1, position, world)
        } else {
            findClosestSpherical(radius, radius, position, world)
        }
    }


    fun getChargeTicks(item: Item): Int {
        return rechargeItems.first { it.item == item }.ticks
    }
}