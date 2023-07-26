package hellozyemlya.common.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

typealias TooltipCallback = (stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext?) -> Unit

interface ClientAwareItem {
    fun addTooltipCallback(callback: TooltipCallback)
}