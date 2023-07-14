package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.items.nbt.ResourceFinderCompassScanNbt
import hellozyemlya.resourcefinder.items.nbt.ResourceFinderCompassTargetsNbt
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

class ResourceFinderCompass(settings: Settings) : Item(settings) {

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        ResourceFinderCompassScanNbt.readNbt(stack).forEach { what, _ ->
            tooltip.add(
                Texts.setStyleIfAbsent(
                    what.resource.name.copyContentOnly(),
                    Style.EMPTY.withColor(TextColor.fromRgb(what.color))
                )
            )
        }
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            scan(stack, entity.blockPos, world)
        }
    }

    companion object {
        private fun scan(itemStack: ItemStack, position: BlockPos, world: World) {
            val scanNbt = ResourceFinderCompassScanNbt.readNbt(itemStack)
            var targetsNbt = ResourceFinderCompassTargetsNbt.EMPTY

            scanNbt.forEach { what, _ ->
                val posCandidate = what.findClosest(16, position, world)
                if (posCandidate.isPresent) {
                    targetsNbt = targetsNbt.add(what, posCandidate.get())
                }
            }

            targetsNbt.writeNbt(itemStack)
        }
    }
}