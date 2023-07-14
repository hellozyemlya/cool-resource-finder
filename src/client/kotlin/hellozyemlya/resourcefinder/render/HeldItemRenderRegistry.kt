package hellozyemlya.resourcefinder.render

import hellozyemlya.resourcefinder.mixin.client.render.BakedItemModelRenderer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import java.util.*

interface HeldItemRenderRegistry {
    companion object {
        @JvmStatic
        val INSTANCE : HeldItemRenderRegistry = HeldItemRendererRegistryImpl()
    }
    fun register(item: ItemConvertible, renderer: HeldItemRenderer)
    operator fun get(item: ItemConvertible): HeldItemRenderer?
}


internal class HeldItemRendererRegistryImpl : HeldItemRenderRegistry {
    private val renderers: MutableMap<ItemConvertible, HeldItemRenderer> =
        HashMap<ItemConvertible, HeldItemRenderer>()
    override fun register(item: ItemConvertible, renderer: HeldItemRenderer) {
        require(
            renderers.putIfAbsent(
                item.asItem(),
                renderer
            ) == null
        ) { "Item " + Registries.ITEM.getId(item.asItem()) + " already has a builtin renderer!" }
    }

    override operator fun get(item: ItemConvertible): HeldItemRenderer? {
        Objects.requireNonNull(item.asItem(), "item is null")
        return renderers[item]
    }
}


interface HeldItemRenderer {
    fun render(
        renderer: BakedItemModelRenderer,
        entity: LivingEntity,
        clientWorld: ClientWorld,
        stack: ItemStack,
        light: Int,
        overlay: Int,
        seed: Int,
        matrices: MatrixStack,
        vertices: VertexConsumer
    )
}

