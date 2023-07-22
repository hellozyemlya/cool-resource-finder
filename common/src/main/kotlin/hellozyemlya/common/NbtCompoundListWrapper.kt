package hellozyemlya.common

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList

abstract class NbtCompoundListWrapper<T>(private val stack: ItemStack, private val key: String) :
        AbstractMutableList<T>() {
    private val nbtList: NbtList
        get() {
            return if (stack.orCreateNbt.contains(key)) {
                stack.orCreateNbt.getList(key, NbtElement.COMPOUND_TYPE.toInt())
            } else {
                val nbtList = NbtList()
                stack.orCreateNbt.put(key, nbtList)
                nbtList
            }
        }
    override val size: Int
        get() = nbtList.size

    protected abstract fun toCompound(element: T): NbtCompound
    protected abstract fun fromCompound(compound: NbtCompound): T
    override fun add(index: Int, element: T) {
        nbtList.add(index, toCompound(element))
    }

    override fun get(index: Int): T {
        return fromCompound(nbtList.getCompound(index))
    }

    override fun removeAt(index: Int): T {
        return fromCompound(nbtList.removeAt(index) as NbtCompound)
    }

    override fun set(index: Int, element: T): T {
        return fromCompound(nbtList.set(index, toCompound(element)) as NbtCompound)
    }

}