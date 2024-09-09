package hellozyemlya.resourcefinder.items.recipes

import com.google.common.collect.Streams
import hellozyemlya.mccompat.CompassComponents
import hellozyemlya.mccompat.RecipeInputInventoryAlias
import hellozyemlya.mccompat.ScanTarget
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

const val MAX_SCAN_CHARGES: Int = 5

class ResourceFinderChargeRecipe(category: CraftingRecipeCategory) : SpecialCraftingRecipe(
        category
) {
    private fun getRecipeItems(inventory: CraftingRecipeInput): Pair<ItemStack, ArrayList<ItemStack>> {
        var compass: ItemStack? = null
        val charges = ArrayList<ItemStack>()

        for (i in 0 until inventory.stackCount) {
            val curStack = inventory.stacks[i]
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

    override fun matches(input: RecipeInputInventoryAlias, world: World?): Boolean {
        var compassStack: ItemStack? = null
        val charges = ArrayList<ItemStack>()
        for (i in 0 until input.stackCount) {
            val curStack = input.stacks[i]
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
                        compassStack.getOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, emptyMap()).entries.stream().map { it.key },
                            charges.stream().map { ResourceRegistry.INSTANCE.getByChargingItem(it.item).group }
                    ).collect(Collectors.toSet()).size
            return estimatedChargesCount <= MAX_SCAN_CHARGES
        }

        return false
    }

    override fun craft(inventory: CraftingRecipeInput, lookup: RegistryWrapper.WrapperLookup): ItemStack? {
        val (compass, charges) = getRecipeItems(inventory)

        val result = compass.copy()

        val oldScanList = result.getOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, emptyMap())
        val newScanList = mutableMapOf<Identifier, ScanTarget>()
        charges.forEach { chargeStack ->
            val chargeItem = chargeStack.item
            val resourceEntry = ResourceRegistry.INSTANCE.getByChargingItem(chargeItem)

            var chargeValue = resourceEntry.getChargeTicks(chargeItem) * chargeStack.count
            val existingEntry = oldScanList.entries.firstOrNull { Registries.ITEM.get(it.key) == resourceEntry.group }
            if (existingEntry != null) {
                chargeValue += existingEntry.value.lifetimeTicks
            }
            newScanList.put(Registries.ITEM.getId(resourceEntry.group), ScanTarget(chargeValue, resourceEntry.color, Optional.empty()))
        }
        result.set(CompassComponents.SCAN_TARGETS_COMPONENT, newScanList)
        return result
    }

    override fun getRemainder(inventory: RecipeInputInventoryAlias): DefaultedList<ItemStack> {
        return DefaultedList.ofSize(inventory.size, ItemStack.EMPTY)
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return ResourceFinder.RESOURCE_FINDER_REPAIR_SERIALIZER
    }
}