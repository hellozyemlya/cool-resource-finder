package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.registry.ResourceRegistry
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
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
    }

    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        superPuperMethod()
        return false
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        stack.getScanList().forEach {
            val blockName = Texts.setStyleIfAbsent(
                it.key.name.copyContentOnly(),
                Style.EMPTY.withColor(TextColor.fromRgb(it.color))
            )
            tooltip.add(
                Texts.join(
                    mutableListOf(
                        ResourceFinderTexts.SCAN_FOR,
                        blockName,
                        ResourceFinderTexts.SCAN_JOIN,
                        Text.of(StringHelper.formatTicks(it.lifetime))
                    ), Text.of(" ")
                )
            )
        }
    }


    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if (!world.isClient) {
                val scanList = stack.getScanList()
                scanList.removeIf { it.lifetime-- <= 0 }

                val position = entity.blockPos

                val currentScanTimeout = stack.scanTimeout--

                if (currentScanTimeout <= 0) {
                    val targetList = stack.getTargetList()
                    targetList.clear()

                    scanList.forEach {
                        val resourceRecord = ResourceRegistry.INSTANCE.getByGroup(it.key)
                        val posCandidate = resourceRecord.findClosest(16, position, world)
                        if (posCandidate.isPresent) {
                            targetList.add(TargetRecord(resourceRecord.color, posCandidate.get()))
                        }
                    }

                    stack.scanTimeout = DEFAULT_SCAN_TIMEOUT
                }

            }
        }
    }
}