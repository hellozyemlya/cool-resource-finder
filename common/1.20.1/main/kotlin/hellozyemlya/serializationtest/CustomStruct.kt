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
    val listExample: MutableList<Int>?
    val mapField: MutableMap<Item, Int>?
    val recursiveMapField: MutableMap<Item, CustomStruct>
    val nullableValue: Int?
    val listOfItems: MutableList<Item>
    val listOfStructs: MutableList<CustomStruct>
    val listOfBlockPos: MutableList<BlockPos>
    companion object HelloWorld
}

@McSerialize
interface CustomStruct2 {
    companion object
}

@McSerialize
interface CustomStruct3 {
    companion object
}