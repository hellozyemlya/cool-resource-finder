package hellozyemlya.resourcefinder.items

import hellozyemlya.common.items.ItemClientSide
import hellozyemlya.common.pushPop
import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceFinderTexts
import hellozyemlya.resourcefinder.items.state.ClientFinderState
import hellozyemlya.resourcefinder.items.state.network.FinderStateRequestPacket
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.ModelPredicateProviderRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.Texts
import net.minecraft.util.Identifier
import net.minecraft.util.StringHelper
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.atan2

class FinderItemClientSide : ItemClientSide<FinderItem>(ResourceFinder.RESOURCE_FINDER_ITEM) {
    private val ARROW_ITEM_ID = Identifier(MOD_NAMESPACE, "resource_finder_compass_arrow")
    private val ARROW_MODEL_ID = ModelIdentifier(ARROW_ITEM_ID, "inventory")
    private val BASE_ITEM_ID = Identifier(MOD_NAMESPACE, "resource_finder_compass_base")
    private val BASE_MODEL_ID = ModelIdentifier(BASE_ITEM_ID, "inventory")
    private val INDICATOR_ITEM_ID = Identifier(MOD_NAMESPACE, "resource_finder_compass_indicator")
    private val INDICATOR_MODEL_ID = ModelIdentifier(INDICATOR_ITEM_ID, "inventory")

    private val idToState: MutableMap<Int, ClientFinderState> = HashMap()


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

    private fun renderArrows(
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

        withState(stack) { state ->
            state.targetList.forEachIndexed { idx, targetRecord ->
                quadColorOverride = ResourceRegistry.INSTANCE.getByGroup(targetRecord.item).color
                val blockPost = targetRecord.pos
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
    }

    private fun renderArrowsPreview(
        stack: ItemStack,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
        renderer: ItemRenderer
    ) {
        withState(stack) { state ->
            state.scanList.forEachIndexed { idx, scanRecord ->
                quadColorOverride = ResourceRegistry.INSTANCE.getByGroup(scanRecord.item).color
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

        val entity = lastEntity

        if (isPreviewRender(mode, entity, stack)) {
            renderArrowsPreview(stack, matrices, vertexConsumers, light, overlay, renderer)

        } else {
            renderArrows(
                entity,
                stack,
                matrices,
                vertexConsumers,
                mode,
                light,
                overlay,
                renderer
            )
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun isPreviewRender(
        mode: ModelTransformationMode,
        entity: LivingEntity?,
        stack: ItemStack
    ): Boolean {
        contract {
            returns(false) implies (entity != null)
        }

        return when (mode) {
            ModelTransformationMode.FIRST_PERSON_RIGHT_HAND, ModelTransformationMode.FIRST_PERSON_LEFT_HAND, ModelTransformationMode.HEAD, ModelTransformationMode.THIRD_PERSON_LEFT_HAND, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND -> {
                entity == null
            }

            ModelTransformationMode.GUI -> {
                if (entity != null) {
                    !(entity.mainHandStack === stack || entity.offHandStack === stack)
                } else {
                    true
                }
            }

            else -> {
                true
            }
        }
    }

    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        if(stack != null && tooltip != null) {
            withState(stack) {
                it.scanList.forEach { scanRecord ->
                    val blockName = Texts.setStyleIfAbsent(
                        scanRecord.item.name.copyContentOnly(),
                        Style.EMPTY.withColor(TextColor.fromRgb(ResourceRegistry.INSTANCE.getByGroup(scanRecord.item).color))
                    )

                    tooltip.add(
                        Texts.join(
                            mutableListOf(
                                ResourceFinderTexts.SCAN_FOR,
                                blockName,
                                ResourceFinderTexts.SCAN_JOIN,
                                Text.of(StringHelper.formatTicks(scanRecord.time))
                            ), Text.of(" ")
                        )
                    )
                }
            }
        }
    }

    fun withState(stack: ItemStack, block: (state: ClientFinderState) -> Unit) {
        if (stack.hasNbt() && stack.orCreateNbt.contains("finder_id")) {
            val id = stack.orCreateNbt.getInt("finder_id")
            val state = idToState[id]
            if (state != null) {
                block(state)
            } else {
                ClientPlayNetworking.send(FinderStateRequestPacket(id))
            }
        }
    }

    init {
        ModelLoadingPlugin.register { ctx ->
            ctx.addModels(BASE_MODEL_ID, INDICATOR_MODEL_ID, ARROW_MODEL_ID)
        }
        ModelPredicateProviderRegistry.register(
            ResourceFinder.RESOURCE_FINDER_ITEM,
            Identifier("hack")
        ) { _: ItemStack, _: ClientWorld?, livingEntity: LivingEntity?, _: Int ->
            lastEntity = livingEntity
            0f
        }
        ColorProviderRegistry.ITEM.register(
            { _: ItemStack, _: Int -> quadColorOverride },
            ResourceFinder.RESOURCE_FINDER_ITEM
        )
        BuiltinItemRendererRegistry.INSTANCE.register(ResourceFinder.RESOURCE_FINDER_ITEM, ::renderCompass)

        ClientPlayNetworking.registerGlobalReceiver(FinderStateUpdatePacket.PACKET_TYPE) { packet: FinderStateUpdatePacket, _: ClientPlayerEntity, _: PacketSender ->
            val state = packet.clientState
            idToState[state.id] = state
        }
    }
}