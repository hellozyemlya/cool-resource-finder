package hellozyemlya.resourcefinder.items

import hellozyemlya.compat.compatGet
import hellozyemlya.compat.compatSet
import hellozyemlya.compat.formatTicks
import hellozyemlya.compat.items.CompatItem
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.*

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

    private data class EntityTicker(var lastServerTick: Int = -1, var currentTicks: Int = 0)

    private val perEntityTicker = mutableMapOf<UUID, EntityTicker>()

    private fun doTick(world: World, entity: Entity): Boolean {
        val entityUuid = entity.uuid
        val ticker = if (!perEntityTicker.containsKey(entityUuid)) {
            val ticker = EntityTicker()
            perEntityTicker[entityUuid] = ticker
            ticker
        } else {
            perEntityTicker[entityUuid]!!
        }

        val serverTicks = world.server!!.ticks
        if (ticker.lastServerTick != serverTicks) {
            ticker.lastServerTick = serverTicks
            ticker.currentTicks += 1
        }

        return ticker.currentTicks % DEFAULT_SCAN_TIMEOUT == 0
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if (!world.isClient) {
                if (doTick(world, entity)) {
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
            }
        }
    }
}