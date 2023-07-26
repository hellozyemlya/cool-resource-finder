package hellozyemlya.common

import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries

inline fun NbtCompound.int(key: String, provider: () -> Int) {
    this.putInt(key, provider())
}

fun NbtCompound.int(key: String, value: Int) {
    this.putInt(key, value)
}

fun NbtCompound.item(key: String, item: Item) {
    this.putString(key, Registries.ITEM.getId(item).toString())
}

fun NbtCompound.compound(key: String, provider: NbtCompound.() -> Unit) {
    if(this.contains(key)) {
        val compound = this.getCompound(key)
        compound.provider()
    } else {
        val compound = NbtCompound()
        this.put(key, compound)
        compound.provider()
    }
}

@JvmInline
value class NbtCompoundList(val list: NbtList)

fun NbtCompoundList.compound(provider: NbtCompound.() -> Unit) {
    val compound = NbtCompound()
    compound.provider()
    list.add(compound)
}

fun NbtCompoundList.clear() {
    list.clear()
}

fun NbtCompound.compoundList(key: String, provider: NbtCompoundList.() -> Unit) {
    if(this.contains(key)) {
        NbtCompoundList(this.getList(key, NbtElement.COMPOUND_TYPE.toInt())).provider()
    } else {
        val list = NbtList()
        this.put(key, list)
        NbtCompoundList(list).provider()
    }
}