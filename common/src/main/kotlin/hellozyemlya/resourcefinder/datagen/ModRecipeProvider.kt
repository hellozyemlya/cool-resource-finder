package hellozyemlya.resourcefinder.datagen

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.ResourceFinder
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class ModRecipeProvider(
    output: FabricDataOutput?,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>?
) : FabricRecipeProvider(output, registriesFuture) {
    override fun generate(exporter: RecipeExporter?) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ResourceFinder.RESOURCE_FINDER_ITEM, 1)
            .pattern(" C ")
            .pattern("C#C")
            .pattern(" C ")
            .input('C', Items.COMPARATOR)
            .input('#', Items.COMPASS)
            .criterion(hasItem(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR))
            .criterion(hasItem(Items.COMPASS), conditionsFromItem(Items.COMPASS))
            .offerTo(exporter, Identifier.of(MOD_NAMESPACE, "resource_finder_compass_recipe"))
    }
}