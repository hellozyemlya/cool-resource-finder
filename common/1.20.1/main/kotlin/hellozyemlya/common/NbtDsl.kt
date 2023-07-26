package hellozyemlya.common

import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

inline fun NbtCompound.int(key: String, provider: () -> Int) {
    this.putInt(key, provider())
}

fun NbtCompound.int(key: String, value: Int) {
    this.putInt(key, value)
}

fun NbtCompound.int(key: String): Int {
    return this.getInt(key)
}

fun NbtCompound.item(key: String, item: Item) {
    this.putString(key, Registries.ITEM.getId(item).toString())
}

fun NbtCompound.item(key: String): Item {
    return Registries.ITEM.get(Identifier.tryParse(this.getString(key)))
}

fun NbtCompound.compound(key: String, provider: NbtCompound.() -> Unit) {
    if (this.contains(key)) {
        val compound = this.getCompound(key)
        compound.provider()
    } else {
        val compound = NbtCompound()
        this.put(key, compound)
        compound.provider()
    }
}

@JvmInline
value class NbtCompoundList(val list: NbtList) : Collection<NbtCompound> {
    override val size: Int
        get() = list.size

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun iterator(): Iterator<NbtCompound> = list.stream().map { it as NbtCompound }.iterator()

    override fun containsAll(elements: Collection<NbtCompound>): Boolean {
        return list.containsAll(elements)
    }

    override fun contains(element: NbtCompound): Boolean {
        return list.contains(element)
    }
}

fun NbtCompoundList.compound(provider: NbtCompound.() -> Unit) {
    val compound = NbtCompound()
    compound.provider()
    list.add(compound)
}

fun NbtCompoundList.clear() {
    list.clear()
}

fun NbtCompound.compoundList(key: String, provider: NbtCompoundList.() -> Unit) {
    if (this.contains(key)) {
        NbtCompoundList(this.getList(key, NbtElement.COMPOUND_TYPE.toInt())).provider()
    } else {
        val list = NbtList()
        this.put(key, list)
        NbtCompoundList(list).provider()
    }
}

fun NbtCompound.compoundList(key: String): NbtCompoundList {
    return if (this.contains(key)) {
        NbtCompoundList(this.getList(key, NbtElement.COMPOUND_TYPE.toInt()))
    } else {
        val list = NbtList()
        this.put(key, list)
        NbtCompoundList(list)
    }
}