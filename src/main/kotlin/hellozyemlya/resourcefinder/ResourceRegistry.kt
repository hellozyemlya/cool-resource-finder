package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.recipes.ResourceFinderCompassChargeRecipe
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.recipe.Recipe
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import kotlin.collections.ArrayList

class ResourceRegistry private constructor() {
    data class ChargeItem(val item: Item, val ticks: Int)

    @JvmRecord
    data class ResourceEntry(
        val index: Int,
        val color: Int,
        val targetBlocks: List<Block>,
        val rechargeItems: List<ChargeItem>,
        val displayItem: Item
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

        fun getChargeTicks(item: Item): Int {
            return rechargeItems.first { it.item == item }.ticks
        }
    }

    private val resourceEntryMap: MutableMap<Int, ResourceEntry> = HashMap()
    private fun addBlockGroup(
        idx: Int,
        color: Int,
        targetBlocks: List<Block>,
        rechargeItems: List<ChargeItem>,
        resource: Item
    ) {
        if (resourceEntryMap.containsKey(idx)) {
            throw IndexOutOfBoundsException()
        }
        resourceEntryMap[idx] = ResourceEntry(idx, color, targetBlocks, rechargeItems, resource)
    }

    fun getByIndex(idx: Int): ResourceEntry? {
        return resourceEntryMap[idx]
    }

    fun getByChargingItem(item: Item): Optional<ResourceEntry> {
        return resourceEntryMap.values.stream().filter { rechargeItems ->
            rechargeItems.rechargeItems.firstOrNull { it.item == item } != null
        }.findFirst()
    }

    fun getRechargeRecipes(): List<Recipe<*>> {
        val result = ArrayList<Recipe<*>>()

        resourceEntryMap.values.forEach { resourceEntry ->
            resourceEntry.rechargeItems.forEach { chargeItem ->
                result.add(
                    ResourceFinderCompassChargeRecipe(
                        Identifier(ResourceFinder.MOD_NAMESPACE, "compass_charge_${Registries.ITEM.getId(chargeItem.item).path}"),
                        chargeItem.item,
                        chargeItem.ticks,
                        resourceEntry
                    )
                )
            }
        }

        return result
    }

    init {
        addBlockGroup(
            0,
            0xff0000,
            listOf(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
            listOf(ChargeItem(Items.REDSTONE_BLOCK, 10800), ChargeItem(Items.REDSTONE, 1200)),
            Items.REDSTONE
        )
        addBlockGroup(
            1,
            0x1D969A,
            listOf(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
            listOf(ChargeItem(Items.DIAMOND_BLOCK, 10800), ChargeItem(Items.DIAMOND, 1200)),
            Items.DIAMOND
        )
        addBlockGroup(
            2,
            0x363636,
            listOf(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
            listOf(ChargeItem(Items.COAL_BLOCK, 10800), ChargeItem(Items.COAL, 1200), ChargeItem(Items.CHARCOAL, 1200)),
            Items.COAL
        )
    }

    companion object {
        val INSTANCE = ResourceRegistry()
    }
}
