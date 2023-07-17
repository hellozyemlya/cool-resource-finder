package hellozyemlya.resourcefinder.items

import hellozyemlya.common.BakedModelEx
import hellozyemlya.common.ClientItem
import hellozyemlya.common.MatrixTransform
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceFinderClient
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.atan2

class ResourceFinderCompassClientItem(item: Item) : ClientItem(item) {
    private val arrowModel: BakedModel by lazy {
        MinecraftClient.getInstance().itemRenderer.getModel(
            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM.defaultStack,
            null,
            null,
            0
        )
    }

    private val arrowStack: ItemStack by lazy {
        ResourceFinder.RESOURCE_FINDER_ARROW_ITEM.defaultStack
    }

    override fun afterHeldItemRenderer(
        entity: LivingEntity,
        stack: ItemStack,
        renderMode: ModelTransformationMode,
        leftHanded: Boolean,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        stack.getTargetList().forEachIndexed { idx, targetRecord ->
            val angle = getArrowAngle(entity, targetRecord.target)
            (arrowModel as BakedModelEx).matrixTransform = MatrixTransform { matrixStack ->
                matrixStack.translate(0.5, 0.5 + (idx * 0.01), 0.5)
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle))
                matrixStack.translate(-0.5, -0.5, -0.5)
            }
            MinecraftClient
                .getInstance()
                .entityRenderDispatcher.heldItemRenderer.renderItem(
                    entity,
                    arrowStack,
                    renderMode,
                    leftHanded,
                    matrices,
                    vertexConsumers,
                    light
                )
        }
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