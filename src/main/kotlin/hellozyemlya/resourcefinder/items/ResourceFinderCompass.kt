package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.ScanRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class ResourceFinderCompass(settings: Settings) : Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        if (stack.getOrCreateNbt().contains(SCAN_FOR_NBT_KEY)) {
            val scanFor = stack.getOrCreateNbt().getIntArray(SCAN_FOR_NBT_KEY)
            for (idx in scanFor) {
                val data = ScanRegistry.INSTANCE.getByIndex(idx)
                tooltip.add(
                    Texts.setStyleIfAbsent(
                        data!!.resource.name.copyContentOnly(),
                        Style.EMPTY.withColor(TextColor.fromRgb(data.color))
                    )
                )
            }
        }
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            scan(stack, entity.blockPos, world)
        }
    }

    companion object {
        const val SCAN_FOR_NBT_KEY = "compass3d.scan_for"
        const val TARGET_POS_NBT_KEY = "compass3d.target_pos"
        const val MAX_SCAN_COUNT = 5
        fun forEachTarget(itemStack: ItemStack, callback: (entry: ScanRegistry.RegistryEntry, pos: BlockPos) -> Unit) {
            if (itemStack.getOrCreateNbt().contains(TARGET_POS_NBT_KEY)) {
                val targetPos =
                    itemStack.getOrCreateNbt().getIntArray(TARGET_POS_NBT_KEY)
                for (pos in 0 until targetPos.size / 4) {
                    val idx = targetPos[pos * 4]
                    if (idx != -1) {
                        val data: ScanRegistry.RegistryEntry = ScanRegistry.INSTANCE.getByIndex(idx)!!
                        callback(
                            data,
                            BlockPos(targetPos[pos * 4 + 1], targetPos[pos * 4 + 2], targetPos[pos * 4 + 3])
                        )
                    }
                }
            }
        }

        fun addForScanning(itemStack: ItemStack, entry: ScanRegistry.RegistryEntry) {
            if (itemStack.getOrCreateNbt().contains(SCAN_FOR_NBT_KEY)) {
                val scanFor =
                    itemStack.getOrCreateNbt().getIntArray(SCAN_FOR_NBT_KEY)
                if (Arrays.stream(scanFor).filter { value: Int -> value == entry.index }.findFirst().isEmpty) {
                    val newValue = Arrays.copyOf(scanFor, scanFor.size + 1)
                    newValue[scanFor.size] = entry.index
                    itemStack.getOrCreateNbt()
                        .putIntArray(SCAN_FOR_NBT_KEY, newValue)
                }
            } else {
                itemStack.getOrCreateNbt()
                    .putIntArray(SCAN_FOR_NBT_KEY, intArrayOf(entry.index))
            }
        }

        fun canAddForScanning(itemStack: ItemStack): Boolean {
            require(itemStack.item is ResourceFinderCompass)
            return if (itemStack.getOrCreateNbt().contains(SCAN_FOR_NBT_KEY)) {
                val scanFor = itemStack.getOrCreateNbt().getIntArray(SCAN_FOR_NBT_KEY)
                scanFor.size < MAX_SCAN_COUNT
            } else {
                true
            }
        }

        private fun scan(itemStack: ItemStack, position: BlockPos, world: World) {
            if (itemStack.getOrCreateNbt().contains(SCAN_FOR_NBT_KEY)) {
                val scanFor =
                    itemStack.getOrCreateNbt().getIntArray(SCAN_FOR_NBT_KEY)
                if (scanFor.isNotEmpty()) {
                    val newNbtData = IntArray(scanFor.size * 4)
                    for (pos in scanFor.indices) {
                        val data = ScanRegistry.INSTANCE.getByIndex(scanFor[pos])
                        val posCandidate = data!!.findClosest(16, position, world)
                        if (posCandidate.isPresent) {
                            newNbtData[pos * 4] = data.index
                            newNbtData[pos * 4 + 1] = posCandidate.get().x
                            newNbtData[pos * 4 + 2] = posCandidate.get().y
                            newNbtData[pos * 4 + 3] = posCandidate.get().z
                        } else {
                            newNbtData[pos * 4] = -1
                        }
                    }
                    itemStack.getOrCreateNbt()
                        .putIntArray(TARGET_POS_NBT_KEY, newNbtData)
                }
            } else {
                itemStack.getOrCreateNbt().remove(TARGET_POS_NBT_KEY)
            }
        }
    }
}