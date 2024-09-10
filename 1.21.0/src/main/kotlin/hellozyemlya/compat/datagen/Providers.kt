package hellozyemlya.compat.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

fun createLangProvider(
    lang: String,
    apply: TranslationBuilder.() -> Unit
): (dataGenerator: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) -> FabricLanguageProvider {
    return { dataGenerator: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup> ->
        object : FabricLanguageProvider(dataGenerator, lang, registryLookup) {
            override fun generateTranslations(
                registryLookup: RegistryWrapper.WrapperLookup?,
                translationBuilder: TranslationBuilder
            ) {
                translationBuilder.apply()
            }
        }
    }
}

fun createRecipeProvider(apply: RecipeExporter.() -> Unit): (dataGenerator: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) -> FabricRecipeProvider {
    return { dataGenerator: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup> ->
        object : FabricRecipeProvider(dataGenerator, registryLookup) {
            override fun generate(exporter: RecipeExporter) {
                exporter.apply()
            }
        }
    }
}