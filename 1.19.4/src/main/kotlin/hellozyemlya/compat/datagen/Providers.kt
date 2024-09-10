package hellozyemlya.compat.datagen

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import hellozyemlya.compat.recipes.CustomRecipe
import hellozyemlya.compat.recipes.ICompatCustomRecipe
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Consumer

fun createLangProvider(
    lang: String,
    apply: TranslationBuilder.() -> Unit
): (dataGenerator: FabricDataOutput) -> FabricLanguageProvider {
    return { dataGenerator: FabricDataOutput ->
        object : FabricLanguageProvider(dataGenerator, lang) {
            override fun generateTranslations(translationBuilder: TranslationBuilder) {
                translationBuilder.apply()
            }
        }
    }
}

fun createRecipeProvider(apply: Consumer<RecipeJsonProvider>.() -> Unit): (dataGenerator: FabricDataOutput) -> FabricRecipeProvider {
    return { dataGenerator: FabricDataOutput ->
        object : FabricRecipeProvider(dataGenerator) {
            override fun generate(exporter: Consumer<RecipeJsonProvider>) {
                exporter.apply()
            }
        }
    }
}

fun <T : ICompatCustomRecipe> Consumer<RecipeJsonProvider>.provideCustomRecipe(
    id: Identifier,
    category: CraftingRecipeCategory,
    serializer: RecipeSerializer<CustomRecipe<T>>
) {
    val recipeProvider = object : RecipeJsonProvider {
        override fun serialize(json: JsonObject) {
            json.add("category", JsonPrimitive(category.asString()))
            json.add("type", JsonPrimitive(Registries.RECIPE_SERIALIZER.getId(serializer).toString()))
        }

        override fun getRecipeId(): Identifier {
            return id
        }

        override fun getSerializer(): RecipeSerializer<*> {
            return serializer
        }

        override fun toAdvancementJson(): JsonObject? {
            return null
        }

        override fun getAdvancementId(): Identifier? {
            return null
        }
    }
    this.accept(recipeProvider)
}