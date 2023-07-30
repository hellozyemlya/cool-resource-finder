package hellozyemlya.resourcefinder.items.server

import hellozyemlya.common.items.ItemServerSide
import hellozyemlya.resourcefinder.items.FINDER_ID_NBT_KEY
import hellozyemlya.resourcefinder.items.FINDER_STATE_NBT_KEY
import hellozyemlya.resourcefinder.items.FinderItem
import hellozyemlya.resourcefinder.items.state.PersistentFinderState
import hellozyemlya.resourcefinder.items.state.network.FinderStateRequestPacket
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.serialization.generated.*
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.PersistentStateManager
import net.minecraft.world.World


class FinderItemServerSide(item: FinderItem) : ItemServerSide<FinderItem>(item) {
    private var server: MinecraftServer? = null
    private val persistentStateManager: PersistentStateManager
        get() = server!!
            .getWorld(World.OVERWORLD)!!
            .persistentStateManager

    init {
        ServerLifecycleEvents.SERVER_STARTED.register {
            server = it
        }

        ServerPlayNetworking.registerGlobalReceiver(FinderStateRequestPacket.PACKET_TYPE) { request: FinderStateRequestPacket, player: ServerPlayerEntity, _: PacketSender ->
            val packet = FinderStateUpdatePacket(getPersistentState(request.id))
            for (nearbyPlayer in PlayerLookup.tracking(player.world as ServerWorld, player.blockPos)) {
                ServerPlayNetworking.send(nearbyPlayer, packet)
            }
        }
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        val state = getPersistentState(getOrAllocateFinderId(stack))

        if(item.hasNbtState(stack)) {
            // merge state after crafting
            val nbtState = item.getNbtState(stack)
            state.targets.clear()
            state.scanList.clear()
            state.scanList.putAll(nbtState.scanList)
            state.markDirty()
            stack.orCreateNbt.remove(FINDER_STATE_NBT_KEY)
        }

        state.inventoryTick(entity as ServerPlayerEntity, selected)
    }

    private fun getState(stack: ItemStack): PersistentFinderState? {
        val id = getId(stack)

        if (id != -1) {
            return getPersistentState(id)
        }

        return null
    }

    fun writePersistentFinderStateToNbt(stack: ItemStack) {
        val state = getState(stack)

        if (state != null) {
            val stateNbt = NbtCompound()
            state.writeTo(stateNbt)
            stack.orCreateNbt.put(FINDER_STATE_NBT_KEY, stateNbt)
        }
    }

    private fun persistentStateKey(id: Int): String = "resource_finder$id"
    private fun getPersistentState(id: Int): PersistentFinderState {
        return persistentStateManager
            .getPersistentFinderStateOrCreate(persistentStateKey(id), id)
    }

    private fun getId(stack: ItemStack): Int {
        val nbt = stack.orCreateNbt
        if (nbt.contains(FINDER_ID_NBT_KEY)) {
            return nbt.getInt(FINDER_ID_NBT_KEY)
        }

        return -1
    }

    private fun getOrAllocateFinderId(stack: ItemStack): Int {
        var id = getId(stack)
        if (id == -1) {
            id = allocateFinderId()
            stack.orCreateNbt.putInt(FINDER_ID_NBT_KEY, id)
        }
        return id
    }

    private fun allocateFinderId(): Int {
        return persistentStateManager
            .getFinderIdAllocatorOrCreate("finder_id_allocator")
            .allocateId()
    }
}