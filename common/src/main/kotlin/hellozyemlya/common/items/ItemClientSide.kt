package hellozyemlya.common.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

abstract class ItemClientSide<TItem>(protected val item: TItem) where TItem : BaseSplitItem<TItem> {
    open fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {

    }

    open fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
    }
}