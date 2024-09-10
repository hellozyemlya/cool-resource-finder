package hellozyemlya.compat.items

import net.minecraft.item.Item

fun createItemSettings(maxCount: Int): Item.Settings {
    return Item.Settings().maxCount(maxCount)
}