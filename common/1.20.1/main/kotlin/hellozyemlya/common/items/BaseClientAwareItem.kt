package hellozyemlya.common.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

abstract class BaseClientAwareItem(settings: Settings) : Item(settings), ClientAwareItem {
    private var tooltipCallback: TooltipCallback? = null
    override fun addTooltipCallback(callback: TooltipCallback) {
        tooltipCallback = callback
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        tooltipCallback.run {
            if (this != null) {
                this(stack, world, tooltip, context)
            }
        }
    }
}