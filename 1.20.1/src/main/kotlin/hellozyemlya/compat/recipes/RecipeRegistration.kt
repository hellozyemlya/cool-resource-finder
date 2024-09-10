package hellozyemlya.compat.recipes

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import net.minecraft.inventory.RecipeInputInventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.world.World

class CustomRecipe<T>(
    private val recipe: ICompatCustomRecipe,
    private val serializer: () -> RecipeSerializer<CustomRecipe<T>>,
    id: Identifier,
    category: CraftingRecipeCategory
) : SpecialCraftingRecipe(id, category) {
    override fun matches(inventory: RecipeInputInventory, world: World): Boolean {
        return recipe.matches(inventory.inputStacks)
    }

    override fun craft(inventory: RecipeInputInventory, registryManager: DynamicRegistryManager?): ItemStack {
        return recipe.craft(inventory.inputStacks)
    }

    override fun fits(width: Int, height: Int): Boolean {
        return recipe.fits(width, height)
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return serializer()
    }

}

fun <T : ICompatCustomRecipe> registerCustomRecipe(id: Identifier, recipe: T): RecipeSerializer<CustomRecipe<T>> {
    val ref = Array<RecipeSerializer<CustomRecipe<T>>?>(1) { null }
    ref[0] = Registry.register(
        Registries.RECIPE_SERIALIZER,
        id,
        SpecialRecipeSerializer { _id, category ->
            CustomRecipe(
                recipe,
                { ref[0]!! },
                _id,
                category
            )
        })
    return ref[0]!!
}