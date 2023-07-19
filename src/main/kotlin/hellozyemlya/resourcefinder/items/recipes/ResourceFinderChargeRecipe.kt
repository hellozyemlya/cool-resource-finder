package hellozyemlya.resourcefinder.items.recipes

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import hellozyemlya.resourcefinder.items.ScanRecord
import hellozyemlya.resourcefinder.items.getScanList
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World

class ResourceFinderChargeRecipe(id: Identifier, category: CraftingRecipeCategory) : SpecialCraftingRecipe(
    id,
    category
) {
    fun extractItems(inventory: CraftingInventory): Pair<ItemStack?, ArrayList<ItemStack>> {
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

        return Pair(compass, charges)
    }

    fun requireItems(inventory: CraftingInventory): Pair<ItemStack, ArrayList<ItemStack>> {
        val result = extractItems(inventory)
        if (result.first == null) {
            throw IllegalStateException()
        }

        return result as Pair<ItemStack, ArrayList<ItemStack>>
    }

    override fun matches(inventory: CraftingInventory, world: World?): Boolean {
        val (compass, charges) = extractItems(inventory)

        if (compass == null)
            return false


        charges.forEach { chargeCandidate ->
            if(!ResourceRegistry.INSTANCE.itemCanCharge(chargeCandidate.item)) {
                return false
            }
        }

        return true
    }

    override fun craft(inventory: CraftingInventory, registryManager: DynamicRegistryManager?): ItemStack {
        val (compass, charges) = requireItems(inventory)

        val result = compass.copy()

        val scanList = result.getScanList()

        charges.forEach { chargeStack ->
            val chargeItem = chargeStack.item
            val resourceEntry = ResourceRegistry.INSTANCE.getByChargingItem(chargeItem).get()
            val chargeValue = resourceEntry.getChargeTicks(chargeItem) * chargeStack.count
            val existingEntry = result.getScanList().firstOrNull { it.resourceEntry.index == resourceEntry.index }
            if (existingEntry != null) {
                existingEntry.entryLifetime += chargeValue
            } else {
                scanList.add(ScanRecord(resourceEntry, chargeValue))
            }
        }

        return result
    }

    override fun getRemainder(inventory: CraftingInventory): DefaultedList<ItemStack> {
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