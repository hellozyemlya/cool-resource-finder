package hellozyemlya.resourcefinder

import hellozyemlya.compat.datagen.createLangProvider
import hellozyemlya.compat.datagen.createRecipeProvider
import hellozyemlya.compat.datagen.provideCustomRecipe
import hellozyemlya.compat.recipes.CustomRecipe
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderChargeRecipe
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.data.server.recipe.RecipeProvider.conditionsFromItem
import net.minecraft.data.server.recipe.RecipeProvider.hasItem
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Identifier

fun FabricLanguageProvider.TranslationBuilder.add(text: MutableText, value: String) {
    val content = text.content

    if (content is TranslatableTextContent) {
        this.add(content.key, value)
        return
    }

    throw IllegalArgumentException("'text' is not translatable")
}

@Suppress("unused")
object ResourceFinderDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack: FabricDataGenerator.Pack = fabricDataGenerator.createPack()
        pack.addProvider(createLangProvider("en_us") {
            add(ResourceFinder.RESOURCE_FINDER_ITEM, "Resource Scanner")
            add(ResourceFinder.RESOURCE_FINDER_ITEM_GROUP, "Resource Scanner")
            add(ResourceFinderTexts.SCAN_FOR, "Finds")
            add(ResourceFinderTexts.SCAN_JOIN, "for")
        })
        pack.addProvider(createLangProvider("uk_ua") {
            add(ResourceFinder.RESOURCE_FINDER_ITEM, "Сканер Ресурсів")
            add(ResourceFinder.RESOURCE_FINDER_ITEM_GROUP, "Сканер Ресурсів")
            add(ResourceFinderTexts.SCAN_FOR, "Знаходить")
            add(ResourceFinderTexts.SCAN_JOIN, "впродовж")
        })
        pack.addProvider(createRecipeProvider {
            ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ResourceFinder.RESOURCE_FINDER_ITEM, 1)
                .pattern(" C ")
                .pattern("C#C")
                .pattern(" C ")
                .input('C', Items.COMPARATOR)
                .input('#', Items.COMPASS)
                .criterion(hasItem(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR))
                .criterion(hasItem(Items.COMPASS), conditionsFromItem(Items.COMPASS))
                .offerTo(this, Identifier.of(MOD_NAMESPACE, "resource_finder_compass_recipe"))
            this.provideCustomRecipe(
                Identifier.of(MOD_NAMESPACE, "resource_finder_charge_recipe")!!,
                CraftingRecipeCategory.MISC,
                ResourceFinder.RECHARGE_RECIPE_SERIALIZER
            )
        })
    }
}