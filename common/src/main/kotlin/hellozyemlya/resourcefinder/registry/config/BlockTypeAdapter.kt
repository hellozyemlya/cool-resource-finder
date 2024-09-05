package hellozyemlya.resourcefinder.registry.config

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object BlockTypeAdapter : TypeAdapter<Block>() {
    override fun write(writer: JsonWriter, block: Block) {
        writer.value(Registries.BLOCK.getId(block).toString())
    }

    override fun read(reader: JsonReader): Block {
        val blockId = Identifier.tryParse(reader.nextString())
        if (!Registries.BLOCK.containsId(blockId)) {
            throw Exception("Can't find block '${blockId}'")
        }
        return Registries.BLOCK.get(blockId)
    }
}