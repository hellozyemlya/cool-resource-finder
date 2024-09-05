package hellozyemlya.resourcefinder.items.compass

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.nbt.serialization.Nbt
import net.silkmc.silk.nbt.serialization.decodeFromNbtElement
import net.silkmc.silk.nbt.serialization.encodeToNbtElement

object BlockPosSerializer : KSerializer<BlockPos> {
    private val xDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("x", PrimitiveKind.INT)
    private val yDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("y", PrimitiveKind.INT)
    private val zDescriptor: SerialDescriptor = PrimitiveSerialDescriptor("z", PrimitiveKind.INT)

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        "BlockPos", StructureKind.LIST,
        xDescriptor,
        yDescriptor,
        zDescriptor
    )

    override fun deserialize(decoder: Decoder): BlockPos {
        return decoder.decodeStructure(descriptor) {
            var x = 0
            var y = 0
            var z = 0

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x = decodeIntElement(xDescriptor, 0)
                    1 -> y = decodeIntElement(yDescriptor, 1)
                    2 -> z = decodeIntElement(zDescriptor, 2)
                    DECODE_DONE -> break
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            BlockPos(x, y, z)
        }
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(xDescriptor, 0, value.x)
            encodeIntElement(yDescriptor, 1, value.y)
            encodeIntElement(zDescriptor, 2, value.z)
        }
    }

}

object ItemNetworkSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Item", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Item {
        return Registries.ITEM.get(decoder.decodeInt())
    }

    override fun serialize(encoder: Encoder, value: Item) {
        encoder.encodeInt(Registries.ITEM.getRawId(value))
    }

}

val NETWORK_SERIALIZATION_MODULE = SerializersModule {
    contextual(Item::class, ItemNetworkSerializer)
    contextual(BlockPos::class, BlockPosSerializer)
}

@OptIn(ExperimentalSerializationApi::class)
val NETWORK_CBOR = Cbor {
    serializersModule = NETWORK_SERIALIZATION_MODULE
}

public val NBT_SERIALIZATION_MODULE = SerializersModule {
    contextual(BlockPos::class, BlockPosSerializer)
}

val NBT_SERIALIZER = Nbt {
    serializersModule = NBT_SERIALIZATION_MODULE
}