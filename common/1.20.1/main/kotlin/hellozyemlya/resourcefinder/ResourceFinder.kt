package hellozyemlya.resourcefinder

import hellozyemlya.mccompat.ItemGroupKeyAlias
import hellozyemlya.mccompat.createItemGroup
import hellozyemlya.resourcefinder.items.FinderItem
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderChargeRecipe
import hellozyemlya.resourcefinder.items.server.FinderItemServerSide
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
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
    private val LOGGER: Logger = LoggerFactory.getLogger("cool-resource-finder")

    val RESOURCE_FINDER_ITEM: FinderItem = Registry.register(
            Registries.ITEM, Identifier(MOD_NAMESPACE, "resource_finder_compass"),
            FinderItem(FabricItemSettings().maxCount(1))
    ).setServerSide(::FinderItemServerSide)

    val RESOURCE_FINDER_ITEM_GROUP: ItemGroupKeyAlias = createItemGroup(RESOURCE_FINDER_ITEM.defaultStack, "resource_finder", "itemGroup.$MOD_NAMESPACE.resource_finder_group")

    val RESOURCE_FINDER_REPAIR_SERIALIZER: RecipeSerializer<ResourceFinderChargeRecipe> = Registry.register(
            Registries.RECIPE_SERIALIZER,
            Identifier(MOD_NAMESPACE, "crafting_special_resource_finder_charge"),
            SpecialRecipeSerializer { id, category ->
                ResourceFinderChargeRecipe(
                        id,
                        category
                )
            })

    override fun onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_ITEM_GROUP).register {
            it.add(RESOURCE_FINDER_ITEM)
        }

        LOGGER.info("'Cool Resource Finder' scans for ${ResourceRegistry.INSTANCE.groups.count()} resource groups.")
    }
}