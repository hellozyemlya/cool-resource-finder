package hellozyemlya.mccompat

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun createItemGroup(icon: ItemStack, path: String, groupNameKey: String): ItemGroupKeyAlias {
    val key = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MOD_NAMESPACE, path))

    Registry.register(
            Registries.ITEM_GROUP,
            key,
            FabricItemGroup.builder()
                    .icon { icon }
                    .displayName(Text.translatable(groupNameKey))
                    .build())
    return key
}