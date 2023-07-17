package hellozyemlya.resourcefinder.items

import hellozyemlya.common.ClientItem
import hellozyemlya.common.getChildStacks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ResourceFinderCompassClientItem(item: Item) : ClientItem(item) {

    override fun renderHeld(
        entity: LivingEntity,
        stack: ItemStack,
        renderMode: ModelTransformationMode,
        leftHanded: Boolean,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        super.renderHeld(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, light)
        stack.getChildStacks("arrows").forEach {
            MinecraftClient
                .getInstance()
                .entityRenderDispatcher
                .heldItemRenderer
                .renderItem(entity, it, renderMode, leftHanded, matrices, vertexConsumers, light)
        }
    }
}