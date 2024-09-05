package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.items.compass.*
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.silkmc.silk.nbt.serialization.decodeFromNbtElement
import net.silkmc.silk.nbt.serialization.encodeToNbtElement
import net.silkmc.silk.persistence.compoundKey
import net.silkmc.silk.persistence.persistentCompound
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.jvm.optionals.getOrNull

@Serializable
data class CompassNbtV0(
    var data: CompassData? = null,
    var compassId: Int = 0
)

private const val COMPASS_NBT_KEY = "data_v0"

class ResourceFinderCompass(settings: Settings) : Item(settings) {
    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
        val CACHE_KEY = compoundKey<ResourceFinderCompassCache>(Identifier(MOD_NAMESPACE, "cache"))
    }

    private var cache: ResourceFinderCompassCache = ResourceFinderCompassCache()

    public fun getCompassData(stack: ItemStack): Pair<Int, CompassData> {
        var modified = false
        val nbtTag = stack.orCreateNbt
        val compassNbt = if (nbtTag.contains(COMPASS_NBT_KEY)) {
            NBT_SERIALIZER.decodeFromNbtElement(nbtTag[COMPASS_NBT_KEY]!!)
        } else {
            modified = true
            CompassNbtV0()
        }.apply {
            if (compassId == 0) {
                compassId = cache.getNextId()
                modified = true
            }
        }
        val compassId = compassNbt.compassId

        val compassData = if (cache.instances.contains(compassId)) {
            cache.instances[compassId]!!
        } else {
            val possibleData = compassNbt.data
            if (possibleData != null) {
                cache.instances[compassId] = possibleData
                compassNbt.data = null
                modified = true
                possibleData
            } else {
                CompassData()
            }
        }

        if (modified) {
            putCompassNbt(stack, compassNbt)
        }
        return Pair(compassId, compassData)
    }

    private fun putCompassNbt(stack: ItemStack, compassNbt: CompassNbtV0) {
        stack.orCreateNbt.put(COMPASS_NBT_KEY, NBT_SERIALIZER.encodeToNbtElement(compassNbt))
    }

    init {
        // load compass cache from persistent storage
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            val overWorld = it.getWorld(World.OVERWORLD)
            if (overWorld != null) {
                val possibleCache = overWorld.persistentCompound[CACHE_KEY]
                if (possibleCache != null) {
                    cache = possibleCache
                }
            }
        })

        ServerPlayConnectionEvents.JOIN.register({ handler: ServerPlayNetworkHandler,
                                                   _: PacketSender,
                                                   _: MinecraftServer ->
            Packets.CACHE_PACKET.send(cache, handler.player)
        })
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
                val (compassId, compassData) = getCompassData(stack)
                // handle scan timeout
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
                    // TODO collect all updates to single packet and send on tick end
                    Packets.SCAN_ITEM_CHANGE_PACKET.sendToAll(
                        listOf(
                            ScanItemChange(
                                compassId,
                                key,
                                value.lifetimeTicks,
                                value.target
                            )
                        )
                    )
                }
            }
        }
    }
}