package hellozyemlya.resourcefinder.items.recipes

import hellozyemlya.resourcefinder.ResourceFinder
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
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
    private val proxy = ResourceFinderChargeRecipeProxy()

    override fun matches(inventory: CraftingInventory, world: World?): Boolean {
        return proxy.matches(inventory, world)
    }

    override fun craft(inventory: CraftingInventory, registryManager: DynamicRegistryManager?): ItemStack {
        return proxy.craft(inventory, registryManager)
    }

    override fun getRemainder(inventory: CraftingInventory): DefaultedList<ItemStack> {
        return proxy.getRemainder(inventory)
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return ResourceFinder.RESOURCE_FINDER_REPAIR_SERIALIZER
    }
}