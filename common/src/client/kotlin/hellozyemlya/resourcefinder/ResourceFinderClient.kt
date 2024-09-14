package hellozyemlya.resourcefinder

import hellozyemlya.common.pushPop
import hellozyemlya.compat.client.compatTickDelta
import hellozyemlya.compat.compatGet
import hellozyemlya.compat.compatGetOrDefault
import hellozyemlya.compat.registries.registerFakeModel
import hellozyemlya.compat.registries.transformTintRgb
import hellozyemlya.resourcefinder.items.CompassComponents
import hellozyemlya.resourcefinder.items.ScanMode
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
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
import kotlin.jvm.optionals.getOrNull
import kotlin.math.atan2

@Suppress("unused")
object ResourceFinderClient : ClientModInitializer {
    private val ARROW_MODEL_ID = registerFakeModel(MOD_NAMESPACE, "resource_finder_compass_arrow")
    private val BASE_MODEL_ID = registerFakeModel(MOD_NAMESPACE, "resource_finder_compass_base")
    private val INDICATOR_MODEL_ID = registerFakeModel(MOD_NAMESPACE, "resource_finder_compass_indicator")
    private val ANTENNAS_MODEL_ID = registerFakeModel(MOD_NAMESPACE, "resource_finder_compass_antennas")

    private var quadColorOverride: Int = -1
    private var lastEntity: LivingEntity? = null

    private fun getAngleTo(entity: Entity, pos: BlockPos): Double {
        val vec3d = Vec3d.ofCenter(pos)

        val sX = MathHelper.lerp(MinecraftClient.getInstance().compatTickDelta.toDouble(), entity.prevX, entity.x)
        val sZ = MathHelper.lerp(MinecraftClient.getInstance().compatTickDelta.toDouble(), entity.prevZ, entity.z)

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
        val scanRecords = stack.compatGetOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, mapOf()).values
        scanRecords.forEachIndexed { idx, targetRecord ->
            val blockPos = targetRecord.target.getOrNull() ?: return@forEachIndexed
            quadColorOverride = targetRecord.color
            matrices.pushPop {
                matrices.translate(0f, (idx * 0.01f), 0f)
                matrices.multiply(
                    RotationAxis.POSITIVE_Y.rotationDegrees(
                        getArrowAngle(
                            entity,
                            blockPos
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
                        entity.blockPos.y > blockPos.y -> {
                            matrices.translate(0f, 0f, -++topIdx * 0.013f)
                            true
                        }

                        entity.blockPos.y < blockPos.y -> {
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
        val scanRecords = stack.compatGetOrDefault(CompassComponents.SCAN_TARGETS_COMPONENT, mapOf()).values
        scanRecords.forEachIndexed { idx, scanRecord ->
            quadColorOverride = scanRecord.color
            matrices.push()
            matrices.translate(0f, (idx * 0.01f), 0f)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25f * idx))
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

    private fun renderCompass(
        stack: ItemStack,
        mode: ModelTransformationMode,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
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

        // render antennas when circular scan mode is selected
        val scanMode = stack.compatGetOrDefault(CompassComponents.SCAN_MODE_COMPONENT, ScanMode.SPHERICAL)
        if (scanMode == ScanMode.CIRCULAR) {
            renderer.renderItem(
                stack,
                ModelTransformationMode.NONE,
                false,
                matrices,
                vertexConsumers,
                light,
                overlay,
                MinecraftClient.getInstance().bakedModelManager.getModel(ANTENNAS_MODEL_ID)
            )
        }

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
    private fun renderPositionedArrows(
        mode: ModelTransformationMode,
        entity: LivingEntity?,
        stack: ItemStack
    ): Boolean {
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
        // capture LivingEntity
        ModelPredicateProviderRegistry.register(
            ResourceFinder.RESOURCE_FINDER_ITEM,
            Identifier.of("minecraft", "hack")
        ) { _: ItemStack, _: ClientWorld?, livingEntity: LivingEntity?, _: Int ->
            lastEntity = livingEntity
            0f
        }

        // override quad colors
        ColorProviderRegistry.ITEM.register(
            { _: ItemStack, _: Int -> transformTintRgb(quadColorOverride) },
            ResourceFinder.RESOURCE_FINDER_ITEM
        )

        // use custom render function for compass
        BuiltinItemRendererRegistry.INSTANCE.register(ResourceFinder.RESOURCE_FINDER_ITEM, ::renderCompass)
    }
}