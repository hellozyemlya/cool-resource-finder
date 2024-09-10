package hellozyemlya.compat.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.world.World

open class CompatItem(settings: Settings) : Item(settings) {
    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return compatAllowAnimations()
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext?
    ) {
        compatAppendToolip(stack, tooltip, 20f)
    }

    open fun compatAllowAnimations(): Boolean {
        return true
    }

    open fun compatAppendToolip(stack: ItemStack, tooltip: MutableList<Text>, tickRate: Float) {
    }
}