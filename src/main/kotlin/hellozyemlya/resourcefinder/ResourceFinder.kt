package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.ResourceFinderCompass
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderChargeRecipe
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import hellozyemlya.resourcefinder.registry.config.Config
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.primaryConstructor

object ResourceFinder : ModInitializer {
    public val LOGGER: Logger = LoggerFactory.getLogger("cool-resource-finder")
    public const val MOD_NAMESPACE = "cool-resource-finder"

    val RESOURCE_FINDER_ITEM: ResourceFinderCompass = Registry.register(
        Registries.ITEM, Identifier(MOD_NAMESPACE, "resource_finder_compass"),
        ResourceFinderCompass(FabricItemSettings().maxCount(1))
    )

    val RESOURCE_FINDER_ARROW_ITEM: Item = Registry.register(
        Registries.ITEM, Identifier(MOD_NAMESPACE, "resource_finder_compass_arrow"),
        Item(FabricItemSettings())
    )

    val RESOURCE_FINDER_BASE_ITEM: Item = Registry.register(
        Registries.ITEM, Identifier(MOD_NAMESPACE, "resource_finder_compass_base"),
        Item(FabricItemSettings())
    )

    val RESOURCE_FINDER_INDICATOR_ITEM: Item = Registry.register(
        Registries.ITEM, Identifier(MOD_NAMESPACE, "resource_finder_compass_indicator"),
        Item(FabricItemSettings())
    )

    public val RESOURCE_FINDER_GROUP: ItemGroup = FabricItemGroup.builder(Identifier(MOD_NAMESPACE, "resource_finder"))
        .icon { ItemStack(RESOURCE_FINDER_ITEM) }
		.displayName(Text.translatable("itemGroup.$MOD_NAMESPACE.resource_finder_group"))
        .build()


    final val RESOURCE_FINDER_REPAIR_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER, Identifier(MOD_NAMESPACE, "crafting_special_resource_finder_charge"), SpecialRecipeSerializer { id, category ->
        ResourceFinderChargeRecipe(
            id,
            category
        )
    })

    override fun onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_GROUP)
			.register { content -> content.add(RESOURCE_FINDER_ITEM) }
//        Registry.register(
//            Registries.RECIPE_SERIALIZER, ResourceFinderCompassChargeRecipe.Serializer.ID,
//            ResourceFinderCompassChargeRecipe.Serializer.INSTANCE
//        )

        LOGGER.info("Hello Fabric world!")
    }
}