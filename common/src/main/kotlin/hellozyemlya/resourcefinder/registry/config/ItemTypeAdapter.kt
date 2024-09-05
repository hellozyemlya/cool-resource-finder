package hellozyemlya.resourcefinder.registry.config

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object ItemTypeAdapter : TypeAdapter<Item>() {
    override fun write(writer: JsonWriter, item: Item) {
        writer.value(Registries.ITEM.getId(item).toString())
    }

    override fun read(reader: JsonReader): Item {
        val itemId = Identifier.tryParse(reader.nextString())
        if(!Registries.ITEM.containsId(itemId)) {
            throw Exception("Can't find item '${itemId}'")
        }
        return Registries.ITEM.get(itemId)
    }
}