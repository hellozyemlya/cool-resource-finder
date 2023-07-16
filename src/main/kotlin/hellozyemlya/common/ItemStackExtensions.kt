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

private class ItemStackList(private val stack: ItemStack, private val key: String) : AbstractMutableList<ItemStack>() {
    private val nbtList: NbtList
        get() {
            return if(stack.orCreateNbt.contains(key)){
                stack.orCreateNbt.getList(key, NbtElement.COMPOUND_TYPE.toInt())
            } else {
                val nbtList = NbtList()
                stack.orCreateNbt.put(key, nbtList)
                nbtList
            }
        }
    override val size: Int
        get() = nbtList.size

    override fun add(index: Int, element: ItemStack) {
        nbtList.add(index, element.toNbtCompound())
    }

    override fun get(index: Int): ItemStack {
        return ItemStack.fromNbt(nbtList.getCompound(index))
    }

    override fun removeAt(index: Int): ItemStack {
        return ItemStack.fromNbt(nbtList.removeAt(index) as NbtCompound)
    }

    override fun set(index: Int, element: ItemStack): ItemStack {
        return ItemStack.fromNbt(nbtList.set(index, element.toNbtCompound()) as NbtCompound)
    }
}