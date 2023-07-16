package hellozyemlya.resourcefinder.items

import hellozyemlya.common.ClientItem
import hellozyemlya.common.getChildStacks
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class ResourceFinderCompassClientItem(item: Item) : ClientItem(item) {

    override fun renderHeld(entity: LivingEntity, itemStack: ItemStack, renderMode: ModelTransformationMode, leftHanded: Boolean, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, world: World, light: Int, overlay: Int, seed: Int) {
        super.renderHeld(entity, itemStack, renderMode, leftHanded, matrices, vertexConsumers, world, light, overlay, seed)
        itemStack.getChildStacks(ResourceFinderCompass.ARROWS_STACKS_KEY).forEach {
            arrowItemStack ->
            // TODO render arrows itemStack stacks
        }
    }
}