package hellozyemlya.common

import kotlinx.serialization.minecraft.getFrom
import kotlinx.serialization.minecraft.put
import kotlinx.serialization.serializer
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager


inline fun <reified T : BasePersistentState> PersistentStateManager.getOrCreate(key: String, noinline factory: () -> T): T {
    return this.getOrCreate(serializer<T>()::getFrom, factory, key)
}

open class BasePersistentState : PersistentState() {
    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        serializer(this::class.java).put(this, nbt)
        return nbt
    }
}