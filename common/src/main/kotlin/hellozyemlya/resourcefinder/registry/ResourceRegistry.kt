package hellozyemlya.resourcefinder.registry

import hellozyemlya.resourcefinder.registry.config.Config
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.Items
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val LOGGER: Logger = LoggerFactory.getLogger("cool-resource-finder")

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

    constructor(entries: List<ResourceEntry>) : this() {
        entries.forEach {
            groupsMap[it.group] = it
        }
    }


    public val groups: Collection<ResourceEntry>
        get() = groupsMap.values

    init {

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
        private val DEFAULT_REGISTRY: ResourceRegistry = ResourceRegistry().apply {
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
                listOf(
                    ChargeItem(Items.COAL_BLOCK, 10800),
                    ChargeItem(Items.COAL, 1200),
                    ChargeItem(Items.CHARCOAL, 1200)
                )
            )
            addGroup(
                Items.IRON_INGOT,
                0xD8D8D8,
                listOf(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK),
                listOf(
                    ChargeItem(Items.IRON_BLOCK, 10800),
                    ChargeItem(Items.IRON_INGOT, 1200),
                    ChargeItem(Items.RAW_IRON, 1200),
                    ChargeItem(Items.IRON_NUGGET, 133)
                )
            )
            addGroup(
                Items.EMERALD,
                0x17DD62,
                listOf(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
                listOf(
                    ChargeItem(Items.EMERALD_BLOCK, 10800),
                    ChargeItem(Items.EMERALD, 1200)
                )
            )
            addGroup(
                Items.COPPER_INGOT,
                0xC15A36,
                listOf(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
                listOf(
                    ChargeItem(Items.COPPER_BLOCK, 10800),
                    ChargeItem(Items.COPPER_INGOT, 1200)
                )
            )
            addGroup(
                Items.GOLD_INGOT,
                0xFDF55F,
                listOf(Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.RAW_GOLD_BLOCK, Blocks.NETHER_GOLD_ORE),
                listOf(
                    ChargeItem(Items.GOLD_BLOCK, 10800),
                    ChargeItem(Items.GOLD_INGOT, 1200),
                    ChargeItem(Items.RAW_GOLD, 1200),
                    ChargeItem(Items.GOLD_NUGGET, 133)
                )
            )
            addGroup(
                Items.NETHERITE_SCRAP,
                0x5D342C,
                listOf(Blocks.ANCIENT_DEBRIS),
                listOf(
                    ChargeItem(Items.NETHERITE_BLOCK, 10800),
                    ChargeItem(Items.NETHERITE_INGOT, 1200),
                    ChargeItem(Items.NETHERITE_SCRAP, 300)
                )
            )
            addGroup(
                Items.LAPIS_LAZULI,
                0x345EC3,
                listOf(Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
                listOf(
                    ChargeItem(Items.LAPIS_BLOCK, 10800),
                    ChargeItem(Items.LAPIS_LAZULI, 1200)
                )
            )
            addGroup(
                Items.QUARTZ,
                0xB6A48E,
                listOf(Blocks.NETHER_QUARTZ_ORE),
                listOf(
                    ChargeItem(Items.QUARTZ_BLOCK, 10800),
                    ChargeItem(Items.QUARTZ, 2700)
                )
            )
        }

        val INSTANCE: ResourceRegistry by lazy {
            val config = Config.load("cool-resource-finder-registry", DEFAULT_REGISTRY)
            LOGGER.info("'Cool Resource Finder' scans for ${config.groups.count()} resource groups.")
            config
        }
    }
}
