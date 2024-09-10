package hellozyemlya.resourcefinder

import hellozyemlya.compat.recipes.registerCustomRecipe
import hellozyemlya.mccompat.ItemGroupKeyAlias
import hellozyemlya.mccompat.createItemGroup
import hellozyemlya.resourcefinder.items.ResourceFinderCompass
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderChargeRecipe
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ResourceFinder : ModInitializer {
    val RESOURCE_FINDER_ITEM: ResourceFinderCompass = Registry.register(
            Registries.ITEM, Identifier(MOD_NAMESPACE, "resource_finder_compass"),
            ResourceFinderCompass(FabricItemSettings().maxCount(1))
    )

    val RESOURCE_FINDER_ITEM_GROUP: ItemGroupKeyAlias = createItemGroup(RESOURCE_FINDER_ITEM.defaultStack, "resource_finder", "itemGroup.$MOD_NAMESPACE.resource_finder_group")

    override fun onInitialize() {
        registerCustomRecipe(Identifier.of(MOD_NAMESPACE, "crafting_special_resource_finder_charge")!!, ResourceFinderChargeRecipe())
        ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_ITEM_GROUP).register {
            it.add(RESOURCE_FINDER_ITEM)
        }
    }
}