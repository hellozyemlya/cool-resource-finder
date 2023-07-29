package hellozyemlya.serializationtest

import hellozyemlya.serialization.annotations.McSerialize
import hellozyemlya.serialization.annotations.NbtIgnore
import hellozyemlya.serialization.annotations.PacketIgnore
import hellozyemlya.serialization.annotations.PersistentStateArg
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager

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

@McSerialize
abstract class PersistentStateExample(@PersistentStateArg public val id: Int) : PersistentState() {
    public abstract var intField: Int

    public val publicIgnoredMap: MutableMap<Int, Int> = HashMap()
    private val privateIgnoredMap: MutableMap<Int, Int> = HashMap()

    companion object
}


@McSerialize
abstract class WithIgnoredFields(@PersistentStateArg public val id: Int) : PersistentState() {
    @NbtIgnore
    public abstract var nbtIgnored: BlockPos

    @PacketIgnore
    public abstract var packetIgnored: Item

    public val publicIgnoredMap: MutableMap<Int, Int> = HashMap()
    private val privateIgnoredMap: MutableMap<Int, Int> = HashMap()

    companion object
}
