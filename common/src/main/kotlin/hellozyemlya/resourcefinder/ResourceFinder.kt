package hellozyemlya.resourcefinder

import hellozyemlya.mccompat.CompassComponents
import hellozyemlya.mccompat.ItemGroupKeyAlias
import hellozyemlya.mccompat.createItemGroup
import hellozyemlya.resourcefinder.items.ResourceFinderCompass
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderChargeRecipe
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ResourceFinder : ModInitializer {
    val RESOURCE_FINDER_ITEM: ResourceFinderCompass = Registry.register(
        Registries.ITEM, Identifier.of(MOD_NAMESPACE, "resource_finder_compass"),
        ResourceFinderCompass(Item.Settings().maxCount(1))
    )

    val RESOURCE_FINDER_ITEM_GROUP: ItemGroupKeyAlias = createItemGroup(
        RESOURCE_FINDER_ITEM.defaultStack,
        "resource_finder",
        "itemGroup.$MOD_NAMESPACE.resource_finder_group"
    )

    val RESOURCE_FINDER_REPAIR_SERIALIZER: RecipeSerializer<ResourceFinderChargeRecipe> = Registry.register(
        Registries.RECIPE_SERIALIZER,
        Identifier.of(MOD_NAMESPACE, "crafting_special_resource_finder_charge"),
        SpecialRecipeSerializer { craftingCategory ->
            ResourceFinderChargeRecipe(
                craftingCategory
            )
        })

    override fun onInitialize() {
        println(CompassComponents.SCAN_TARGETS_COMPONENT)
        ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_ITEM_GROUP).register {
            it.add(RESOURCE_FINDER_ITEM)
        }
    }
}