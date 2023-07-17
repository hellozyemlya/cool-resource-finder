package hellozyemlya.resourcefinder.items

import hellozyemlya.common.ClientItem
import hellozyemlya.common.getChildStacks
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.atan2

class ResourceFinderCompassArrowClientItem(item: Item) : ClientItem(item) {
    override fun transformHeldMatrices(entity: LivingEntity, stack: ItemStack, renderMode: ModelTransformationMode, leftHanded: Boolean, matrices: MatrixStack, light: Int) {
        val pos = stack.arrowTarget
        val idx = stack.arrowIndex
        val angle = getArrowAngle(entity, pos)

        matrices.translate(0.5, 0.5 + (idx * 0.01), 0.5)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle))
        matrices.translate(-0.5, -0.5, -0.5)
    }

    override fun isOverrideModelColors(): Boolean {
        return true
    }


    override fun getColor(stack: ItemStack, colorIndex: Int): Int {
        return stack.arrowResource.color
    }

    companion object {
        private fun getAngleTo(entity: Entity, pos: BlockPos): Double {
            val vec3d = Vec3d.ofCenter(pos)
            return atan2(vec3d.getZ() - entity.z, vec3d.getX() - entity.x) / 6.2831854820251465
        }

        private fun getBodyYaw(entity: Entity): Double {
            return MathHelper.floorMod((entity.bodyYaw / 360.0f).toDouble(), 1.0)
        }

        private fun getArrowAngle(entity: LivingEntity, pos: BlockPos): Float {
            val d = getAngleTo(entity, pos)
            val e = getBodyYaw(entity)
            val a = (0.5 - (e - 0.25 - d)).toFloat()

            return 180f - (360f * a)
        }
    }
}