package hellozyemlya.resourcefinder.items.server.state

import kotlinx.serialization.SerialName
import net.minecraft.nbt.NbtCompound
import net.minecraft.world.PersistentState

class FinderIdAllocator() : PersistentState() {
    private constructor(id: Int) : this() {
        nextId = id
    }

    @SerialName("next_finder_id")
    private var nextId: Int = 0

    fun allocateId(): Int {
        val result = ++nextId
        markDirty()
        return result
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.putInt("next_finder_id", nextId)
        return nbt
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): FinderIdAllocator {
            return FinderIdAllocator(nbt.getInt("next_finder_id"))
        }
    }
}