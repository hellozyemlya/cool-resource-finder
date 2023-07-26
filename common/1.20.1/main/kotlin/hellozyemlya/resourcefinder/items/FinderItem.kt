package hellozyemlya.resourcefinder.items

import hellozyemlya.common.items.BaseSplitItem
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

class FinderItem(settings: Settings) : BaseSplitItem<FinderItem>(settings) {
    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }
}

