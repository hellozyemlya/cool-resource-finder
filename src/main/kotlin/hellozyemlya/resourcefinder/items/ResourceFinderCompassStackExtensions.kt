@file:JvmName("ResourceFinderCompassStackExtensions")

package hellozyemlya.resourcefinder.items

import hellozyemlya.common.NbtCompoundListWrapper
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class ScanRecord(val compound: NbtCompound) {
    public var resourceEntry: ResourceRegistry.ResourceEntry
        get() = ResourceRegistry.INSTANCE.getByIndex(compound.getInt("what"))!!
        set(value) = compound.putInt("what", value.index)

    public var entryLifetime: Int
        get() = compound.getInt("lifetime")
        set(value) = compound.putInt("lifetime", value)

    constructor(entry: ResourceRegistry.ResourceEntry, lifetime: Int) : this(NbtCompound()) {
        resourceEntry = entry
        entryLifetime = lifetime
    }

}

class TargetRecord(val compound: NbtCompound) {
    public var resourceEntry: ResourceRegistry.ResourceEntry
        get() = ResourceRegistry.INSTANCE.getByIndex(compound.getInt("what"))!!
        set(value) = compound.putInt("what", value.index)

    public var target: BlockPos
        get() {
            val data = compound.getIntArray("position")
            return BlockPos(data[0], data[1], data[2])
        }
        set(value) = compound.putIntArray("position", intArrayOf(value.x, value.y, value.z))

    constructor(entry: ResourceRegistry.ResourceEntry, target: BlockPos) : this(NbtCompound()) {
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