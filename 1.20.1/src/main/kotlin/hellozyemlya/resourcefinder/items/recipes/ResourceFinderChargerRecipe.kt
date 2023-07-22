package hellozyemlya.resourcefinder.items.recipes

import hellozyemlya.resourcefinder.ResourceFinder
import net.minecraft.inventory.RecipeInputInventory
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

    override fun matches(inventory: RecipeInputInventory, world: World?): Boolean {
        return proxy.matches(inventory, world)
    }

    override fun craft(inventory: RecipeInputInventory, registryManager: DynamicRegistryManager?): ItemStack {
        return proxy.craft(inventory, registryManager)
    }

    override fun getRemainder(inventory: RecipeInputInventory): DefaultedList<ItemStack> {
        return proxy.getRemainder(inventory)
    }

    override fun fits(width: Int, height: Int): Boolean {
        return width * height >= 2
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return ResourceFinder.RESOURCE_FINDER_REPAIR_SERIALIZER
    }
}