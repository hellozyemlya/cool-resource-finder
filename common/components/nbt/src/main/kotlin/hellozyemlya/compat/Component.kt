package hellozyemlya.compat

import com.mojang.serialization.Codec
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.util.Identifier
import org.jetbrains.annotations.Nullable


data class NbtComponent<T>(val identifier: Identifier, val codec: Codec<T>)

typealias CompatComponentType<T> = NbtComponent<T>

fun <T> compatRegisterComponent(identifier: Identifier, codec: Codec<T>): CompatComponentType<T> {
    return NbtComponent(identifier, codec)
}

fun <T> ItemStack.compatGetOrDefault(component: CompatComponentType<T>, default: T): T {
    val id = component.identifier.toString()
    val codec = component.codec
    if (this.orCreateNbt.contains(id)) {
        val dataResult = codec.decode(NbtOps.INSTANCE, this.orCreateNbt.get(id))
        return if (dataResult.result().isEmpty) {
            default
        } else {
            dataResult.result().get().first
        }
    } else {
        return default
    }
}

fun <T> ItemStack.compatGet(component: CompatComponentType<T>): T? {
    val id = component.identifier.toString()
    val codec = component.codec
    if (this.orCreateNbt.contains(id)) {
        val dataResult = codec.decode(NbtOps.INSTANCE, this.orCreateNbt.get(id))
        return dataResult.result().get().first
    } else {
        return null
    }
}

fun <T> ItemStack.compatSet(component: CompatComponentType<T>, value: T) {
    val id = component.identifier.toString()
    val codec = component.codec
    val dataResult = codec.encodeStart(NbtOps.INSTANCE, value)
    this.orCreateNbt.put(id, dataResult.result().get())
}