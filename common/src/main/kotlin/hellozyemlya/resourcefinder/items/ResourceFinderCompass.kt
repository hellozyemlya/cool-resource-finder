package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.items.compass.CompassData
import hellozyemlya.resourcefinder.items.compass.CompassScanItem
import hellozyemlya.resourcefinder.items.storage.itemStorageCache
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.StringHelper
import net.minecraft.world.World
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.jvm.optionals.getOrNull

class ResourceFinderCompass(settings: Settings) : Item(settings) {
    private var minecraftServer: MinecraftServer? = null

    init {
        ServerLifecycleEvents.SERVER_STARTED.register { server -> minecraftServer = server }
    }

    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
    }

    public val storage = itemStorageCache<CompassData>(
        this,
        "compass-storage",
        { CompassData() }
    )

    fun copyStack(stack: ItemStack): ItemStack {
        val result = defaultStack.copy()
        val originalData = storage.getItemData(stack)
        storage.modifyItemData(result) { _, data ->
            data.scanTimeoutTicks = -1
            originalData.scanList.forEach { (k, v) ->
                data.scanList[k] = CompassScanItem(v.lifetimeTicks, v.target, v.color)
            }
            true to Unit
        }
        return result
    }

    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        storage.getItemData(stack).scanList.forEach { (k, v) ->
            val blockName = Texts.setStyleIfAbsent(
                k.name.copyContentOnly(),
                Style.EMPTY.withColor(TextColor.fromRgb(v.color))
            )
            tooltip.add(
                Texts.join(
                    mutableListOf(
                        ResourceFinderTexts.SCAN_FOR,
                        blockName,
                        ResourceFinderTexts.SCAN_JOIN,
                        Text.of(StringHelper.formatTicks(v.lifetimeTicks))
                    ), Text.of(" ")
                )
            )
        }
    }


    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if (!world.isClient) {
                storage.modifyItemData(stack) { _, compassData ->
                    val doTick = compassData.scanList.size > 0

                    if (doTick) {
                        compassData.scanTimeoutTicks--
                        val doScan = compassData.scanTimeoutTicks <= 0
                        if (doScan) {
                            compassData.scanTimeoutTicks = DEFAULT_SCAN_TIMEOUT
                        }

                        // scan or cleanup scan list items
                        val position = entity.blockPos
                        val scanListIterator = compassData.scanList.iterator()

                        while (scanListIterator.hasNext()) {
                            val (key, value) = scanListIterator.next()
                            value.lifetimeTicks--
                            if (value.lifetimeTicks == 0) {
                                scanListIterator.remove()
                            }
                            if (doScan && value.lifetimeTicks != 0) {
                                val resourceRecord = ResourceRegistry.INSTANCE.getByGroup(key)
                                value.target = resourceRecord.findClosest(16, position, world).getOrNull()
                            }
                        }
                        true to Unit
                    } else {
                        false to Unit
                    }
                }
            }
        }
    }
}