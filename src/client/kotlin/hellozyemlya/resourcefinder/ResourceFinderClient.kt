package hellozyemlya.resourcefinder

import hellozyemlya.common.pushPop
import hellozyemlya.resourcefinder.items.getScanList
import hellozyemlya.resourcefinder.items.getTargetList
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.atan2

object ResourceFinderClient : ClientModInitializer {
    private val ARROW_ITEM_ID = Identifier(ResourceFinder.MOD_NAMESPACE, "resource_finder_compass_arrow")
    private val ARROW_MODEL_ID = ModelIdentifier(ARROW_ITEM_ID, "inventory")
    private val BASE_ITEM_ID = Identifier(ResourceFinder.MOD_NAMESPACE, "resource_finder_compass_base")
    private val BASE_MODEL_ID = ModelIdentifier(BASE_ITEM_ID, "inventory")
    private val INDICATOR_ITEM_ID = Identifier(ResourceFinder.MOD_NAMESPACE, "resource_finder_compass_indicator")
    private val INDICATOR_MODEL_ID = ModelIdentifier(INDICATOR_ITEM_ID, "inventory")

    private var quadColorOverride: Int = -1
    private var lastEntity: LivingEntity? = null

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

    private fun renderPositionedArrows(
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
            quadColorOverride = targetRecord.color
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
                        stack,
                        ModelTransformationMode.NONE,
                        false,
                        matrices,
                        vertexConsumers,
                        light,
                        overlay,
                        MinecraftClient.getInstance().bakedModelManager.getModel(ARROW_MODEL_ID)
                )
            }

            if (mode != ModelTransformationMode.GUI) {
                matrices.pushPop {
                    val renderIndicator = when {
                        entity.blockPos.y > blockPost.y -> {
                            matrices.translate(0f, 0f, -++topIdx * 0.013f)
                            true
                        }

                        entity.blockPos.y < blockPost.y -> {
                            matrices.multiply(
                                    RotationAxis.POSITIVE_Y.rotation(Math.PI.toFloat())
                            )
                            matrices.translate(0f, 0f, -++botIdx * 0.013f)
                            true
                        }

                        else -> false
                    }
                    if (renderIndicator) {
                        renderer.renderItem(
                                stack,
                                ModelTransformationMode.NONE,
                                false,
                                matrices,
                                vertexConsumers,
                                light,
                                overlay,
                                MinecraftClient.getInstance().bakedModelManager.getModel(INDICATOR_MODEL_ID)
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
            quadColorOverride = scanRecord.color
            matrices.push()
            matrices.translate(0f, (idx * 0.01f), 0f)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25f * idx))
            // TODO set color here somehow
            renderer.renderItem(
                    stack,
                    ModelTransformationMode.NONE,
                    false,
                    matrices,
                    vertexConsumers,
                    light,
                    overlay,
                    MinecraftClient.getInstance().bakedModelManager.getModel(ARROW_MODEL_ID)
            )
            matrices.pop()
        }
    }

    private fun renderCompass(stack: ItemStack, mode: ModelTransformationMode, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
        val renderer = MinecraftClient.getInstance().itemRenderer

        // reset quad color
        quadColorOverride = -1

        // reset matrices translated by ItemRenderer
        matrices.translate(0.5f, 0.5f, 0.5f)

        // render compass base model
        renderer.renderItem(
                stack,
                ModelTransformationMode.NONE,
                false,
                matrices,
                vertexConsumers,
                light,
                overlay,
                MinecraftClient.getInstance().bakedModelManager.getModel(BASE_MODEL_ID)
        )

        val entity = lastEntity

        if (renderPositionedArrows(mode, entity, stack)) {
            renderPositionedArrows(
                    entity,
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

    @OptIn(ExperimentalContracts::class)
    private fun renderPositionedArrows(mode: ModelTransformationMode, entity: LivingEntity?, stack: ItemStack): Boolean {
        contract {
            returns(true) implies (entity != null)
        }

        return when (mode) {
            ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, ModelTransformationMode.FIRST_PERSON_LEFT_HAND, ModelTransformationMode.HEAD, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND -> {
                entity != null
            }

            ModelTransformationMode.GUI -> {
                if (entity != null) {
                    entity.mainHandStack === stack || entity.offHandStack === stack
                } else {
                    false
                }
            }

            else -> {
                false
            }
        }
    }

    override fun onInitializeClient() {
        // load compass parts models
        ModelLoadingPlugin.register { ctx ->
            ctx.addModels(BASE_MODEL_ID, INDICATOR_MODEL_ID, ARROW_MODEL_ID)
        }

        // capture LivingEntity
        ModelPredicateProviderRegistry.register(ResourceFinder.RESOURCE_FINDER_ITEM, Identifier("hack")) { _: ItemStack, _: ClientWorld?, livingEntity: LivingEntity?, _: Int ->
            lastEntity = livingEntity
            0f
        }

        // override quad colors
        ColorProviderRegistry.ITEM.register(
                { _: ItemStack, _: Int -> quadColorOverride },
                ResourceFinder.RESOURCE_FINDER_ITEM
        )

        // use custom render function for compass
        BuiltinItemRendererRegistry.INSTANCE.register(ResourceFinder.RESOURCE_FINDER_ITEM, ::renderCompass)
    }
}