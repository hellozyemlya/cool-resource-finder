package hellozyemlya.resourcefinder

import hellozyemlya.compat.items.createItemSettings
import hellozyemlya.compat.recipes.CustomRecipe
import hellozyemlya.compat.recipes.registerCustomRecipe
import hellozyemlya.mccompat.ItemGroupKeyAlias
import hellozyemlya.mccompat.createItemGroup
import hellozyemlya.resourcefinder.items.CompassComponents
import hellozyemlya.resourcefinder.items.ResourceFinderCompass
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderChargeRecipe
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ResourceFinder : ModInitializer {
    val RESOURCE_FINDER_ITEM: ResourceFinderCompass = Registry.register(
        Registries.ITEM, Identifier.of(MOD_NAMESPACE, "resource_finder_compass"),
        ResourceFinderCompass(createItemSettings(1))
    )

    val RESOURCE_FINDER_ITEM_GROUP: ItemGroupKeyAlias = createItemGroup(
        RESOURCE_FINDER_ITEM.defaultStack,
        "resource_finder",
        "itemGroup.$MOD_NAMESPACE.resource_finder_group"
    )

    lateinit var RECHARGE_RECIPE_SERIALIZER: RecipeSerializer<CustomRecipe<ResourceFinderChargeRecipe>>
    override fun onInitialize() {
        CompassComponents.SCAN_TARGETS_COMPONENT.apply {
            println(CompassComponents.SCAN_TARGETS_COMPONENT)
        }
        RECHARGE_RECIPE_SERIALIZER = registerCustomRecipe(
            Identifier.of(MOD_NAMESPACE, "crafting_special_resource_finder_charge")!!,
            ResourceFinderChargeRecipe()
        )
        ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_ITEM_GROUP).register {
            it.add(RESOURCE_FINDER_ITEM)
        }
    }
}