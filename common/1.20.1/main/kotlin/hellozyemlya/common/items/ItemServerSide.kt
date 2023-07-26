package hellozyemlya.common.items

import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

abstract class ItemServerSide<TItem>(protected val item: TItem) where TItem : BaseSplitItem<TItem> {
    open fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {

    }
}