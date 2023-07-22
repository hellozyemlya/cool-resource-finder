package hellozyemlya.resourcefinder

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ResourceFinder : BaseResourceFinderInitializer() {
    val RESOURCE_FINDER_GROUP_KEY: RegistryKey<ItemGroup> =
        RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier(MOD_NAMESPACE, "resource_finder"))

    public val RESOURCE_FINDER_GROUP: ItemGroup = Registry.register(
        Registries.ITEM_GROUP,
        RESOURCE_FINDER_GROUP_KEY,
        FabricItemGroup.builder()
            .icon { ItemStack(RESOURCE_FINDER_ITEM) }
            .displayName(Text.translatable("itemGroup.$MOD_NAMESPACE.resource_finder_group"))
            .build())

    val GROUP_TRANSLATE_KEY = RESOURCE_FINDER_GROUP_KEY
    override fun onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(RESOURCE_FINDER_GROUP_KEY)
            .register { content -> content.add(RESOURCE_FINDER_ITEM) }
        super.onInitialize()
    }
}