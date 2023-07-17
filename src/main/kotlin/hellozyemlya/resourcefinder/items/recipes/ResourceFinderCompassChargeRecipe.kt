package hellozyemlya.resourcefinder.items.recipes

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import hellozyemlya.resourcefinder.items.ScanRecord
import hellozyemlya.resourcefinder.items.getScanList
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.ShapelessRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import java.util.stream.IntStream

class ResourceFinderCompassChargeRecipe(
    id: Identifier,
    private val chargeItem: Item,
    private val chargeValue: Int,
    private val resourceEntry: ResourceRegistry.ResourceEntry
) :
    ShapelessRecipe(
        id,
        "resource_finder_compass_charge",
        CraftingRecipeCategory.EQUIPMENT,
        ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack,
        DefaultedList.copyOf(
            null,
            Ingredient.ofItems(ResourceFinder.RESOURCE_FINDER_ITEM),
            Ingredient.ofItems(chargeItem)
        )
    ) {
    private val defaultOutput: ItemStack by lazy {
        val stack = ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
        stack.getScanList().add(ScanRecord(resourceEntry, chargeValue))
        stack
    }

    override fun getOutput(registryManager: DynamicRegistryManager?): ItemStack {
        return this.defaultOutput
    }

    override fun craft(
        craftingInventory: CraftingInventory?,
        dynamicRegistryManager: DynamicRegistryManager?
    ): ItemStack {
        if (craftingInventory == null) {
            return ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
        }
        val stackCandidate = IntStream
            .range(0, craftingInventory.size())
            .mapToObj { slot: Int -> craftingInventory.getStack(slot) }
            .filter { it.item == ResourceFinder.RESOURCE_FINDER_ITEM }
            .findFirst()

        if (stackCandidate.isEmpty) {
            return ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
        }

        val stackCopy = stackCandidate.get().copy()

        val scanList = stackCopy.getScanList()
        val existingEntry = stackCopy.getScanList().firstOrNull { it.resourceEntry.index == resourceEntry.index }
        if (existingEntry != null) {
            existingEntry.entryLifetime += chargeValue
        } else {
            scanList.add(ScanRecord(resourceEntry, chargeValue))
        }

        return stackCopy
    }
}
