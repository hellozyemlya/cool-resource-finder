package hellozyemlya.resourcefinder

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableTextContent

fun FabricLanguageProvider.TranslationBuilder.add(text: MutableText, value: String) {
    val content = text.content

    if (content is TranslatableTextContent) {
        this.add(content.key, value)
        return
    }

    throw IllegalArgumentException("'text' is not translatable")
}

class EngLangProvider(dataGenerator: FabricDataOutput) : FabricLanguageProvider(dataGenerator, "en_us") {
    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_ITEM, "Resource Scanner")
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_GROUP, "Resource Scanner")
        translationBuilder.add(ResourceFinderTexts.SCAN_FOR, "Finds")
        translationBuilder.add(ResourceFinderTexts.SCAN_JOIN, "for")
    }
}

class UaLangProvider(dataGenerator: FabricDataOutput) : FabricLanguageProvider(dataGenerator, "uk_ua") {
    override fun generateTranslations(translationBuilder: TranslationBuilder) {
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_ITEM, "Сканер Ресурсів")
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_GROUP, "Сканер Ресурсів")
        translationBuilder.add(ResourceFinderTexts.SCAN_FOR, "Знаходить")
        translationBuilder.add(ResourceFinderTexts.SCAN_JOIN, "впродовж")
    }
}

object ResourceFinderDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack: FabricDataGenerator.Pack = fabricDataGenerator.createPack()
        pack.addProvider(::EngLangProvider)
        pack.addProvider(::UaLangProvider)
    }
}