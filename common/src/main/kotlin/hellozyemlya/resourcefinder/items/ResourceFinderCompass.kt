package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.items.compass.CompassData
import hellozyemlya.resourcefinder.items.storage.createItemStorageCache
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import kotlinx.serialization.Serializable
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.world.World
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.jvm.optionals.getOrNull

@Serializable
data class CompassNbtV0(
    var compassId: Int = -1,
    var data: CompassData? = null,
)


class ResourceFinderCompass(settings: Settings) : Item(settings) {
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
    }

    private val storage = createItemStorageCache<CompassData>(
        this,
        MOD_NAMESPACE,
        "compass-storage",
        World.OVERWORLD,
        { CompassData() }
    )

    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text?>, context: TooltipContext?) {
        // TODO fix me
//        stack.getScanList().forEach {
//            val blockName = Texts.setStyleIfAbsent(
//                it.key.name.copyContentOnly(),
//                Style.EMPTY.withColor(TextColor.fromRgb(it.color))
//            )
//            tooltip.add(
//                Texts.join(
//                    mutableListOf(
//                        ResourceFinderTexts.SCAN_FOR,
//                        blockName,
//                        ResourceFinderTexts.SCAN_JOIN,
//                        Text.of(StringHelper.formatTicks(it.lifetime))
//                    ), Text.of(" ")
//                )
//            )
//        }
    }


    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (selected && stack != null && world != null) {
            if (!world.isClient) {
                storage.modifyItemData(stack) { _, compassData ->
                    val doTick = compassData.scanList.size > 0

                    if (doTick) {
                        compassData.scanTimeoutTicks--
                        val doScan = compassData.scanTimeoutTicks == 0
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
                    }
                    false to Unit
                }
            }
        }
    }
}