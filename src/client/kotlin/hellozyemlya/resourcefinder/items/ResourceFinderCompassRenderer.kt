package hellozyemlya.resourcefinder.items

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.mixin.client.render.BakedItemModelRenderer
import hellozyemlya.resourcefinder.render.HeldItemRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2

class ResourceFinderCompassRenderer : HeldItemRenderer {


    companion object {
        private const val COMPASS_3D_ARROW_COLOR_KEY = "compass3d.arrow.color"
        private val colorLessArrowItemStack: ItemStack = ItemStack(ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
        private val colorToArrowStack: MutableMap<Int, ItemStack> = HashMap()

        private fun getArrowStackFromColor(color: Int): ItemStack? {
            return if (colorToArrowStack.containsKey(color)) {
                colorToArrowStack[color]
            } else {
                val result = ItemStack(ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
                result.getOrCreateNbt().putInt(COMPASS_3D_ARROW_COLOR_KEY, color)
                colorToArrowStack[color] = result
                result
            }
        }

        private fun getArrowModel(seed: Int): BakedModel {
            return MinecraftClient.getInstance().itemRenderer.getModel(colorLessArrowItemStack, null, null, seed)
        }

        fun getColor(stack: ItemStack, tintIndex: Int): Int {
            return stack.getOrCreateNbt().getInt(COMPASS_3D_ARROW_COLOR_KEY)
        }

        private fun getAngleTo(entity: Entity, pos: BlockPos): Double {
            val vec3d = Vec3d.ofCenter(pos)
            return atan2(vec3d.getZ() - entity.z, vec3d.getX() - entity.x) / 6.2831854820251465
        }

        private fun getBodyYaw(entity: Entity): Double {
            return MathHelper.floorMod((entity.bodyYaw / 360.0f).toDouble(), 1.0)
        }
    }

    override fun render(
        renderer: BakedItemModelRenderer,
        entity: LivingEntity,
        clientWorld: ClientWorld,
        stack: ItemStack,
        light: Int,
        overlay: Int,
        seed: Int,
        matrices: MatrixStack,
        vertices: VertexConsumer
    ) {
        val arrowModel = getArrowModel(seed)
        var offset = 0.0

        ResourceFinderCompass.PositionNbt(stack).forEach { position ->
            val arrowItemStack = getArrowStackFromColor(position.entry.color)

            // calculate rotation angle
            val d = getAngleTo(entity, position.position)
            val e = getBodyYaw(entity)
            val a = (0.5 - (e - 0.25 - d)).toFloat()
            matrices.push()
            // rotate matrix for arrow model
            matrices.translate(0.5, 0.5 + offset, 0.5)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(360 * a)))
            matrices.translate(-0.5, -0.5, -0.5)

            // render arrow model
            renderer.renderBakedModel(arrowModel, arrowItemStack, light, overlay, matrices, vertices)
            matrices.pop()
            offset += 0.01
        }
    }
}