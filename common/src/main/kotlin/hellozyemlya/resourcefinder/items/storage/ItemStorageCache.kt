package hellozyemlya.resourcefinder.items.storage

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.items.compass.NETWORK_CBOR
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.world.PersistentState
import net.minecraft.world.World
import net.silkmc.silk.network.packet.s2cPacket

@Serializable
data class DataStorage<T>(var nextId: Int = -1, var data: MutableMap<Int, T> = mutableMapOf())


abstract class ItemsCachePersistStateManager<T> : PersistentState() {
    abstract var nextId: Int

    abstract var data: MutableMap<Int, T>
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> itemsCachePersistStateManager(storage: DataStorage<T>): ItemsCachePersistStateManager<T> {
    return object : ItemsCachePersistStateManager<T>() {
        override var nextId: Int
            get() = storage.nextId
            set(value) {
                storage.nextId = value
                markDirty()
            }

        override var data: MutableMap<Int, T>
            get() = storage.data
            set(value) {
                storage.data = value
                markDirty()
            }

        override fun writeNbt(nbt: NbtCompound): NbtCompound {
            nbt.putByteArray("data", NETWORK_CBOR.encodeToByteArray(storage))
            return nbt
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> ServerWorld.getItemsCacheState(uniqueDataKey: String): ItemsCachePersistStateManager<T> {
    return this.persistentStateManager.getOrCreate(
        { compound ->
            val decoded = NETWORK_CBOR.decodeFromByteArray<DataStorage<T>>(compound.getByteArray("data"))
            itemsCachePersistStateManager(decoded)
        },
        { itemsCachePersistStateManager(DataStorage()) },
        "${uniqueDataKey}-persist"
    )
}


interface ItemStorageCache<T : Any> {
    fun getNextId(): Int
    fun getItemData(stack: ItemStack): T
    fun <R> modifyItemData(stack: ItemStack, use: (id: Int, data: T) -> Pair<Boolean, R>): R
    fun initClient()
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> itemStorageCache(
    item: Item,
    uniqueDataKey: String,
    noinline defaultFactory: () -> T
): ItemStorageCache<T> {
    val persistIn = World.OVERWORLD
    val updPacket = s2cPacket<Map<Int, T>>(Identifier(MOD_NAMESPACE, "${uniqueDataKey}-upd-packet"), NETWORK_CBOR)
    val cacheDumpPacket =
        s2cPacket<Map<Int, T>>(Identifier(MOD_NAMESPACE, "${uniqueDataKey}-cache-packet"), NETWORK_CBOR)
    var server: MinecraftServer? = null
    val nbtIdKey = "${uniqueDataKey}-nbt"
    var serverThread: Long? = null
    val serverCache: HashMap<Int, T> = hashMapOf()
    val clientCache: HashMap<Int, T> = hashMapOf()

    assert(item.maxCount == 1, { "ItemStorageCache requires maxCount = 1" })

    val isServer: () -> Boolean = { serverThread != null && serverThread == Thread.currentThread().id }
    val requireServer: () -> Unit = {
        require(server != null && isServer()) {
            "Expected to be executed in server"
        }
    }

    // remember server thread; load persisted data
    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted {
        serverThread = Thread.currentThread().id
        server = it
        serverCache.clear()
        val persistWorld = it.getWorld(persistIn)
        if (persistWorld != null) {
            val ps = persistWorld.getItemsCacheState<T>(uniqueDataKey)
            serverCache.putAll(ps.data)
        }
    })

    // ensure to send compass data cache to new client
    ServerPlayConnectionEvents.JOIN.register { handler: ServerPlayNetworkHandler,
                                               _: PacketSender,
                                               _: MinecraftServer ->
        cacheDumpPacket.send(serverCache, handler.player)
    }

    // manage update packet sending at the end of the server tick
    val updData = mutableMapOf<Int, T>()

    ServerTickEvents.END_SERVER_TICK.register { _ ->
        if (updData.isNotEmpty()) {
            updPacket.sendToAll(updData)
            val persistWorld = server!!.getWorld(persistIn)
            if (persistWorld != null) {
                val ps = server!!.getWorld(persistIn)!!.getItemsCacheState<T>(uniqueDataKey)
                ps.data = serverCache
            }
            updData.clear()
        }
    }

    return object : ItemStorageCache<T> {
        override fun getNextId(): Int {
            requireServer()
            val ps = server!!.getWorld(persistIn)!!.getItemsCacheState<T>(uniqueDataKey)
            return ++ps.nextId
        }

        override fun getItemData(stack: ItemStack): T {
            if (isServer()) {
                return modifyItemData(stack) { _, data -> false to data }
            } else {
                if (stack.item != item) {
                    throw Exception("Requires ${item}, got ${stack.item}")
                }
                if (stack.hasNbt() && stack.nbt!!.contains(nbtIdKey)) {
                    val itemId = stack.nbt!!.getInt(nbtIdKey)
                    return if (clientCache.contains(itemId)) clientCache[itemId]!! else defaultFactory()
                }
                return defaultFactory()
            }
        }

        override fun initClient() {
            cacheDumpPacket.receiveOnClient { packet, _ ->
                clientCache.clear()
                clientCache.putAll(packet)
            }

            updPacket.receiveOnClient { packet, _ ->
                clientCache.putAll(packet)
            }
        }

        override fun <R> modifyItemData(stack: ItemStack, use: (id: Int, data: T) -> Pair<Boolean, R>): R {
            requireServer()

            if (stack.item != item) {
                throw Exception("Requires ${item}, got ${stack.item}")
            }

            val itemId = if (stack.hasNbt() && stack.nbt!!.contains(nbtIdKey)) {
                stack.nbt!!.getInt(nbtIdKey)
            } else {
                val newId = getNextId()
                stack.orCreateNbt.putInt(nbtIdKey, newId)
                newId
            }

            val (cacheModified, itemData) = if (!serverCache.containsKey(itemId)) {
                val data = defaultFactory()
                serverCache[itemId] = data
                true to data
            } else {
                false to serverCache[itemId]!!
            }

            val (modified, result) = use(itemId, itemData)

            if (modified || cacheModified) {
                updData[itemId] = itemData
            }

            return result
        }
    }
}