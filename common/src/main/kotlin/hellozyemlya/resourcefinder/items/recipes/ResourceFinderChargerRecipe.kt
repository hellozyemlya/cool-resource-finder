package hellozyemlya.resourcefinder.items.recipes

import com.google.common.collect.Streams
import hellozyemlya.mccompat.RecipeInputInventoryAlias
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceFinder.RESOURCE_FINDER_ITEM
import hellozyemlya.resourcefinder.items.compass.CompassScanItem
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.stream.Collectors

const val MAX_SCAN_CHARGES: Int = 5

class ResourceFinderChargeRecipe(id: Identifier, category: CraftingRecipeCategory) : SpecialCraftingRecipe(
    id,
    category
) {
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

    override fun matches(inventory: RecipeInputInventoryAlias, world: World?): Boolean {
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

        if (compassStack != null && charges.size > 0) {

            val estimatedChargesCount =
                Streams.concat(
                    RESOURCE_FINDER_ITEM.getServerCompassData(compassStack).second.scanList.entries.stream().map { it.key },
                    charges.stream().map { ResourceRegistry.INSTANCE.getByChargingItem(it.item).group }
                ).collect(Collectors.toSet()).size
            return estimatedChargesCount <= MAX_SCAN_CHARGES
        }

        return false
    }

    override fun craft(inventory: RecipeInputInventoryAlias, registryManager: DynamicRegistryManager?): ItemStack {
        val (compass, charges) = getRecipeItems(inventory)

        val result = compass.copy()
        val scanList = RESOURCE_FINDER_ITEM.getServerCompassData(result).second.scanList

        charges.forEach { chargeStack ->
            val chargeItem = chargeStack.item
            val resourceEntry = ResourceRegistry.INSTANCE.getByChargingItem(chargeItem)

            val chargeValue = resourceEntry.getChargeTicks(chargeItem) * chargeStack.count
            val existingEntry = scanList.entries.firstOrNull { it.key == resourceEntry.group }
            if (existingEntry != null) {
                existingEntry.value.lifetimeTicks += chargeValue
            } else {
                scanList[resourceEntry.group] = CompassScanItem(chargeValue, color = resourceEntry.color)
            }
        }

        return result
    }

    override fun getRemainder(inventory: RecipeInputInventoryAlias): DefaultedList<ItemStack> {
        val defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY)
        for (i in defaultedList.indices) {
            inventory.setStack(i, ItemStack.EMPTY)
        }
        return defaultedList
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return ResourceFinder.RESOURCE_FINDER_REPAIR_SERIALIZER
    }
}