@file:JvmName("ItemStackExtensions")

package hellozyemlya.common

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList


fun ItemStack.getChildStacks(key: String): MutableList<ItemStack> {
    return ItemStackList(this, key)
}

fun ItemStack.toNbtCompound(): NbtCompound {
    val nbt = NbtCompound()
    this.writeNbt(nbt)
    return nbt
}

private class ItemStackList(stack: ItemStack, key: String) : NbtCompoundListWrapper<ItemStack>(stack, key) {
    override fun toCompound(element: ItemStack): NbtCompound {
        return element.toNbtCompound()
    }

    override fun fromCompound(compound: NbtCompound): ItemStack {
        return ItemStack.fromNbt(compound)
    }

}