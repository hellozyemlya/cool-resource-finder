package hellozyemlya.resourcefinder.items.server

import hellozyemlya.common.items.ItemServerSide
import hellozyemlya.resourcefinder.items.FinderItem
import hellozyemlya.resourcefinder.items.state.FinderIdAllocator
import hellozyemlya.resourcefinder.items.state.PersistentFinderState
import hellozyemlya.resourcefinder.items.state.network.FinderStateRequestPacket
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.serialization.generated.getFinderIdAllocatorOrCreate
import hellozyemlya.serialization.generated.getPersistentFinderStateOrCreate
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World


class FinderItemServerSide(item: FinderItem) : ItemServerSide<FinderItem>(item) {
    private var server: MinecraftServer? = null

    init {
        ServerLifecycleEvents.SERVER_STARTED.register {
            server = it
        }

        ServerPlayNetworking.registerGlobalReceiver(FinderStateRequestPacket.PACKET_TYPE) { request: FinderStateRequestPacket, player: ServerPlayerEntity, _: PacketSender ->
            ServerPlayNetworking.send(player, FinderStateUpdatePacket(getState(request.id)))
        }
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        getState(stack).inventoryTick(entity as ServerPlayerEntity, selected)
    }

    fun reallocateId(stack: ItemStack) {
        val id = allocateFinderId()
        stack.orCreateNbt.putInt("finder_id", id)
    }

    fun getState(stack: ItemStack): PersistentFinderState {
        return getState(getFinderId(stack))
    }

    fun getState(id: Int): PersistentFinderState {
        return server!!
            .getWorld(World.OVERWORLD)!!
            .persistentStateManager
            .getPersistentFinderStateOrCreate("resource_finder$id", id)
    }

    //    @Environment(EnvType.SERVER)
    private fun getFinderId(stack: ItemStack): Int {
        val nbt = stack.orCreateNbt
        if (nbt.contains("finder_id")) {
            return nbt.getInt("finder_id")
        }

        val id = allocateFinderId()
        nbt.putInt("finder_id", id)
        return id
    }

    //    @Environment(EnvType.SERVER)
    private fun allocateFinderId(): Int {
        return server!!
            .getWorld(World.OVERWORLD)!!
            .persistentStateManager
            .getFinderIdAllocatorOrCreate("finder_id_allocator")
            .allocateId()
    }
}