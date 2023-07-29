package hellozyemlya.resourcefinder.items

import hellozyemlya.common.items.BaseSplitItem
import hellozyemlya.resourcefinder.items.state.FinderState
import hellozyemlya.serialization.generated.createDefault
import hellozyemlya.serialization.generated.readFrom
import hellozyemlya.serialization.generated.writeTo
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand

const val FINDER_STATE_NBT_KEY = "finder_state"
const val FINDER_ID_NBT_KEY = "finder_id"
class FinderItem(settings: Settings) : BaseSplitItem<FinderItem>(settings) {
    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }

    fun hasNbtState(stack: ItemStack): Boolean {
        return stack.hasNbt() && stack.nbt!!.contains(FINDER_STATE_NBT_KEY)
    }

    fun getNbtState(stack: ItemStack): FinderState {
        val nbt = stack.orCreateNbt
        return if(nbt.contains(FINDER_STATE_NBT_KEY)) {
            FinderState.readFrom(nbt.getCompound(FINDER_STATE_NBT_KEY))
        } else {
            FinderState.createDefault()
        }
    }
    inline fun modifyNbtState(stack: ItemStack, block: (state: FinderState) -> Unit) {
        val state = getNbtState(stack)

        block(state)

        val newStateNbt = NbtCompound()
        state.writeTo(newStateNbt)
        stack.orCreateNbt.put(FINDER_STATE_NBT_KEY, newStateNbt)
    }
}

