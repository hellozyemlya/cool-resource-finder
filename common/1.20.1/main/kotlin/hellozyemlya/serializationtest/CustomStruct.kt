package hellozyemlya.serializationtest

import hellozyemlya.serialization.annotations.McSerialize
import hellozyemlya.serialization.generated.create
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