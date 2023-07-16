package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.ResourceRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class ResourceFinderCompassArrowItem(settings: Settings) : Item(settings) {
    companion object {
        public fun readArrowData(stack: ItemStack): Triple<ResourceRegistry.ResourceEntry, BlockPos, Int> {
            return null!!
        }

        public fun writeArrowData(stack: ItemStack, resourceEntry: ResourceRegistry.ResourceEntry, pos: BlockPos, index: Int) {

        }
    }
}