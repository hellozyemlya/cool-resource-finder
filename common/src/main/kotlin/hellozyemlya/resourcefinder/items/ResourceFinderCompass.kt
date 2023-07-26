package hellozyemlya.resourcefinder.items

import hellozyemlya.common.getOrCreate
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.items.state.FinderIdAllocator
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Hand
import net.minecraft.util.StringHelper
import net.minecraft.world.World

class ResourceFinderCompass(settings: Settings) : Item(settings) {
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
        if (stack != null && world != null && entity is PlayerEntity) {
            if (!world.isClient) {
                getState(stack).inventoryTick(entity, selected)
            }
        }
    }

    public fun getState(stack: ItemStack): ResourceFinderState {
        val server = this.server

        requireNotNull(server)

        val nbt = stack.orCreateNbt
        val stateManager = server.getFinderStateManager()

        if (nbt.contains("finder_id")) {
            return stateManager.getOrCreate(nbt.getInt("finder_id"), stack)
        }

        val id = allocateFinderId()
        nbt.putInt("finder_id", id)
        return stateManager.getOrCreate(id, stack)
    }

    private fun allocateFinderId(): Int {
        return server!!
            .getWorld(World.OVERWORLD)!!
            .persistentStateManager
            .getOrCreate("finder_id_map", ::FinderIdAllocator)
            .allocateId()
    }

}

public fun MinecraftServer.getFinderStateManager(): ResourceFinderStateManager {
    return ResourceFinderStateManager
}


