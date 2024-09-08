package hellozyemlya.common

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import kotlin.reflect.KProperty

interface CompoundDelegate<T> {
    operator fun getValue(thisRef: CompoundWrapper, property: KProperty<*>): T
    operator fun setValue(thisRef: CompoundWrapper, property: KProperty<*>, value: T)
}

interface RoCompoundDelegate<T> {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T
}

open class CompoundWrapper(@PublishedApi internal val compound: NbtCompound) {
    fun integer(default: Int = 0, name: String? = null): CompoundDelegate<Int> {
        return object : CompoundDelegate<Int> {
            override fun getValue(thisRef: CompoundWrapper, property: KProperty<*>): Int {
                val resolvedName = name ?: property.name
                if (compound.contains(resolvedName)) {
                    return compound.getInt(name)
                } else {
                    return default
                }
            }

            override fun setValue(thisRef: CompoundWrapper, property: KProperty<*>, value: Int) {
                compound.putInt(name, value)
            }
        }
    }

    inline fun <reified T : CompoundWrapper> compoundList(
        crossinline factory: (nbt: NbtCompound) -> T,
        name: String? = null
    ): RoCompoundDelegate<MutableList<T>> {
        return object : RoCompoundDelegate<MutableList<T>> {
            override fun getValue(thisRef: Any, property: KProperty<*>): MutableList<T> {
                val resolvedName = name ?: property.name

                val list = if (compound.contains(resolvedName)) {
                    compound.getList(resolvedName, NbtCompound.COMPOUND_TYPE.toInt())
                } else {
                    val l = NbtList()
                    compound.put(resolvedName, l)
                    l
                }
                
                return object : AbstractMutableList<T>() {
                    override fun add(index: Int, element: T) {
                        list.add(index, element.compound)
                    }

                    override val size: Int
                        get() = list.size

                    override fun get(index: Int): T {
                        return factory(list[index] as NbtCompound)
                    }

                    override fun removeAt(index: Int): T {
                        return factory(list.removeAt(index) as NbtCompound)
                    }

                    override fun set(index: Int, element: T): T {
                        return factory(list.set(index, element.compound) as NbtCompound)
                    }

                }
            }
        }
    }
}

class Hello(compound: NbtCompound) : CompoundWrapper(compound) {
    var tickCount by integer(name = "tickCount")
    val helloList by compoundList(Hello)
}
