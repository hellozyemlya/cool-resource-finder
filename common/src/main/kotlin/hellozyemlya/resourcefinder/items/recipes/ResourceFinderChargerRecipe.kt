package hellozyemlya.resourcefinder.items.recipes

import com.google.common.collect.Streams
import hellozyemlya.compat.compatGetOrDefault
import hellozyemlya.compat.compatSet
import hellozyemlya.compat.recipes.ICompatCustomRecipe
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.items.CompassComponents
import hellozyemlya.resourcefinder.items.ScanTarget
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.collection.DefaultedList
import java.util.stream.Collectors

const val MAX_SCAN_CHARGES: Int = 5

class ResourceFinderChargeRecipe : ICompatCustomRecipe {
    private fun getRecipeItems(inventory: List<ItemStack>): Pair<ItemStack, ArrayList<ItemStack>> {
        var compass: ItemStack? = null
        val charges = ArrayList<ItemStack>()

        for (element in inventory) {
            if (!element.isEmpty) {
                if (element.isOf(ResourceFinder.RESOURCE_FINDER_ITEM)) {
                    compass = element
                } else {
                    charges.add(element)
                }
            }
        }

        if (compass == null || charges.isEmpty()) {
            throw IllegalStateException()
        }

        return Pair(compass, charges)
    }

    override fun matches(inputStacks: List<ItemStack>): Boolean {
        var compassStack: ItemStack? = null
        val charges = ArrayList<ItemStack>()

        for (element in inputStacks) {
            if (!element.isEmpty) {
                if (element.isOf(ResourceFinder.RESOURCE_FINDER_ITEM)) {
                    compassStack = element
                } else {
                    if (!ResourceRegistry.INSTANCE.canBeChargedBy(element.item)) {
                        return false
                    }
                    charges.add(element)
                }
            }
        }



        if (compassStack != null && charges.size > 0) {
            val scanListEntries =
                compassStack.compatGetOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, mapOf()).entries
            val estimatedChargesCount =
                Streams.concat(
                    scanListEntries.stream().map { it.key },
                    charges.stream()
                        .map { Registries.ITEM.getId(ResourceRegistry.INSTANCE.getByChargingItem(it.item).group) }
                ).collect(Collectors.toSet()).size
            return estimatedChargesCount <= MAX_SCAN_CHARGES
        }

        return false
    }

    override fun craft(inputStacks: List<ItemStack>): ItemStack {
        val (compass, charges) = getRecipeItems(inputStacks)

        val result = compass.copy()
        val scanList = result.compatGetOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, mapOf()).toMutableMap()
        charges.forEach { chargeStack ->
            val chargeItem = chargeStack.item
            val resourceEntry = ResourceRegistry.INSTANCE.getByChargingItem(chargeItem)
            val groupId = Registries.ITEM.getId(resourceEntry.group)
            val chargeValue = resourceEntry.getChargeTicks(chargeItem) * chargeStack.count
            val existingEntry = scanList[groupId]
            scanList[groupId] = ScanTarget(chargeValue + (existingEntry?.lifetimeTicks ?: 0), resourceEntry.color)
        }
        result.compatSet(CompassComponents.SCAN_TARGETS_COMPONENT, scanList)
        return result
    }

    override fun getRemainder(inputStacks: List<ItemStack>): DefaultedList<ItemStack> {
        return DefaultedList.ofSize(inputStacks.size, ItemStack.EMPTY)
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }
}