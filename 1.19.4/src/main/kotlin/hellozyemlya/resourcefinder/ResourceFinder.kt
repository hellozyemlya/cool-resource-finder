package hellozyemlya.resourcefinder

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ResourceFinder : BaseResourceFinderInitializer() {

    val RESOURCE_FINDER_GROUP: ItemGroup = FabricItemGroup.builder(Identifier(MOD_NAMESPACE, "resource_finder"))
        .icon { ItemStack(super.RESOURCE_FINDER_ITEM) }
        .displayName(Text.translatable("itemGroup.tutorial.test_group"))
        .build()

    val GROUP_TRANSLATE_KEY = RESOURCE_FINDER_GROUP
    override fun onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_GROUP)
            .register { content -> content.add(super.RESOURCE_FINDER_ITEM) }
        super.onInitialize()
    }
}