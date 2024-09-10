package hellozyemlya.resourcefinder.items

import hellozyemlya.compat.compatGet
import hellozyemlya.compat.compatGetOrDefault
import hellozyemlya.compat.compatSet
import hellozyemlya.compat.formatTicks
import hellozyemlya.compat.items.CompatItem
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.StringHelper
import net.minecraft.world.World

// TODO calculate ticks and time based on tick rate
class ResourceFinderCompass(settings: Settings) : CompatItem(settings) {
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
    }

    override fun compatAllowAnimations(): Boolean {
        return false
    }

    override fun compatAppendToolip(stack: ItemStack, tooltip: MutableList<Text>, tickRate: Float) {
        val scanTargets = stack.compatGet(CompassComponents.SCAN_TARGETS_COMPONENT)
        if (!scanTargets.isNullOrEmpty()) {
            scanTargets.forEach { (key, value) ->
                val blockName = Texts.setStyleIfAbsent(
                    Registries.ITEM.get(key).name.copyContentOnly(),
                    Style.EMPTY.withColor(TextColor.fromRgb(value.color))
                )
                tooltip.add(
                    Texts.join(
                        mutableListOf(
                            ResourceFinderTexts.SCAN_FOR,
                            blockName,
                            ResourceFinderTexts.SCAN_JOIN,
                            Text.of(formatTicks(value.lifetimeTicks, tickRate))
                        ), Text.of(" ")
                    )
                )
            }
        }
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if (!world.isClient) {
                val currentScanTimeout =
                    stack.compatGetOrDefault(
                        CompassComponents.TICK_TIMEOUT_COMPONENT,
                        DEFAULT_SCAN_TIMEOUT
                    ) - 1

                if (currentScanTimeout <= 0) {
                    val currentScanTargets = stack.compatGet(CompassComponents.SCAN_TARGETS_COMPONENT)
                    if (!currentScanTargets.isNullOrEmpty()) {
                        val newScanTargets = mutableMapOf<Identifier, ScanTarget>()
                        val position = entity.blockPos
                        currentScanTargets.forEach { (key, value) ->
                            val newLifetime = value.lifetimeTicks - DEFAULT_SCAN_TIMEOUT
                            if (newLifetime > 0) {
                                val resourceRecord = ResourceRegistry.INSTANCE.getByGroup(Registries.ITEM.get(key))
                                val posCandidate = resourceRecord.findClosest(16, position, world)
                                newScanTargets[key] = ScanTarget(newLifetime, value.color, posCandidate)
                            }
                        }
                        stack.compatSet(CompassComponents.SCAN_TARGETS_COMPONENT, newScanTargets)
                    }

                }
                stack.compatSet(
                    CompassComponents.TICK_TIMEOUT_COMPONENT,
                    if (currentScanTimeout <= 0) DEFAULT_SCAN_TIMEOUT else currentScanTimeout
                )
            }
        }
    }
}