package hellozyemlya.resourcefinder.items.storage

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.items.compass.NETWORK_CBOR
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.silkmc.silk.network.packet.ServerToClientPacketDefinition
import net.silkmc.silk.persistence.compoundKey
import net.silkmc.silk.persistence.persistentCompound

/**
 * Represents client-server synchronized storage of data, associated with item stack. Requires stack max size to be 1.
 */
@OptIn(ExperimentalSerializationApi::class)
class ItemStorageCache<T : Any>(
    private val item: Item,
    uniqueDataKey: String,
    private val dataMapSerializer: KSerializer<Map<Int, T>>,
    dataRecordSerializer: KSerializer<Pair<Int, T>>,
    private val defaultData: () -> T,
) {
    private val persistIn = World.OVERWORLD
    private val updPacket: ServerToClientPacketDefinition<Pair<Int, T>> = ServerToClientPacketDefinition(
        Identifier(MOD_NAMESPACE, "${uniqueDataKey}-upd-packet"),
        NETWORK_CBOR,
        dataRecordSerializer
    )
    private val cacheDumpPacket: ServerToClientPacketDefinition<Map<Int, T>> = ServerToClientPacketDefinition(
        Identifier(MOD_NAMESPACE, "${uniqueDataKey}-cache-packet"),
        NETWORK_CBOR,
        dataMapSerializer
    )
    private var server: MinecraftServer? = null
    private val nbtIdKey = "${uniqueDataKey}-nbt"
    private val persistCompoundKey = compoundKey<ByteArray>(Identifier(MOD_NAMESPACE, "${uniqueDataKey}-persist"))
    private val idCompoundKey = compoundKey<Int>(Identifier(MOD_NAMESPACE, "${uniqueDataKey}-id"))
    private var serverThread: Long? = null

    init {
        assert(item.maxCount == 1, { "ItemStorageCache requires maxCount = 1" })


        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
            serverThread = Thread.currentThread().id
            server = it
            val persistWorld = it.getWorld(persistIn)
            if (persistWorld != null) {
                val possibleCache = persistWorld.persistentCompound[persistCompoundKey]
                if (possibleCache != null) {
                    serverCache.clear()
                    serverCache.putAll(NETWORK_CBOR.decodeFromByteArray(dataMapSerializer, possibleCache))
                }
                nextId = persistWorld.persistentCompound[idCompoundKey] ?: -1
            }
        })

        ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler,
                                                   _: PacketSender,
                                                   _: MinecraftServer ->
            cacheDumpPacket.send(serverCache, handler.player)
        }
    }

    private val serverCache: HashMap<Int, T> = hashMapOf()
    private val clientCache: HashMap<Int, T> = hashMapOf()
    private var nextId: Int = -1

    private fun getNextId(): Int {
        requireServer()

        nextId++
        server!!.getWorld(World.OVERWORLD)!!.persistentCompound[idCompoundKey] = nextId
        server!!.getWorld(World.OVERWORLD)!!.persistentStateManager.save()
        return nextId
    }

    //    persistWorld.persistentCompound[idCompoundKey]
    private fun requireServer() {
        require(server != null && isServer()) {
            "Expected to be executed in server"
        }
    }

    private fun isServer(): Boolean {
        return serverThread != null && serverThread == Thread.currentThread().id
    }

    /**
     * Returns data for given item stack. It is safe to call that on client and server logical sides.
     */
    fun getItemData(stack: ItemStack): T {
        if (isServer()) {
            return modifyItemData(stack) { _, data -> false to data }
        } else {
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

    fun <R> modifyItemData(stack: ItemStack, use: (id: Int, data: T) -> Pair<Boolean, R>): R {
        requireServer()

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

        if (!serverCache.containsKey(itemId)) {
            serverCache[itemId] = defaultData()
        }
        val itemData = serverCache[itemId]!!

        val (modified, result) = use(itemId, itemData)

        // send updated data for item to all currently connected clients
        if (modified) {
            updPacket.sendToAll(itemId to itemData)
            // TODO better persistence
            val persistWorld = server!!.getWorld(persistIn)
            if (persistWorld != null) {
                persistWorld.persistentCompound[persistCompoundKey] =
                    NETWORK_CBOR.encodeToByteArray(dataMapSerializer, serverCache)
                persistWorld.persistentStateManager.save()
            }

        }

        return result
    }

    fun initClient() {
        cacheDumpPacket.receiveOnClient { packet, _ ->
            clientCache.clear()
            clientCache.putAll(packet)
        }

        updPacket.receiveOnClient { (id, data), _ ->
            clientCache[id] = data
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> createItemStorageCache(
    item: Item,
    uniqueDataKey: String,
    noinline defaultFactory: () -> T
): ItemStorageCache<T> {
    return ItemStorageCache(
        item,
        uniqueDataKey,
        NETWORK_CBOR.serializersModule.serializer<Map<Int, T>>(),
        NETWORK_CBOR.serializersModule.serializer<Pair<Int, T>>(),
        defaultFactory
    )
}