package hellozyemlya.compat.items

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item

fun createItemSettings(maxCount: Int): Item.Settings {
    return FabricItemSettings().maxCount(maxCount)
}