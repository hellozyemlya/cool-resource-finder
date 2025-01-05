package hellozyemlya.compat.recipes

import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialCraftingRecipe
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.input.CraftingRecipeInput
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import net.minecraft.world.World

class CustomRecipe<T : ICompatCustomRecipe>(
    private val recipe: T?,
    private val serializer: () -> RecipeSerializer<CustomRecipe<T>>,
    category: CraftingRecipeCategory
) : SpecialCraftingRecipe(category) {
    override fun matches(input: CraftingRecipeInput, world: World?): Boolean {
        return recipe?.matches(input.stacks) ?: false
    }

    override fun craft(input: CraftingRecipeInput, lookup: RegistryWrapper.WrapperLookup?): ItemStack {
        return recipe?.craft(input.stacks) ?: ItemStack.EMPTY
    }

    override fun fits(width: Int, height: Int): Boolean {
        return recipe?.fits(width, height) ?: false
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
        SpecialRecipeSerializer { category ->
            CustomRecipe(
                recipe,
                { ref[0]!! },
                category
            )
        })
    return ref[0]!!
}