package hellozyemlya.resourcefinder.items

import hellozyemlya.mccompat.CompassComponents
import hellozyemlya.mccompat.ScanTarget
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.StringHelper
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrNull

class ResourceFinderCompass(settings: Settings) : Item(settings) {
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
    }

    override fun allowComponentsUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType?
    ) {
        stack.getOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, emptyMap()).forEach { k, v ->
            val blockName = Texts.setStyleIfAbsent(
                Registries.ITEM.get(k).name.copyContentOnly(),
                Style.EMPTY.withColor(TextColor.fromRgb(v.color))
            )
            tooltip.add(
                Texts.join(
                    mutableListOf(
                        ResourceFinderTexts.SCAN_FOR,
                        blockName,
                        ResourceFinderTexts.SCAN_JOIN,
                        Text.of(StringHelper.formatTicks(v.lifetimeTicks, context.updateTickRate))
                    ), Text.of(" ")
                )
            )
        }
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if (!world.isClient) {
                // update timeout
                val currentTimeout =
                    stack.getOrDefault(CompassComponents.TICK_TIMEOUT_COMPONENT, DEFAULT_SCAN_TIMEOUT) - 1

                if (currentTimeout <= 0) {
                    stack.set(CompassComponents.TICK_TIMEOUT_COMPONENT, DEFAULT_SCAN_TIMEOUT)
                    // update scan list
                    val currentScanList = stack.getOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, emptyMap())
                    if (currentScanList.isNotEmpty()) {
                        val position = entity.blockPos
                        val newScanList = mutableMapOf<Identifier, ScanTarget>()
                        currentScanList.forEach { (scanItemId, scanTarget) ->
                            val targetLifetime = scanTarget.lifetimeTicks - DEFAULT_SCAN_TIMEOUT
                            if (targetLifetime > 0) {
                                val resourceRecord =
                                    ResourceRegistry.INSTANCE.getByGroup(Registries.ITEM.get(scanItemId))
                                val posCandidate = resourceRecord.findClosest(16, position, world)
                                newScanList[scanItemId] =
                                    scanTarget.copy(lifetimeTicks = targetLifetime, target = posCandidate)
                            }
                        }
                        stack.set(CompassComponents.SCAN_TARGETS_COMPONENT, newScanList)
                    }
                } else {
                    stack.set(CompassComponents.TICK_TIMEOUT_COMPONENT, currentTimeout)
                }
            }
        }
    }
}