@file:JvmName("ResourceFinderCompassStackExtensions")

package hellozyemlya.resourcefinder.items

import hellozyemlya.common.NbtCompoundListWrapper
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class ScanRecord(val compound: NbtCompound) {
    public var resourceEntry: ResourceEntry
        get() = ResourceRegistry.INSTANCE.getByIndex(compound.getInt("what"))!!
        set(value) = compound.putInt("what", value.index)

    public var entryLifetime: Int
        get() = compound.getInt("lifetime")
        set(value) = compound.putInt("lifetime", value)

    constructor(entry: ResourceEntry, lifetime: Int) : this(NbtCompound()) {
        resourceEntry = entry
        entryLifetime = lifetime
    }

}

class TargetRecord(val compound: NbtCompound) {
    public var resourceEntry: ResourceEntry
        get() = ResourceRegistry.INSTANCE.getByIndex(compound.getInt("what"))!!
        set(value) = compound.putInt("what", value.index)

    public var target: BlockPos
        get() {
            val data = compound.getIntArray("position")
            return BlockPos(data[0], data[1], data[2])
        }
        set(value) = compound.putIntArray("position", intArrayOf(value.x, value.y, value.z))

    constructor(entry: ResourceEntry, target: BlockPos) : this(NbtCompound()) {
        resourceEntry = entry
        this.target = target
    }
}

class ScanRecordList(stack: ItemStack, key: String) : NbtCompoundListWrapper<ScanRecord>(stack, key) {
    override fun toCompound(element: ScanRecord): NbtCompound {
        return element.compound
    }

    override fun fromCompound(compound: NbtCompound): ScanRecord {
        return ScanRecord(compound)
    }

}

class TargetRecordList(stack: ItemStack, key: String) : NbtCompoundListWrapper<TargetRecord>(stack, key) {
    override fun toCompound(element: TargetRecord): NbtCompound {
        return element.compound
    }

    override fun fromCompound(compound: NbtCompound): TargetRecord {
        return TargetRecord(compound)
    }

}

private fun ItemStack.requireIsCompass() {
    require(this.item == ResourceFinder.RESOURCE_FINDER_ITEM)
}


public fun ItemStack.getScanList(): MutableList<ScanRecord> {
    this.requireIsCompass()
    return ScanRecordList(this, "scan_list")
}

public fun ItemStack.getTargetList(): MutableList<TargetRecord> {
    this.requireIsCompass()
    return TargetRecordList(this, "target_list")
}

public var ItemStack.scanTimeout: Int
    get() {
        this.requireIsCompass()
        val nbt = this.orCreateNbt
        return if(nbt.contains("scan_timeout")) {
            nbt.getInt("scan_timeout")
        } else {
            nbt.putInt("scan_timeout", ResourceFinderCompass.DEFAULT_SCAN_TIMEOUT)
            ResourceFinderCompass.DEFAULT_SCAN_TIMEOUT
        }
    }
    set(value) {
        this.requireIsCompass()
        this.orCreateNbt.putInt("scan_timeout", value)
    }