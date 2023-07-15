package hellozyemlya.nbt

import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import kotlin.reflect.KProperty

interface INbt {
    var fieldCount: Int
    val nbt: NbtCompound
}


abstract class NbtField<TOwner : INbt, TValue>(owner: TOwner, default: TValue) {
    private val idx: Int = owner.fieldCount
    protected var value: TValue

    init {
        owner.fieldCount++
        value = if (owner.nbt.contains(idx.toString())) {
            this.read(owner.nbt)
        } else {
            default
        }
    }

    val nbtKey: String
        get() = idx.toString()

    operator fun getValue(thisRef: Any?, property: KProperty<*>): TValue {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: TValue) {
        this.value = value
    }

    protected abstract fun read(nbt: NbtCompound): TValue

    abstract fun write(nbt: NbtCompound)
}

open class SimpleNbtField<TOwner : INbt, TValue>(
    owner: TOwner, default: TValue,
    private val getter: (nbt: NbtCompound, key: String) -> TValue,
    private val setter: (nbt: NbtCompound, key: String, value: TValue) -> Unit
) : NbtField<TOwner, TValue>(owner, default) {
    override fun read(nbt: NbtCompound): TValue {
        return getter(nbt, nbtKey)
    }

    override fun write(nbt: NbtCompound) {
        setter(nbt, nbtKey, value)
    }
}

class IntNbtField<TOwner : INbt>(owner: TOwner) :
    SimpleNbtField<TOwner, Int>(owner, 0, NbtCompound::getInt, NbtCompound::putInt)

class BlockPosNbtField<TOwner : INbt>(owner: TOwner) : NbtField<TOwner, BlockPos>(owner, BlockPos.ORIGIN) {
    override fun read(nbt: NbtCompound): BlockPos {
        val pos = nbt.getCompound(nbtKey)
        return BlockPos(pos.getInt("x"), pos.getInt("y"), pos.getInt("z"))
    }

    override fun write(nbt: NbtCompound) {
        val pos = NbtCompound()
        pos.putInt("x", value.x)
        pos.putInt("y", value.y)
        pos.putInt("z", value.z)
        nbt.put(nbtKey, pos)
    }
}