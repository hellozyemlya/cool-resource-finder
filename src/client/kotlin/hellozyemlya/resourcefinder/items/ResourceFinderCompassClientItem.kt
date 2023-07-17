package hellozyemlya.resourcefinder.items

import hellozyemlya.common.BakedModelEx
import hellozyemlya.common.ClientItem
import hellozyemlya.common.GuiItemRenderer
import hellozyemlya.common.MatrixTransform
import hellozyemlya.resourcefinder.ResourceFinder
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
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

    private fun prepareArrowModel(entity: LivingEntity?, pos: BlockPos?, idx: Int) {
        if(entity != null && pos != null) {
            val angle = getArrowAngle(entity, pos)
            (arrowModel as BakedModelEx).matrixTransform = MatrixTransform { matrixStack ->
                matrixStack.translate(0.5, 0.5 + (idx * 0.01), 0.5)
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle))
                matrixStack.translate(-0.5, -0.5, -0.5)
            }
        } else {
            (arrowModel as BakedModelEx).matrixTransform = MatrixTransform { matrixStack ->
                matrixStack.translate(0.5, 0.5 + (idx * 0.01), 0.5)
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - 25 * idx))
                matrixStack.translate(-0.5, -0.5, -0.5)
            }
        }

    }

    override fun afterHeldItemRendered(
        entity: LivingEntity,
        stack: ItemStack,
        renderMode: ModelTransformationMode,
        leftHanded: Boolean,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int
    ) {
        stack.getTargetList().forEachIndexed { idx, targetRecord ->
            prepareArrowModel(entity, targetRecord.target, idx)
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

    override fun afterGuiItemRendered(
        itemRenderer: GuiItemRenderer,
        matrices: MatrixStack,
        entity: LivingEntity?,
        world: World?,
        stack: ItemStack,
        x: Int,
        y: Int,
        seed: Int,
        depth: Int
    ) {
        if(entity != null && (entity.offHandStack === stack || entity.mainHandStack === stack)) {
            stack.getTargetList().forEachIndexed { idx, targetRecord ->
                prepareArrowModel(entity, targetRecord.target, idx)
                itemRenderer.render(matrices, entity, world, arrowStack, x, y, seed, depth)
            }
        } else {
            stack.getScanList().forEachIndexed { idx, scanRecord ->
                prepareArrowModel(null, null, idx)
                itemRenderer.render(matrices, entity, world, arrowStack, x, y, seed, depth)
            }
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