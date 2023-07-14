package hellozyemlya.resourcefinder.items.nbt

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ScanRegistry
import net.minecraft.item.ItemStack

@JvmInline
value class ResourceFinderCompassScanNbt(val data: IntArray) {
    init {
        require(data.size % DataSize == 0)
    }

    companion object {
        const val NBT_KEY = "resource_finder_compass.scan_for"
        const val DataSize = 2
        private val EMPTY = ResourceFinderCompassScanNbt(IntArray(0))

        fun readNbt(stack: ItemStack): ResourceFinderCompassScanNbt {
            require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
            return if (stack.orCreateNbt.contains(NBT_KEY)) {
                ResourceFinderCompassScanNbt(stack.orCreateNbt.getIntArray(NBT_KEY))
            } else {
                EMPTY
            }
        }
    }

    val count: Int
        get() = data.size / DataSize

    public inline fun forEach(callback: (what: ScanRegistry.RegistryEntry, lifeTime: Int) -> Unit) {
        for (i in 0 until data.size / DataSize) {
            callback(ScanRegistry.INSTANCE.getByIndex(data[i * DataSize])!!, data[i * DataSize + 1])
        }
    }

    public fun add(what: ScanRegistry.RegistryEntry, lifeTime: Int): ResourceFinderCompassScanNbt {
        for (i in 0 until data.size / DataSize) {
            if (data[i * DataSize] == what.index)
                return this
        }

        val newData = data.copyOf(data.size + DataSize)

        newData[newData.size - 2] = what.index
        newData[newData.size - 1] = lifeTime

        return ResourceFinderCompassScanNbt(newData)
    }

    fun setAt(idx: Int, what: ScanRegistry.RegistryEntry, lifeTime: Int) {
        data[idx * DataSize] = what.index
        data[idx * DataSize + 1] = lifeTime
    }

    fun writeNbt(stack: ItemStack) {
        require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
        stack.orCreateNbt.putIntArray(NBT_KEY, data)
    }
}