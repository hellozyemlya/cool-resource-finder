package hellozyemlya.resourcefinder.datagen

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceFinderTexts
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.registry.RegistryWrapper
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableTextContent
import java.util.concurrent.CompletableFuture


fun FabricLanguageProvider.TranslationBuilder.add(text: MutableText, value: String) {
    val content = text.content

    if (content is TranslatableTextContent) {
        this.add(content.key, value)
        return
    }

    throw IllegalArgumentException("'text' is not translatable")
}

class EngLangProvider(dataOutput: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) : FabricLanguageProvider(dataOutput, "en_us", registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup?,
        translationBuilder: TranslationBuilder
    ) {
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_ITEM, "Resource Scanner")
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_ITEM_GROUP, "Resource Scanner")
        translationBuilder.add(ResourceFinderTexts.SCAN_FOR, "Finds")
        translationBuilder.add(ResourceFinderTexts.SCAN_JOIN, "for")
    }
}

class UaLangProvider(dataOutput: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) : FabricLanguageProvider(dataOutput, "uk_ua", registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup?,
        translationBuilder: TranslationBuilder
    ) {
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_ITEM, "Сканер Ресурсів")
        translationBuilder.add(ResourceFinder.RESOURCE_FINDER_ITEM_GROUP, "Сканер Ресурсів")
        translationBuilder.add(ResourceFinderTexts.SCAN_FOR, "Знаходить")
        translationBuilder.add(ResourceFinderTexts.SCAN_JOIN, "впродовж")
    }
}