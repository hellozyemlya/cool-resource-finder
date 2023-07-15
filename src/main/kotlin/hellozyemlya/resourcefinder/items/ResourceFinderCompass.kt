package hellozyemlya.resourcefinder.items

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
import net.minecraft.world.World
import org.slf4j.LoggerFactory

private const val SCAN_NBT_KEY = "resource_finder_compass.scan_for"
private const val POSITIONS_NBT_KEY = "resource_finder_compass.target_pos"

class ResourceFinderCompass(settings: Settings) : Item(settings) {
    public var clientInventoryTick: ((ItemStack?, World?, Entity, Int, Boolean) -> Unit)? = null

    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        ScanNbt(stack).forEach { scanEntry ->
            val blockName = Texts.setStyleIfAbsent(
                scanEntry.entry.displayItem.name.copyContentOnly(),
                Style.EMPTY.withColor(TextColor.fromRgb(scanEntry.entry.color))
            )
            tooltip.add(
                Texts.join(
                    mutableListOf(
                        Text.of("Finds"),
                        blockName,
                        Text.of("for"),
                        Text.of(StringHelper.formatTicks(scanEntry.lifetime))
                    ), Text.of(" ")
                )
            )
        }
    }


    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            // TODO move to server and send network packet with updated positions
            if(!world.isClient && world.time % 20 == 0L) {
                stack.orCreateNbt.putLong("hello.world", world.tickOrder)
            }
//            if (world.isClient) {
//                // decrease scan lifetime
//                val scanNbt = ScanNbt(stack)
//                scanNbt.removeIf { it.lifetime-- <= 0 }
//                scanNbt.write(stack)
//                // write scan data nbt
//                scan(stack, scanNbt, entity.blockPos, world)
//            }

            if(world.isClient) {
                LoggerFactory.getLogger("cool-resource-finder").info(stack.orCreateNbt.getLong("hello.world").toString())
                clientInventoryTick?.invoke(stack, world, entity, slot, selected)
            }
        }
    }

    companion object {
        private fun scan(itemStack: ItemStack, scanNbt: ScanNbt, position: BlockPos, world: World) {
            val targetsNbt = PositionNbt()

            scanNbt.forEach { scanEntry ->
                val posCandidate = scanEntry.entry.findClosest(16, position, world)
                if (posCandidate.isPresent) {
                    targetsNbt.add(PositionEntry(scanEntry.entry, posCandidate.get()))
                }
            }

            targetsNbt.write(itemStack)
        }
    }


    abstract class NbtArray<T>() : ArrayList<T>() {
        constructor(stack: ItemStack) : this() {
            require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
            this.read(stack.orCreateNbt)
        }

        protected abstract fun read(nbt: NbtCompound)

        fun write(stack: ItemStack) {
            require(stack.item == ResourceFinder.RESOURCE_FINDER_ITEM)
            writeImpl(stack)
        }

        protected abstract fun writeImpl(stack: ItemStack)
    }

    data class ScanEntry(val entry: ResourceRegistry.ResourceEntry, var lifetime: Int)
    class ScanNbt(stack: ItemStack) : NbtArray<ScanEntry>(stack) {
        fun createOrGetEntryFor(resourceEntry: ResourceRegistry.ResourceEntry): ScanEntry {
            var scanEntry: ScanEntry? = firstOrNull { it.entry == resourceEntry }
            if (scanEntry == null) {
                scanEntry = ScanEntry(resourceEntry, 0)
            }
            add(scanEntry)

            return scanEntry
        }

        override fun read(nbt: NbtCompound) {
            if (nbt.contains(SCAN_NBT_KEY)) {
                val data = nbt.getIntArray(SCAN_NBT_KEY)
                for (i in 0 until data.size / 2) {
                    add(ScanEntry(ResourceRegistry.INSTANCE.getByIndex(data[i * 2])!!, data[i * 2 + 1]))
                }
            }

        }

        override fun writeImpl(stack: ItemStack) {
            val data = IntArray(size * 2)
            for (i in indices) {
                val entry = this[i]
                data[i * 2] = entry.entry.index
                data[i * 2 + 1] = entry.lifetime
            }

            stack.orCreateNbt.putIntArray(SCAN_NBT_KEY, data)
        }
    }

    data class PositionEntry(val entry: ResourceRegistry.ResourceEntry, var position: BlockPos)


    class PositionNbt : NbtArray<PositionEntry> {
        constructor() : super()
        constructor(stack: ItemStack) : super(stack)

        override fun read(nbt: NbtCompound) {
            if (nbt.contains(POSITIONS_NBT_KEY)) {
                val data = nbt.getIntArray(POSITIONS_NBT_KEY)
                for (i in 0 until data.size / 4) {
                    add(
                        PositionEntry(
                            ResourceRegistry.INSTANCE.getByIndex(data[i * 4])!!,
                            BlockPos(data[i * 4 + 1], data[i * 4 + 2], data[i * 4 + 3])
                        )
                    )
                }
            }

        }

        override fun writeImpl(stack: ItemStack) {
            val data = IntArray(size * 4)
            for (i in indices) {
                val entry = this[i]
                data[i * 4] = entry.entry.index
                data[i * 4 + 1] = entry.position.x
                data[i * 4 + 2] = entry.position.y
                data[i * 4 + 3] = entry.position.z
            }

            stack.orCreateNbt.putIntArray(POSITIONS_NBT_KEY, data)
        }
    }
}