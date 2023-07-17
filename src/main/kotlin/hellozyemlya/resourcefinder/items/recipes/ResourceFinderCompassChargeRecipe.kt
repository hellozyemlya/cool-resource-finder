package hellozyemlya.resourcefinder.items.recipes

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import hellozyemlya.resourcefinder.items.ResourceFinderCompass
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

class ResourceFinderCompassChargeRecipe(id: Identifier, private val chargeItem: Item, private val chargeValue: Int, private val resourceEntry: ResourceRegistry.ResourceEntry) :
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

    override fun getOutput(registryManager: DynamicRegistryManager?): ItemStack {
        val stackCopy = ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
        val scanNbt = ResourceFinderCompass.ScanNbt(stackCopy)
        val scanEntry = scanNbt.createOrGetEntryFor(resourceEntry)
        scanEntry.lifetime += chargeValue
        scanNbt.write(stackCopy)
        return stackCopy
    }

    override fun craft(
        craftingInventory: CraftingInventory?,
        dynamicRegistryManager: DynamicRegistryManager?
    ): ItemStack {
        if(craftingInventory == null) {
            return ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
        }
        val stackCandidate = IntStream
            .range(0, craftingInventory.size())
            .mapToObj { slot: Int -> craftingInventory.getStack(slot) }
            .filter { it.item == ResourceFinder.RESOURCE_FINDER_ITEM}
            .findFirst()
        if(stackCandidate.isEmpty) {
            return ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
        }

        val stackCopy = stackCandidate.get().copy()
        val scanNbt = ResourceFinderCompass.ScanNbt(stackCopy)
        val scanEntry = scanNbt.createOrGetEntryFor(resourceEntry)
        scanEntry.lifetime += chargeValue
        scanNbt.write(stackCopy)
        return stackCopy
    }
//    override fun matches(inventory: CraftingInventory, world: World): Boolean {
//        val allStacks = IntStream.range(0, inventory.size())
//            .mapToObj { slot: Int -> inventory.getStack(slot) }
//            .filter { itemStack: ItemStack -> !itemStack.isEmpty }.toList()
//        val compassStack = allStacks.stream()
//            .filter { itemStack: ItemStack -> itemStack.item === ResourceFinder.RESOURCE_FINDER_ITEM }
//            .findFirst()
//        if (compassStack.isEmpty) return false
//        val otherStack = allStacks.stream()
//            .filter { itemStack: ItemStack -> itemStack.item !== ResourceFinder.RESOURCE_FINDER_ITEM }
//            .findFirst()
//        if (otherStack.isEmpty) return false
//        val resourceEntry: Optional<ResourceRegistry.ResourceEntry> =
//            ResourceRegistry.INSTANCE.getByChargingItem(otherStack.get().item)
//        return !resourceEntry.isEmpty
//    }
//
//    override fun craft(inventory: CraftingInventory, registryManager: DynamicRegistryManager): ItemStack {
//        val allStacks = IntStream.range(0, inventory.size())
//            .mapToObj { slot: Int -> inventory.getStack(slot) }
//            .filter { itemStack: ItemStack -> !itemStack.isEmpty }.toList()
//        val compassStack = allStacks.stream()
//            .filter { itemStack: ItemStack -> itemStack.item === ResourceFinder.RESOURCE_FINDER_ITEM }
//            .findFirst()
//        if (compassStack.isEmpty) return ItemStack.EMPTY
//        val otherStack = allStacks.stream()
//            .filter { itemStack: ItemStack -> itemStack.item !== ResourceFinder.RESOURCE_FINDER_ITEM }
//            .findFirst()
//        if (otherStack.isEmpty) return ItemStack.EMPTY
//        val resourceEntry: Optional<ResourceRegistry.ResourceEntry> =
//            ResourceRegistry.INSTANCE.getByChargingItem(otherStack.get().item)
//        if (resourceEntry.isEmpty) return ItemStack.EMPTY
//        val newStack = compassStack.get().copy()
//
//        val scanNbt = ResourceFinderCompass.ScanNbt(newStack)
//        val scanEntry = scanNbt.createOrGetEntryFor(resourceEntry.get())
//        scanEntry.lifetime += resourceEntry.get().getChargeTicks(otherStack.get().item)
//        scanNbt.write(newStack)
//
//        return newStack
//    }
//
//    override fun fits(width: Int, height: Int): Boolean {
//        return true
//    }
//
//    override fun getOutput(registryManager: DynamicRegistryManager): ItemStack {
//        return ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
//    }
//
//    override fun getId(): Identifier {
//        return id
//    }
}
