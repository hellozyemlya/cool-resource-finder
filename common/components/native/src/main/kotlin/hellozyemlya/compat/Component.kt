package hellozyemlya.compat

import com.mojang.serialization.Codec
import net.minecraft.component.ComponentType
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier


typealias CompatComponentType<T> = ComponentType<T>

fun <T> compatRegisterComponent(identifier: Identifier, codec: Codec<T>): CompatComponentType<T> {
    return Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        identifier,
        ComponentType.builder<T>().codec(codec).build()
    )
}

fun <T> ItemStack.compatGetOrDefault(component: CompatComponentType<T>, default: T): T {
    return this.getOrDefault(component, default)
}

fun <T> ItemStack.compatGet(component: CompatComponentType<T>): T? {
    return this.get(component)
}

fun <T> ItemStack.compatSet(component: CompatComponentType<T>, value: T) {
    this.set(component, value)
}