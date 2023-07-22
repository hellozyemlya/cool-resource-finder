package hellozyemlya.resourcefinder.items.recipes

import com.google.common.collect.Streams
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.items.ScanRecord
import hellozyemlya.resourcefinder.items.getScanList
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.stream.Collectors

const val MAX_SCAN_CHARGES: Int = 5

class ResourceFinderChargeRecipeProxy {
    private fun getRecipeItems(inventory: Inventory): Pair<ItemStack, ArrayList<ItemStack>> {
        var compass: ItemStack? = null
        val charges = ArrayList<ItemStack>()

        for (i in 0 until inventory.size()) {
            val curStack = inventory.getStack(i)
            if (!curStack.isEmpty) {
                if (curStack.isOf(ResourceFinder.RESOURCE_FINDER_ITEM)) {
                    compass = curStack
                } else {
                    charges.add(curStack)
                }
            }
        }

        if (compass == null || charges.isEmpty()) {
            throw IllegalStateException()
        }

        return Pair(compass, charges)
    }

    fun matches(inventory: Inventory, world: World?): Boolean {
        var compassStack: ItemStack? = null
        val charges = ArrayList<ItemStack>()

        for (i in 0 until inventory.size()) {
            val curStack = inventory.getStack(i)
            if (!curStack.isEmpty) {
                if (curStack.isOf(ResourceFinder.RESOURCE_FINDER_ITEM)) {
                    compassStack = curStack
                } else {
                    if (!ResourceRegistry.INSTANCE.canBeChargedBy(curStack.item)) {
                        return false
                    }
                    charges.add(curStack)
                }
            }
        }

        if(compassStack != null && charges.size > 0) {
            val estimatedChargesCount =
                Streams.concat(
                    compassStack.getScanList().stream().map { it.key },
                    charges.stream().map { ResourceRegistry.INSTANCE.getByChargingItem(it.item).group }
                ).collect(Collectors.toSet()).size
            return estimatedChargesCount <= MAX_SCAN_CHARGES
        }

        return false
    }

    fun craft(inventory: Inventory, registryManager: DynamicRegistryManager?): ItemStack {
        val (compass, charges) = getRecipeItems(inventory)

        val result = compass.copy()

        val scanList = result.getScanList()

        charges.forEach { chargeStack ->
            val chargeItem = chargeStack.item
            val resourceEntry = ResourceRegistry.INSTANCE.getByChargingItem(chargeItem)

            val chargeValue = resourceEntry.getChargeTicks(chargeItem) * chargeStack.count
            val existingEntry = result.getScanList().firstOrNull { it.key == resourceEntry.group }
            if (existingEntry != null) {
                existingEntry.lifetime += chargeValue
            } else {
                scanList.add(ScanRecord(resourceEntry.group, resourceEntry.color, chargeValue))
            }
        }

        return result
    }

    fun getRemainder(inventory: Inventory): DefaultedList<ItemStack> {
        val defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY)
        for (i in defaultedList.indices) {
            inventory.setStack(i, ItemStack.EMPTY)
        }
        return defaultedList
    }
}