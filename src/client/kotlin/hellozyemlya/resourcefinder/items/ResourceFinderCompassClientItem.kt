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
    public val LOGGER: Logger = LoggerFactory.getLogger("cool-resource-finder")

    val arrowModel: BakedModel by lazy {
        MinecraftClient.getInstance().bakedModelManager.getModel(ResourceFinderClient.ARROW_MODEL_ID)
    }

    override fun afterItemRendered(
        stack: ItemStack,
        renderMode: ModelTransformationMode,
        leftHanded: Boolean,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        model: BakedModel
    ) {
        val player = MinecraftClient.getInstance().player
        if(player != null) {
            val mainHandStack = player.mainHandStack
            val offHandStack = player.offHandStack
            LOGGER.info("BEGIN: $renderMode, $overlay, ${System.identityHashCode(stack)}")
            if(mainHandStack != null)
                LOGGER.info("main hand ${System.identityHashCode(mainHandStack)}")
            if(offHandStack != null)
                LOGGER.info("main hand ${System.identityHashCode(offHandStack)}")
            if(offHandStack != null)
                LOGGER.info("render stack ${System.identityHashCode(stack)}")
            var rendered = false
            if(mainHandStack === stack || offHandStack === stack) {
                LOGGER.info(" ... rendering ${System.identityHashCode(stack)} rendering")
                stack.getTargetList().forEachIndexed { idx, targetRecord ->

                    val angle = getArrowAngle(player, targetRecord.target)
                    (arrowModel as BakedModelEx).matrixTransform = MatrixTransform { matrixStack ->
                        matrixStack.translate(0.5, 0.5 + (idx * 0.01), 0.5)
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle))
                        matrixStack.translate(-0.5, -0.5, -0.5)
                    }
                    MinecraftClient
                        .getInstance()
                        .itemRenderer.renderItem(
                            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM.defaultStack,
                            renderMode,
                            leftHanded,
                            matrices,
                            vertexConsumers,
                            light,
                            overlay,
                            arrowModel
                        )
                }
                rendered = true
            }

            LOGGER.info("END: $renderMode, $rendered, $overlay, ${System.identityHashCode(stack)}")
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