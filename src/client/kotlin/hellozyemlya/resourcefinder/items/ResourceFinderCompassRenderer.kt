//package hellozyemlya.resourcefinder.items
//
//import hellozyemlya.resourcefinder.ResourceFinder
//import net.minecraft.entity.Entity
//import net.minecraft.item.ItemStack
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.MathHelper
//import net.minecraft.util.math.RotationAxis
//import net.minecraft.util.math.Vec3d
//import net.minecraft.world.World
//import kotlin.math.atan2
//
//
//class ResourceFinderCompassRenderer {
//    companion object {
//        private const val COMPASS_3D_ARROW_COLOR_KEY = "compass3d.arrow.color"
//        private val colorToArrowStack: MutableMap<Int, ItemStack> = HashMap()
//
//        private fun getArrowStackFromColor(color: Int): ItemStack {
//            val result = ItemStack(ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
//            result.getOrCreateNbt().putInt(COMPASS_3D_ARROW_COLOR_KEY, color)
//            return result
////            return if (colorToArrowStack.containsKey(color)) {
////                colorToArrowStack[color]!!
////            } else {
////                val result = ItemStack(ResourceFinder.RESOURCE_FINDER_ARROW_ITEM)
////                result.getOrCreateNbt().putInt(COMPASS_3D_ARROW_COLOR_KEY, color)
////                colorToArrowStack[color] = result
////                result
////            }
//        }
//
//        fun getColor(stack: ItemStack, tintIndex: Int): Int {
//            return stack.getOrCreateNbt().getInt(COMPASS_3D_ARROW_COLOR_KEY)
//        }
//
//        private fun getAngleTo(entity: Entity, pos: BlockPos): Double {
//            val vec3d = Vec3d.ofCenter(pos)
//            return atan2(vec3d.getZ() - entity.z, vec3d.getX() - entity.x) / 6.2831854820251465
//        }
//
//        private fun getBodyYaw(entity: Entity): Double {
//            return MathHelper.floorMod((entity.bodyYaw / 360.0f).toDouble(), 1.0)
//        }
//
//        val resourceFinderCompassInventoryTick =
//            { itemStack: ItemStack?, world: World?, entity: Entity, slot: Int, selected: Boolean ->
//                @Suppress("CAST_NEVER_SUCCEEDS")
//                val stackEx = itemStack as ItemStackEx
//
//                stackEx.subStacks.clear()
//
//                ResourceFinderCompass.PositionNbt(itemStack).forEachIndexed { idx, positionEntry ->
//                    val arrowItemStack = getArrowStackFromColor(positionEntry.entry.color)
//
//                    @Suppress("CAST_NEVER_SUCCEEDS")
//                    val arrowItemStackEx = arrowItemStack as ItemStackEx
//
//                    arrowItemStackEx.modelTransform = MatrixModifier { _, matrices ->
//                        val d = getAngleTo(entity, positionEntry.position)
//                        val e = getBodyYaw(entity)
//                        val a = (0.5 - (e - 0.25 - d)).toFloat()
//                        matrices.translate(0.5, 0.5 + (idx * 0.01), 0.5)
//                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f))
//                        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-(360 * a)))
//                        matrices.translate(-0.5, -0.5, -0.5)
//                    }
//
//                    stackEx.subStacks.add(arrowItemStack)
//                }
//            }
//    }
//}