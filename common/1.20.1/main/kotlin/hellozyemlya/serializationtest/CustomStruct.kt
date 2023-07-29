package hellozyemlya.serializationtest

import hellozyemlya.serialization.annotations.McSerialize
import hellozyemlya.serialization.annotations.NbtIgnore
import hellozyemlya.serialization.annotations.PacketIgnore
import hellozyemlya.serialization.annotations.PersistentStateArg
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState


//@McSerialize
//interface ResourceScannerState {
//    @PersistentStateArg
//    val id: Int
//    val scanRecords: MutableMap<Item, Int>
//    @NbtIgnore
//    val targets: MutableMap<Item, BlockPos>
//    companion object
//}
//@McSerialize
//abstract class PersistentResourceScannerState : PersistentState(), ResourceScannerState {
//    companion object
//}
