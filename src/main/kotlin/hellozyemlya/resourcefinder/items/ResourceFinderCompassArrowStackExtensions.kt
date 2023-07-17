@file:JvmName("ResourceFinderCompassArrowStackExtensions")

package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import java.lang.IllegalArgumentException

public fun ItemStack.requireIsArrow() {
    require(this.item == ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
}

var ItemStack.arrowResource: ResourceRegistry.ResourceEntry
    get() {
        this.requireIsArrow()
        if (this.hasNbt() && this.nbt!!.contains("arrow_resource_entry")) {
            return ResourceRegistry.INSTANCE.getByIndex(this.nbt!!.getInt("arrow_resource_entry"))!!
        }
        throw IllegalArgumentException("Stack does not contains arrow_resource_entry")
    }
    set(value) {
        this.requireIsArrow()
        this.orCreateNbt.putInt("arrow_resource_entry", value.index)
    }

var ItemStack.arrowTarget: BlockPos
    get() {
        this.requireIsArrow()
        if (this.hasNbt() && this.nbt!!.contains("arrow_target")) {
            val pos = this.nbt!!.getIntArray("arrow_target")
            return BlockPos(pos[0], pos[1], pos[2])
        }

        return BlockPos.ORIGIN
    }
    set(value) {
        this.requireIsArrow()
        this.orCreateNbt.putIntArray("arrow_target", intArrayOf(value.x, value.y, value.z))
    }

var ItemStack.arrowIndex: Int
    get() {
        this.requireIsArrow()
        if (this.hasNbt() && this.nbt!!.contains("arrow_index")) {
            return this.nbt!!.getInt("arrow_index")
        }

        return -1
    }
    set(value) {
        this.requireIsArrow()
        this.orCreateNbt.putInt("arrow_index", value)
    }