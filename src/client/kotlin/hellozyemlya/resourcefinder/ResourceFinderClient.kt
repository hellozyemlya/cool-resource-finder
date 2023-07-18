package hellozyemlya.resourcefinder

import hellozyemlya.resourcefinder.items.getScanList
import hellozyemlya.resourcefinder.items.getTargetList
import hellozyemlya.resourcefinder.render.GuiItemRenderCallContext
import hellozyemlya.resourcefinder.render.HeldItemRenderCallContext
import hellozyemlya.resourcefinder.render.ItemRenderCallStack
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

    private fun renderArrowsOnEntity(
        entity: LivingEntity,
        stack: ItemStack,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        renderer: ItemRenderer
    ) {
        stack.getTargetList().forEachIndexed { idx, targetRecord ->
            matrices.push()
            matrices.translate(0f, (idx * 0.01f), 0f)
            matrices.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(
                    getArrowAngle(
                        entity,
                        targetRecord.target
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
            matrices.pop()
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
            ItemRenderCallStack.INSTANCE.requireTopmost()
            when (val ctx = ItemRenderCallStack.INSTANCE.peekTopContext()) {
                is GuiItemRenderCallContext -> {
                    val entity = ctx.entity()
                    if (entity != null) {
                        if (entity.mainHandStack === stack || entity.offHandStack === stack) {
                            renderArrowsOnEntity(entity, stack, matrices, vertexConsumers, light, overlay, renderer)
                            return@register
                        }
                    }
                }

                is HeldItemRenderCallContext -> {
                    renderArrowsOnEntity(ctx.entity(), stack, matrices, vertexConsumers, light, overlay, renderer)
                    return@register
                }
            }


            renderArrowsPreview(stack, matrices, vertexConsumers, light, overlay, renderer)
        }

        ColorProviderRegistry.ITEM.register(
            { stack, _ -> stack.orCreateNbt.getInt("color") },
            ResourceFinder.RESOURCE_FINDER_ARROW_ITEM
        )
    }
}