package hellozyemlya.compat.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeJsonProvider
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