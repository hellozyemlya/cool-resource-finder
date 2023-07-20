package hellozyemlya.resourcefinder.registry

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items

class ResourceRegistry private constructor() {
    private val groupsMap: MutableMap<Item, ResourceEntry> = HashMap()

    private val chargeItemToEntry: Int2ObjectOpenHashMap<ResourceEntry> by lazy {
        val result = Int2ObjectOpenHashMap<ResourceEntry>()
        groupsMap.values.forEach { resourceEntry ->
            resourceEntry.rechargeItems.forEach { chargeEntry ->
                result[Item.getRawId(chargeEntry.item)] = resourceEntry
            }
        }
        result
    }

    public val groups: Collection<ResourceEntry>
        get() = groupsMap.values

    init {
        addGroup(
            Items.REDSTONE,
            0xff0000,
            listOf(Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
            listOf(ChargeItem(Items.REDSTONE_BLOCK, 10800), ChargeItem(Items.REDSTONE, 1200))
        )
        addGroup(
            Items.DIAMOND,
            0x1D969A,
            listOf(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
            listOf(ChargeItem(Items.DIAMOND_BLOCK, 10800), ChargeItem(Items.DIAMOND, 1200))
        )
        addGroup(
            Items.COAL,
            0x363636,
            listOf(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
            listOf(ChargeItem(Items.COAL_BLOCK, 10800), ChargeItem(Items.COAL, 1200), ChargeItem(Items.CHARCOAL, 1200))
        )
    }
    private fun addGroup(
        groupItem: Item,
        color: Int,
        targetBlocks: List<Block>,
        rechargeItems: List<ChargeItem>
    ) {
        groupsMap[groupItem] = ResourceEntry(groupItem, color, targetBlocks, rechargeItems)
    }

    fun getByChargingItem(item: Item): ResourceEntry {
        return chargeItemToEntry[Item.getRawId(item)]
    }

    fun canBeChargedBy(item: Item): Boolean {
        return chargeItemToEntry.containsKey(Item.getRawId(item))
    }

    fun getByGroup(item: Item): ResourceEntry {
        return groupsMap[item]!!
    }

    companion object {
        val INSTANCE = ResourceRegistry()
    }
}
