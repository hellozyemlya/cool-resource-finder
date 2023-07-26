package hellozyemlya.resourcefinder.items

import hellozyemlya.common.items.BaseClientAwareItem
import hellozyemlya.resourcefinder.items.state.FinderIdAllocator
import hellozyemlya.resourcefinder.items.state.FinderState
import hellozyemlya.resourcefinder.items.state.network.FinderStateRequestPacket
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Hand
import net.minecraft.world.World

class ResourceFinderCompass(settings: Settings) : BaseClientAwareItem(settings) {
    public var server: MinecraftServer? = null

    init {
        ServerPlayNetworking.registerGlobalReceiver(FinderStateRequestPacket.PACKET_TYPE) {
                request: FinderStateRequestPacket, player: ServerPlayerEntity, _: PacketSender ->
            ServerPlayNetworking.send(player, FinderStateUpdatePacket(getServerState(request.id)))
        }
    }

    companion object {
        const val DEFAULT_SCAN_TIMEOUT = 10
    }

    override fun allowNbtUpdateAnimation(
        player: PlayerEntity?,
        hand: Hand?,
        oldStack: ItemStack?,
        newStack: ItemStack?
    ): Boolean {
        return false
    }


    override fun inventoryTick(stack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean) {
        if (stack != null && world != null && entity is ServerPlayerEntity) {
            if (!world.isClient) {
                getServerState(stack).inventoryTick(entity, selected)
            }
        }
    }

    fun reallocateId(stack: ItemStack) {
        val id = allocateFinderId()
        stack.orCreateNbt.putInt("finder_id", id)
    }

//    @Environment(EnvType.SERVER)
    fun getServerState(stack: ItemStack): FinderState {
        return getServerState(getFinderId(stack))
    }

    fun getServerState(id: Int): FinderState {
        return server!!
            .getWorld(World.OVERWORLD)!!
            .persistentStateManager
            .getOrCreate(FinderState::fromNbt, {FinderState(id)},"resource_finder$id")
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
            .getOrCreate(FinderIdAllocator::fromNbt, ::FinderIdAllocator, "finder_id_map")
            .allocateId()
    }

}

