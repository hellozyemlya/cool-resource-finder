package hellozyemlya.resourcefinder.items

import hellozyemlya.common.getChildStacks
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.StringHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import net.minecraft.world.World
import org.slf4j.LoggerFactory

private const val SCAN_NBT_KEY = "resource_finder_compass.scan_for"
private const val POSITIONS_NBT_KEY = "resource_finder_compass.target_pos"

class ResourceFinderCompass(settings: Settings) : Item(settings) {
    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        stack.getScanList().forEach {
            val blockName = Texts.setStyleIfAbsent(
                it.resourceEntry.displayItem.name.copyContentOnly(),
                Style.EMPTY.withColor(TextColor.fromRgb(it.resourceEntry.color))
            )
            tooltip.add(
                Texts.join(
                    mutableListOf(
                        Text.of("Finds"),
                        blockName,
                        Text.of("for"),
                        Text.of(StringHelper.formatTicks(it.entryLifetime))
                    ), Text.of(" ")
                )
            )
        }
    }


    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if(world.isClient) {
//                stack.getChildStacks("arrows").forEach {
//                    ResourceFinder.LOGGER.info("Got child stacks from server what: ${it.orCreateNbt.getInt("what")} color: ${it.orCreateNbt.getInt("color")} ")
//                }
            } else {
                val arrows = stack.getChildStacks("arrows")
                arrows.clear()

                val scanList = stack.getScanList()
                scanList.removeIf { it.entryLifetime-- <= 0 }

                val position = entity.blockPos

                var idx = 0

                scanList.forEach {
                    val posCandidate = it.resourceEntry.findClosest(16, position, world)
                    if (posCandidate.isPresent) {
                        val arrowItemStack = ResourceFinder.RESOURCE_FINDER_ARROW_ITEM.defaultStack
                        arrowItemStack.arrowResource = it.resourceEntry
                        arrowItemStack.arrowTarget = posCandidate.get()
                        arrowItemStack.arrowIndex = idx
                        arrows.add(arrowItemStack)
                        idx++
                    }
                }
            }
        }
    }



//    abstract class NbtArray<T>() : ArrayList<T>() {
//        constructor(stack: ItemStack) : this() {
//            require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
//            this.read(stack.orCreateNbt)
//        }
//
//        protected abstract fun read(nbt: NbtCompound)
//
//        fun write(stack: ItemStack) {
//            require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
//            writeImpl(stack)
//        }
//
//        protected abstract fun writeImpl(stack: ItemStack)
//    }
//
//    data class ScanEntry(val entry: ResourceRegistry.ResourceEntry, var lifetime: Int)
//    class ScanNbt(stack: ItemStack) : NbtArray<ScanEntry>(stack) {
//        fun createOrGetEntryFor(resourceEntry: ResourceRegistry.ResourceEntry): ScanEntry {
//            var scanEntry: ScanEntry? = firstOrNull { it.entry == resourceEntry }
//            if (scanEntry == null) {
//                scanEntry = ScanEntry(resourceEntry, 0)
//                add(scanEntry)
//            }
//
//            return scanEntry
//        }
//
//        override fun read(nbt: NbtCompound) {
//            if (nbt.contains(SCAN_NBT_KEY)) {
//                val data = nbt.getIntArray(SCAN_NBT_KEY)
//                for (i in 0 until data.size / 2) {
//                    add(ScanEntry(ResourceRegistry.INSTANCE.getByIndex(data[i * 2])!!, data[i * 2 + 1]))
//                }
//            }
//
//        }
//
//        override fun writeImpl(stack: ItemStack) {
//            val data = IntArray(size * 2)
//            for (i in indices) {
//                val entry = this[i]
//                data[i * 2] = entry.entry.index
//                data[i * 2 + 1] = entry.lifetime
//            }
//
//            stack.orCreateNbt.putIntArray(SCAN_NBT_KEY, data)
//        }
//    }
}