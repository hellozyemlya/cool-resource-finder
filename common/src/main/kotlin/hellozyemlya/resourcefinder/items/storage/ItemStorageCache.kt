package hellozyemlya.resourcefinder.items.storage

import hellozyemlya.resourcefinder.items.compass.NETWORK_CBOR
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.silkmc.silk.network.packet.ServerToClientPacketDefinition
import net.silkmc.silk.network.packet.s2cPacket
import net.silkmc.silk.persistence.compoundKey
import net.silkmc.silk.persistence.persistentCompound

/**
 * Represents client-server synchronized storage of data, associated with item stack. Requires stack max size to be 1.
 */
@OptIn(ExperimentalSerializationApi::class)
class ItemStorageCache<T : Any>(
    private val item: Item,
    nbtIdKey: Identifier,
    persistKey: Identifier,
    private val persistIn: RegistryKey<World> = World.OVERWORLD,
    private val updPacket: ServerToClientPacketDefinition<Pair<Int, T>>,
    private val cacheDumpPacket: ServerToClientPacketDefinition<Map<Int, T>>,
    private val defaultData: () -> T,
) {
    private val nbtIdKey = nbtIdKey.toString()
    private val persistCompoundKey = compoundKey<ByteArray>(persistKey)

    init {
        assert(item.maxCount == 1, { "ItemStorageCache requires maxCount = 1" })
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            val persistWorld = it.getWorld(persistIn)
            if (persistWorld != null) {
                val possibleCache = persistWorld.persistentCompound[persistCompoundKey]
                if (possibleCache != null) {
                    cache.clear()
                    cache.putAll(NETWORK_CBOR.decodeFromByteArray<Map<Int, T>>(possibleCache))
                }
            }
        })

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler,
                                                   _: PacketSender,
                                                   _: MinecraftServer ->
            cacheDumpPacket.send(cache, handler.player)
        }
    }

    private val cache: HashMap<Int, T> = hashMapOf()
    private var nextId: Int = -1
    private fun getNextId(): Int = ++nextId

    fun getItemData(stack: ItemStack): T {
        return modifyItemData(stack) { _, data -> false to data }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun <R> modifyItemData(stack: ItemStack, use: (id: Int, data: T) -> Pair<Boolean, R>): R {
        if (stack.item != item) {
            throw Exception("Requires ${item}, got ${stack.item}")
        }

        // get or create default nbt
        val itemId = if (stack.hasNbt() && stack.nbt!!.contains(nbtIdKey)) {
            stack.nbt!!.getInt(nbtIdKey)
        } else {
            val newId = getNextId()
            stack.orCreateNbt.putInt(nbtIdKey, newId)
            newId
        }

        if (!cache.containsKey(itemId)) {
            cache[itemId] = defaultData()
        }
        val itemData = cache[itemId]!!

        val (modified, result) = use(itemId, itemData)

        // send updated data for item to all currently connected clients
        if (modified) {
            updPacket.sendToAll(itemId to itemData)
        }

        return result
    }

    inner public class Client {
        private val clientCache: HashMap<Int, T> = hashMapOf()

        init {
            cacheDumpPacket.receiveOnClient { packet, _ ->
                clientCache.clear()
                clientCache.putAll(packet)
            }

            updPacket.receiveOnClient { (id, data), _ ->
                clientCache[id] = data
            }
        }

        public fun <R> useData(stack: ItemStack, use: (id: Int, data: T) -> R): R {
            if (stack.item != item) {
                throw Exception("Requires ${item}, got ${stack.item}")
            }
            if (stack.hasNbt() && stack.nbt!!.contains(nbtIdKey)) {
                val itemId = stack.nbt!!.getInt(nbtIdKey)
                return use(itemId, if (clientCache.contains(itemId)) clientCache[itemId]!! else defaultData())
            }

            return use(-1, defaultData())
        }

        public fun getData(stack: ItemStack): T {
            if (stack.item != item) {
                throw Exception("Requires ${item}, got ${stack.item}")
            }
            if (stack.hasNbt() && stack.nbt!!.contains(nbtIdKey)) {
                val itemId = stack.nbt!!.getInt(nbtIdKey)
                return if (clientCache.contains(itemId)) clientCache[itemId]!! else defaultData()
            }
            return defaultData()
        }
    }

    fun createClient(): Client {
        return Client()
    }
}

@OptIn(ExperimentalSerializationApi::class)
public inline fun <reified T : Any> createItemStorageCache(
    item: Item,
    prefix: String,
    suffix: String,
    persistIn: RegistryKey<World>,
    noinline defaultData: () -> T
): ItemStorageCache<T> {
    return ItemStorageCache<T>(
        item, Identifier(prefix, "${suffix}-id-key"),
        Identifier(prefix, "${suffix}-persist"),
        persistIn,
        s2cPacket<Pair<Int, T>>(Identifier(prefix, "${suffix}-upd-pkt"), NETWORK_CBOR),
        s2cPacket<Map<Int, T>>(Identifier(prefix, "${suffix}-dump-pkt"), NETWORK_CBOR),
        defaultData
    )
}