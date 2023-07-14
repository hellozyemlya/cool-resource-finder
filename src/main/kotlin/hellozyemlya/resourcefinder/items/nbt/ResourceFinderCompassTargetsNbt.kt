package hellozyemlya.resourcefinder.items.nbt

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ScanRegistry
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

@JvmInline
value class ResourceFinderCompassTargetsNbt(val data: IntArray) {
    init {
        require(data.size % DataSize == 0)
    }

    companion object {
        const val DataSize = 4
        val EMPTY = ResourceFinderCompassTargetsNbt(IntArray(0))
        const val NBT_KEY = "resource_finder_compass.target_pos"
        fun readNbt(stack: ItemStack): ResourceFinderCompassTargetsNbt {
            require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
            return if (stack.orCreateNbt.contains(NBT_KEY)) {
                ResourceFinderCompassTargetsNbt(stack.orCreateNbt.getIntArray(NBT_KEY))
            } else {
                EMPTY
            }
        }
    }

    val count: Int
        get() = data.size / DataSize

    fun setAt(idx: Int, what: Int, pos: BlockPos) {
        data[idx * DataSize] = what
        data[idx * DataSize + 1] = pos.x
        data[idx * DataSize + 2] = pos.y
        data[idx * DataSize + 3] = pos.z
    }

    fun add(what: ScanRegistry.RegistryEntry, pos: BlockPos): ResourceFinderCompassTargetsNbt {
        for (i in 0 until data.size / DataSize) {
            if (data[i * DataSize] == what.index)
                return this
        }

        val newData = data.copyOf(data.size + DataSize)

        newData[newData.size - 4] = what.index
        newData[newData.size - 3] = pos.x
        newData[newData.size - 2] = pos.y
        newData[newData.size - 1] = pos.z

        return ResourceFinderCompassTargetsNbt(newData)
    }

    inline fun forEach(callback: (entry: ScanRegistry.RegistryEntry, pos: BlockPos) -> Unit) {
        for (idx in 0 until data.size / DataSize) {
            callback(
                ScanRegistry.INSTANCE.getByIndex(data[idx * DataSize])!!,
                BlockPos(data[idx * DataSize + 1], data[idx * DataSize + 2], data[idx * DataSize + 3])
            )
        }
    }

    fun writeNbt(stack: ItemStack) {
        require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
        stack.orCreateNbt.putIntArray(NBT_KEY, data)
    }
}