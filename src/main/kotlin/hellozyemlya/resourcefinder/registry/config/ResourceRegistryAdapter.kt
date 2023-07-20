package hellozyemlya.resourcefinder.registry.config

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry

object ResourceRegistryAdapter : TypeAdapter<ResourceRegistry>() {
    override fun write(p0: JsonWriter?, p1: ResourceRegistry) {
        Config.gson.toJson(p1.groups.toTypedArray(), Array<ResourceEntry>::class.java, p0)
    }

    override fun read(p0: JsonReader?): ResourceRegistry {
        TODO("Not yet implemented")
    }
}