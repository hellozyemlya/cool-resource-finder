package hellozyemlya.compat.items

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Hand

open class CompatItem(settings: Settings) : Item(settings) {
    override fun allowComponentsUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return compatAllowAnimations()
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType?
    ) {
        compatAppendToolip(stack, tooltip, context.updateTickRate)
    }

    open fun compatAllowAnimations(): Boolean {
        return true
    }

    open fun compatAppendToolip(stack: ItemStack, tooltip: MutableList<Text>, tickRate: Float) {
    }
}