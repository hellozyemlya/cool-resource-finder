package hellozyemlya.serializationtest

import hellozyemlya.serialization.annotations.McSerialize
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos

@McSerialize
interface CustomStruct {
    val intValue: Int
    val stringValue: String
    val innerStruct: CustomStruct2
    val itemField: Item
    val blockPos: BlockPos
}

@McSerialize
interface CustomStruct2 {
}

@McSerialize
interface CustomStruct3 {
}