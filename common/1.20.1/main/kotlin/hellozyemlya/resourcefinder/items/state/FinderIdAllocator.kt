package hellozyemlya.resourcefinder.items.state

import hellozyemlya.serialization.annotations.McSerialize
import kotlinx.serialization.SerialName
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.PersistentState

@McSerialize
abstract class FinderIdAllocator : PersistentState() {
    public abstract var nextId: Int

    fun allocateId(): Int {
        val result = ++nextId
        markDirty()
        return result
    }

    companion object
}