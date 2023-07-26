package hellozyemlya.common

import net.minecraft.nbt.NbtCompound
import net.minecraft.world.PersistentState
import kotlin.reflect.KProperty




class PersistentStateHelper : PersistentState() {
    var hello: Int by PersistentInt(this, ::hello)
    override fun writeNbt(nbt: NbtCompound?): NbtCompound {
        return nbt!!
    }
}

class PersistentInt(val state: PersistentState, property: KProperty<*>) {
    private var storage: Int = 0

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return storage
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        state.markDirty()
        storage = value
    }
}