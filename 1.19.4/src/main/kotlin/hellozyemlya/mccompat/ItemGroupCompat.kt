package hellozyemlya.mccompat

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun createItemGroup(icon: ItemStack, path: String, groupNameKey: String): ItemGroupKeyAlias {
    return FabricItemGroup.builder(Identifier(MOD_NAMESPACE, path))
            .icon { icon }
            .displayName(Text.translatable(groupNameKey))
            .build()
}
