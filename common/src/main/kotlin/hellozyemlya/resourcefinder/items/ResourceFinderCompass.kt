package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
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
import net.silkmc.silk.network.packet.s2cPacket

@Serializable
data class CompassItemChargeChange(val compassId: Int, val chargeItem: Int, val ticks: Int)
data class CompassItemPositionChange(val compassId: Int, val chargeItem: Int, val x: Int, val y: Int, val z: Int)
data class CompassItemPositionUnset(val compassId: Int, val chargeItem: Int)

@OptIn(ExperimentalSerializationApi::class)
class ResourceFinderCompass(settings: Settings) : Item(settings) {
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
        public val CHARGE_CHANGE_PACKET = s2cPacket<CompassItemChargeChange>(Identifier(MOD_NAMESPACE, "charge_change"))
        public val ALL_CHARGES_PACKET = s2cPacket<List<CompassItemChargeChange>>(Identifier(MOD_NAMESPACE, "all_charges"))
        init {
            CHARGE_CHANGE_PACKET.receiveOnClient { packet, context ->
                println("[CLIENT] ticks ${packet.ticks}")
            }
        }
    }
    init {
        println("ResourceFinderCompass created")
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
                scanList.removeIf {
                    it.lifetime--
                    CHARGE_CHANGE_PACKET.sendToAll(CompassItemChargeChange(-1, Registries.ITEM.getRawId(it.key), it.lifetime))
                    it.lifetime <= 0
                }

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