package hellozyemlya.resourcefinder

import com.google.common.collect.ImmutableList
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class ScanRegistry private constructor() {
    @JvmRecord
    data class RegistryEntry(
        val index: Int,
        val color: Int,
        val targetBlocks: List<Block>,
        val rechargeItems: List<Item>,
        val resource: Item
    ) {
        fun findClosest(distance: Int, position: BlockPos?, world: World): Optional<BlockPos> {
            return BlockPos
                .findClosest(
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
    }

    private val registryEntryMap: MutableMap<Int, RegistryEntry> = HashMap()
    private fun addBlockGroup(
        idx: Int,
        color: Int,
        targetBlocks: List<Block>,
        rechargeItems: List<Item>,
        resource: Item
    ) {
        if (registryEntryMap.containsKey(idx)) {
            throw IndexOutOfBoundsException()
        }
        registryEntryMap[idx] = RegistryEntry(idx, color, targetBlocks, rechargeItems, resource)
    }

    fun getByIndex(idx: Int): RegistryEntry? {
        return registryEntryMap[idx]
    }

    fun getByChargingItem(item: Item): Optional<RegistryEntry> {
        return registryEntryMap.values.stream().filter { (_, _, _, rechargeItems): RegistryEntry ->
            rechargeItems.contains(
                item
            )
        }.findFirst()
    }

    init {
        addBlockGroup(
            0, 0xff0000, ImmutableList.of(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE), ImmutableList.of(
                Items.REDSTONE_BLOCK
            ), Items.REDSTONE
        )
        addBlockGroup(
            1, 0x1D969A, ImmutableList.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE), ImmutableList.of(
                Items.DIAMOND_BLOCK
            ), Items.DIAMOND
        )
        addBlockGroup(
            2,
            0x363636,
            ImmutableList.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
            ImmutableList.of(Items.COAL_BLOCK),
            Items.COAL
        )
    }

    companion object {
        val INSTANCE = ScanRegistry()
    }
}
