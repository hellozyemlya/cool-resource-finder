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
    override fun transformMatrices(stack: ItemStack, matrices: MatrixStack) {
        val (resource, pos) = ResourceFinderCompassArrowItem.readArrowData(stack)
        val angle = getArrowAngle(null!!, pos)

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