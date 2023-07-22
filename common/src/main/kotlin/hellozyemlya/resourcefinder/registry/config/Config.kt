package hellozyemlya.resourcefinder.registry.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.item.Item
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths


object Config {
    private object AdapterFactory : TypeAdapterFactory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> create(p0: Gson, p1: TypeToken<T>): TypeAdapter<T>? {
            return when {
                Block::class.java.isAssignableFrom(p1.rawType) -> BlockTypeAdapter as TypeAdapter<T>
                Item::class.java.isAssignableFrom(p1.rawType) -> ItemTypeAdapter as TypeAdapter<T>
                else -> null
            }
        }

    }

    val gson: Gson by lazy {
        val builder = GsonBuilder()
        builder.registerTypeAdapterFactory(AdapterFactory)
        builder.registerTypeAdapter(ResourceRegistry::class.java, ResourceRegistryAdapter)
        builder.setPrettyPrinting()
        builder.create()
    }

    fun getConfigFile(configName: String): File {
        return Paths.get(FabricLoader.getInstance().configDir.toString(), "$configName.json").toFile()
    }

    public inline fun <reified T> save(obj: T, name: String) {
        FileWriter(getConfigFile(name)).use { fileWriter ->
            gson.toJson(
                    obj,
                    T::class.java,
                    fileWriter
            )
        }
    }

    public inline fun <reified T> load(name: String, default: T): T {
        val configFile = getConfigFile(name)
        if (configFile.exists()) {
            FileReader(configFile).use { fileReader ->
                return gson.fromJson(
                        fileReader,
                        T::class.java,
                )
            }
        } else {
            save(default, name)
            return default
        }
    }
}