package hellozyemlya.common

import net.minecraft.item.Item
import hellozyemlya.common.items.ClientAwareItem
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

abstract class ClientItem<T>(val item: T) where T: Item, T : ClientAwareItem {
    init {
        item.addTooltipCallback(::appendTooltips)
    }


    protected open fun appendTooltips(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext?) {

    }
}