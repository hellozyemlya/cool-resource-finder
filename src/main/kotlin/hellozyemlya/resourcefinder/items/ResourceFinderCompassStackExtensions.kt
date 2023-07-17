@file:JvmName("ResourceFinderCompassStackExtensions")

package hellozyemlya.resourcefinder.items

import hellozyemlya.common.NbtCompoundListWrapper
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

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

class ScanRecordList(stack: ItemStack, key: String) : NbtCompoundListWrapper<ScanRecord>(stack, key) {
    override fun toCompound(element: ScanRecord): NbtCompound {
        return element.compound
    }

    override fun fromCompound(compound: NbtCompound): ScanRecord {
        return ScanRecord(compound)
    }

}

private fun ItemStack.requireIsCompass() {
    require(this.item == ResourceFinder.RESOURCE_FINDER_ITEM)
}


public fun ItemStack.getScanList(): MutableList<ScanRecord> {
    this.requireIsCompass()
    return ScanRecordList(this, "scan_list")
}