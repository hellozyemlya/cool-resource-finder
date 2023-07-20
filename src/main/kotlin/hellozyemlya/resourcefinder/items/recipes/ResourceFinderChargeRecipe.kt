package hellozyemlya.resourcefinder.items.recipes

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.items.ScanRecord
import hellozyemlya.resourcefinder.items.getScanList
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.lang.IllegalStateException

class ResourceFinderChargeRecipe(id: Identifier, category: CraftingRecipeCategory) : SpecialCraftingRecipe(
    id,
    category
) {
    private fun getRecipeItems(inventory: RecipeInputInventory): Pair<ItemStack, ArrayList<ItemStack>> {
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

        if(compass == null || charges.isEmpty()) {
            throw IllegalStateException()
        }

        return Pair(compass, charges)
    }

    override fun matches(inventory: RecipeInputInventory, world: World?): Boolean {
        var hasCompass = false
        var hasOtherResources = false

        for (i in 0 until inventory.size()) {
            val curStack = inventory.getStack(i)
            if (!curStack.isEmpty) {
                if (curStack.isOf(ResourceFinder.RESOURCE_FINDER_ITEM)) {
                    hasCompass = true
                } else {
                    if(!ResourceRegistry.INSTANCE.canBeChargedBy(curStack.item)) {
                        return false
                    }
                    hasOtherResources = true
                }
            }
        }

        return hasCompass && hasOtherResources
    }

    override fun craft(inventory: RecipeInputInventory, registryManager: DynamicRegistryManager?): ItemStack {
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

    override fun getRemainder(inventory: RecipeInputInventory): DefaultedList<ItemStack> {
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