package hellozyemlya.resourcefinder.registry.config

import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry

object ResourceRegistryAdapter : TypeAdapter<ResourceRegistry>() {
    override fun write(p0: JsonWriter?, p1: ResourceRegistry) {
        val groups = p1.groups
        Config.gson.toJson(groups, groups.javaClass, p0)
    }

    override fun read(p0: JsonReader?): ResourceRegistry {
        val entities = Config.gson.fromJson<List<ResourceEntry>>(p0, object : TypeToken<List<ResourceEntry>>() {}.type)
        return ResourceRegistry(entities)
    }
}