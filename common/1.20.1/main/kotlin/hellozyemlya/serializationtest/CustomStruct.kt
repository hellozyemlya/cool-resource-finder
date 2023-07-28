package hellozyemlya.serializationtest

import hellozyemlya.serialization.annotations.McSerialize
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos

@McSerialize
interface CustomStruct {
    val intValue: Int
    val stringValue: String
    val innerStruct: StructOfLists
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
interface StructOfLists {
    val listOfStructs: MutableList<StructOfLists>
    val listOfBlockPos: MutableList<BlockPos>
    val listOfInts: MutableList<Int>
    companion object
}

@McSerialize
interface StructOfNullableLists {
    val listOfStructs: MutableList<StructOfLists>?
    val listOfBlockPos: MutableList<BlockPos>?
    val listOfInts: MutableList<Int>?
    companion object
}

@McSerialize
interface CustomStruct3 {
    companion object
}

@McSerialize
abstract class FinderState(public val id: Int) {
    public abstract val intField: Int

    public val publicIgnoredMap: MutableMap<Int, Int> = HashMap()
    private val privateIgnoredMap: MutableMap<Int, Int> = HashMap()

    companion object
}