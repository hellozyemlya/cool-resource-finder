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
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.util.*

// TODO calculate ticks and time based on tick rate
class ResourceFinderCompass(settings: Settings) : CompatItem(settings) {
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
        const val DEFAULT_SCAN_DISTANCE = 16
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

    override fun use(world: World?, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val scanMode = stack.compatGetOrDefault(CompassComponents.SCAN_MODE_COMPONENT, ScanMode.SPHERICAL)
        stack.compatSet(
            CompassComponents.SCAN_MODE_COMPONENT,
            if (scanMode == ScanMode.SPHERICAL) ScanMode.CIRCULAR else ScanMode.SPHERICAL
        )
        return super.use(world, user, hand)
    }

    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (stack != null && world != null) {
            if (!world.isClient) {
                if (entity.isPlayer) {
                    val playerEntity = entity as PlayerEntity
                    if (selected || playerEntity.offHandStack == stack || playerEntity.mainHandStack == stack) {
                        if (doTick(world, entity)) {
                            val currentScanTargets = stack.compatGet(CompassComponents.SCAN_TARGETS_COMPONENT)
                            val scanMode =
                                stack.compatGetOrDefault(CompassComponents.SCAN_MODE_COMPONENT, ScanMode.SPHERICAL)

                            // in spherical scan mode scans for 16 blocks in each direction
                            // in circular mode scans twice horizontally, but only 2 blocks up-down direction
                            val verticalDistance = if (scanMode == ScanMode.SPHERICAL) DEFAULT_SCAN_DISTANCE else 2
                            val horizontalDistance =
                                if (scanMode == ScanMode.SPHERICAL) DEFAULT_SCAN_DISTANCE else DEFAULT_SCAN_DISTANCE * 2

                            if (!currentScanTargets.isNullOrEmpty()) {
                                val newScanTargets = mutableMapOf<Identifier, ScanTarget>()
                                val position = entity.blockPos.add(0, 1, 0)
                                currentScanTargets.forEach { (key, value) ->
                                    val newLifetime = value.lifetimeTicks - DEFAULT_SCAN_TIMEOUT
                                    if (newLifetime > 0) {
                                        val resourceRecord =
                                            ResourceRegistry.INSTANCE.getByGroup(Registries.ITEM.get(key))
                                        val posCandidate =
                                            resourceRecord.findClosest(horizontalDistance, verticalDistance, position, world)
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
    }
}