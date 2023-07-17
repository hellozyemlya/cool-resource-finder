package hellozyemlya.resourcefinder.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.StringHelper
import net.minecraft.world.World

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
            if (!world.isClient) {
                val scanList = stack.getScanList()
                scanList.removeIf { it.entryLifetime-- <= 0 }

                val position = entity.blockPos

                val targetList = stack.getTargetList()
                targetList.clear()

                scanList.forEach {
                    val posCandidate = it.resourceEntry.findClosest(16, position, world)
                    if (posCandidate.isPresent) {
                        targetList.add(TargetRecord(it.resourceEntry, posCandidate.get()))
                    }
                }
            }
        }
    }
}