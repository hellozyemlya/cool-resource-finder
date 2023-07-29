package hellozyemlya.common.items

import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

abstract class BaseSplitItem<TSelf>(settings: Settings?) : Item(settings) where TSelf : BaseSplitItem<TSelf> {
    private var client: ItemClientSide<TSelf>? = null
    private var server: ItemServerSide<TSelf>? = null

    public fun <TClient : ItemClientSide<TSelf>> setClientPart(factory: () -> TClient): TSelf {
        client = factory()
        return this as TSelf
    }

    public fun <TServer : ItemServerSide<TSelf>> setServerSide(factory: (self: TSelf) -> TServer): TSelf {
        server = factory(this as TSelf)
        return this as TSelf
    }

    public fun <TServer : ItemServerSide<TSelf>> getServerSide(): TServer {
        server.run {
            requireNotNull(this)
            return this as TServer
        }
    }

    public fun <TServer : ItemServerSide<TSelf>> withServerSide(block: TServer.() -> Unit) {
        (server as? TServer)?.block()
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        client?.appendTooltip(stack, world, tooltip, context)
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient) {
            client?.inventoryTick(stack, world, entity, slot, selected)
        } else {
            server?.inventoryTick(stack, world, entity, slot, selected)
        }
    }
}