package hellozyemlya.resourcefinder

import hellozyemlya.common.pushPop
import hellozyemlya.resourcefinder.items.getScanList
import hellozyemlya.resourcefinder.items.getTargetList
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import kotlin.math.atan2

object ResourceFinderClient : ClientModInitializer {
    private val arrowsCache: MutableMap<Int, ItemStack> = HashMap()
    private val indicatorsCache: MutableMap<Int, ItemStack> = HashMap()

    private fun arrowFromColor(color: Int): ItemStack {
        return if (arrowsCache.containsKey(color)) {
            arrowsCache[color]!!
        } else {
            val arrowStack = ResourceFinder.RESOURCE_FINDER_ARROW_ITEM.defaultStack
            arrowStack.orCreateNbt.putInt("color", color)
            arrowsCache[color] = arrowStack
            arrowStack
        }
    }

    private fun indicatorFromColor(color: Int): ItemStack {
        return if (indicatorsCache.containsKey(color)) {
            indicatorsCache[color]!!
        } else {
            val indicatorStack = ResourceFinder.RESOURCE_FINDER_INDICATOR_ITEM.defaultStack
            indicatorStack.orCreateNbt.putInt("color", color)
            indicatorsCache[color] = indicatorStack
            indicatorStack
        }
    }

    private fun getAngleTo(entity: Entity, pos: BlockPos): Double {
        val vec3d = Vec3d.ofCenter(pos)

        val sX = MathHelper.lerp(MinecraftClient.getInstance().tickDelta.toDouble(), entity.prevX, entity.x)
        val sZ = MathHelper.lerp(MinecraftClient.getInstance().tickDelta.toDouble(), entity.prevZ, entity.z)

        return atan2(vec3d.getZ() - sZ, vec3d.getX() - sX) / 6.2831854820251465
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
    private fun renderArrowsOnEntity(
        entity: LivingEntity,
        stack: ItemStack,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        mode: ModelTransformationMode,
        light: Int,
        overlay: Int,
        renderer: ItemRenderer
    ) {
        var topIdx = -1
        var botIdx = -1
        stack.getTargetList().forEachIndexed { idx, targetRecord ->
            val blockPost = targetRecord.target
            matrices.pushPop {
                matrices.translate(0f, (idx * 0.01f), 0f)
                matrices.multiply(
                    RotationAxis.POSITIVE_Y.rotationDegrees(
                        getArrowAngle(
                            entity,
                            blockPost
                        )
                    )
                )
                renderer.renderItem(
                    arrowFromColor(targetRecord.resourceEntry.color),
                    ModelTransformationMode.NONE,
                    light,
                    overlay,
                    matrices,
                    vertexConsumers,
                    null,
                    0
                )
            }

            if(mode != ModelTransformationMode.GUI) {
                matrices.pushPop {
                    val renderIndicator = when {
                        entity.blockPos.y > blockPost.y -> {
                            matrices.translate(0f, 0f, - ++topIdx * 0.013f)
                            true
                        }
                        entity.blockPos.y < blockPost.y -> {
                            matrices.multiply(
                                RotationAxis.POSITIVE_Y.rotation(Math.PI.toFloat())
                            )
                            matrices.translate(0f, 0f, - ++botIdx * 0.013f)
                            true
                        }
                        else -> false
                    }
                    if(renderIndicator) {
                        renderer.renderItem(
                            indicatorFromColor(targetRecord.resourceEntry.color),
                            ModelTransformationMode.NONE,
                            light,
                            overlay,
                            matrices,
                            vertexConsumers,
                            null,
                            0
                        )
                    }
                }
            }
        }
    }

    private fun renderArrowsPreview(
        stack: ItemStack,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        renderer: ItemRenderer
    ) {
        stack.getScanList().forEachIndexed { idx, scanRecord ->
            matrices.push()
            matrices.translate(0f, (idx * 0.01f), 0f)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25f * idx))
            renderer.renderItem(
                arrowFromColor(scanRecord.resourceEntry.color),
                ModelTransformationMode.NONE,
                light,
                overlay,
                matrices,
                vertexConsumers,
                null,
                0
            )
            matrices.pop()
        }
    }

    override fun onInitializeClient() {
        BuiltinItemRendererRegistry.INSTANCE.register(ResourceFinder.RESOURCE_FINDER_ITEM) { stack: ItemStack, mode: ModelTransformationMode, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int ->
            val renderer = MinecraftClient.getInstance().itemRenderer

            matrices.translate(0.5f, 0.5f, 0.5f)
            renderer.renderItem(
                ResourceFinder.RESOURCE_FINDER_BASE_ITEM.defaultStack,
                ModelTransformationMode.NONE,
                light,
                overlay,
                matrices,
                vertexConsumers,
                null,
                0
            )

            val renderEntities = ItemStackWithRenderLivingEntityList.getRenderLivingEntityList(stack)

            if (renderEntities.isEmpty) {
                renderArrowsPreview(stack, matrices, vertexConsumers, light, overlay, renderer)
            } else {
                val entity = renderEntities.top()
                val renderWithEntity = when (mode) {
                    ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, ModelTransformationMode.FIRST_PERSON_LEFT_HAND, ModelTransformationMode.HEAD, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND -> {
                        true
                    }

                    ModelTransformationMode.GUI -> {
                        entity.mainHandStack === stack || entity.offHandStack === stack
                    }

                    else -> {
                        false
                    }
                }

                if (renderWithEntity) {
                    renderArrowsOnEntity(
                        renderEntities.top(),
                        stack,
                        matrices,
                        vertexConsumers,
                        mode,
                        light,
                        overlay,
                        renderer
                    )
                } else {
                    renderArrowsPreview(stack, matrices, vertexConsumers, light, overlay, renderer)
                }
            }
        }

        ColorProviderRegistry.ITEM.register(
            { stack, _ -> stack.orCreateNbt.getInt("color") },
            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM
        )

        ColorProviderRegistry.ITEM.register(
            { stack, _ -> stack.orCreateNbt.getInt("color") },
            ResourceFinder.RESOURCE_FINDER_INDICATOR_ITEM
        )

        TracksRenderLivingEntity.setTracksRenderLivingEntity(ResourceFinder.RESOURCE_FINDER_ITEM, true)
    }
}