package hellozyemlya.resourcefinder.items.storage

import hellozyemlya.resourcefinder.items.compass.NETWORK_CBOR
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
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


@Serializable
data class ItemNbt<T>(var id: Int = -1, var data: T?)

/**
 * Represents client-server synchronized storage of data, associated with item stack. Requires stack max size to be 1.
 */
@OptIn(ExperimentalSerializationApi::class)
class ItemStorageCache<T : Any>(
    private val item: Item,
    private val nbtKey: Identifier,
    persistKey: Identifier,
    private val persistIn: RegistryKey<World> = World.OVERWORLD,
    private val updPacket: ServerToClientPacketDefinition<Pair<Int, T>>,
    private val cacheDumpPacket: ServerToClientPacketDefinition<Map<Int, T>>,
    private val defaultData: () -> T,
) {
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

    fun <R> modifyItemData(stack: ItemStack, use: (id: Int, data: T) -> Pair<Boolean, R>): R {
        return modifyItemData(stack, true, use)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun <R> modifyItemData(stack: ItemStack, useCache: Boolean, use: (id: Int, data: T) -> Pair<Boolean, R>): R {
        if (stack.item != item) {
            throw Exception("Requires ${item}, got ${stack.item}")
        }

        var needWriteNbt = false

        // get or create default nbt
        val nbt: ItemNbt<T> = if (stack.hasNbt() && stack.nbt!!.contains(nbtKey.toString())) {
            val nbt = stack.nbt!!.getByteArray(nbtKey.toString())
            NETWORK_CBOR.decodeFromByteArray(nbt)
        } else {
            needWriteNbt = true
            ItemNbt<T>(-1, defaultData())
        }

        val (itemId, itemData) = if (useCache) {
            // dump to cache(or read from cache) if required
            if (nbt.id == -1) {
                // dump from nbt to cache, and return value from cache
                nbt.id = getNextId()
                needWriteNbt = true
                cache[nbt.id] = if (nbt.data != null) {
                    nbt.data!!
                } else {
                    defaultData()
                }
                nbt.data = null
                nbt.id to cache[nbt.id]!!
            } else {
                // or just get from cache
                if (!cache.containsKey(nbt.id)) {
                    cache[nbt.id] = defaultData()
                }
                nbt.id to cache[nbt.id]!!
            }
        } else {
            // just use nbt value or cached value
            nbt.id to nbt.data!!
        }

        val (modify, result) = use(itemId, itemData)

        // modifications made, and we don't using cache, need write nbt
        needWriteNbt = needWriteNbt || (!useCache && modify)

        if (needWriteNbt) {
            stack.orCreateNbt.putByteArray(nbtKey.toString(), NETWORK_CBOR.encodeToByteArray(nbt))
        }

        // send updated data for item to all currently connected clients
        if (useCache && modify) {
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

        @OptIn(ExperimentalSerializationApi::class)
        public fun <R> useData(stack: ItemStack, use: (id: Int, data: T) -> R): R {
            if (stack.item != item) {
                throw Exception("Requires ${item}, got ${stack.item}")
            }
            if (stack.hasNbt() && stack.nbt!!.contains(nbtKey.toString())) {
                val nbt = NETWORK_CBOR.decodeFromByteArray<ItemNbt<T>>(stack.nbt!!.getByteArray(nbtKey.toString()))
                if (nbt.id == -1) {
                    return use(-1, if (nbt.data != null) nbt.data!! else defaultData())
                } else {
                    return use(nbt.id, if (clientCache.contains(nbt.id)) clientCache[nbt.id]!! else defaultData())

                }
            }

            return use(-1, defaultData())
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
        item, Identifier(prefix, "${suffix}-nbt"),
        Identifier(prefix, "${suffix}-persist"),
        persistIn,
        s2cPacket<Pair<Int, T>>(Identifier(prefix, "${suffix}-upd-pkt"), NETWORK_CBOR),
        s2cPacket<Map<Int, T>>(Identifier(prefix, "${suffix}-dump-pkt"), NETWORK_CBOR),
        defaultData
    )
}