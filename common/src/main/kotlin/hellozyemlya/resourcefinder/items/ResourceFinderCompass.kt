package hellozyemlya.resourcefinder.items

import hellozyemlya.common.items.BaseClientAwareItem
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.items.state.FinderIdAllocator
import hellozyemlya.resourcefinder.items.state.FinderState
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.StringHelper
import net.minecraft.world.World

class ResourceFinderCompass(settings: Settings) : BaseClientAwareItem(settings) {
    public var server: MinecraftServer? = null

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

//    @Environment(EnvType.SERVER)
    fun getServerState(stack: ItemStack): FinderState {
        val id = getFinderId(stack)

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

