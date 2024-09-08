package hellozyemlya.resourcefinder.items.storage

import hellozyemlya.resourcefinder.items.compass.NETWORK_SERIALIZATION_MODULE
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.util.math.BlockPos
import net.silkmc.silk.nbt.serialization.Nbt
import net.silkmc.silk.nbt.serialization.decodeFromNbtElement
import net.silkmc.silk.nbt.serialization.encodeToNbtElement
import org.junit.jupiter.api.Test

@Serializable
data class CompassScanItem(var lifetimeTicks: Int, @Contextual var target: BlockPos? = null, var color: Int = 0)

@Serializable
data class CompassData(var scanTimeoutTicks: Int = 0, val scanList: MutableMap<Int, CompassScanItem> = mutableMapOf())


@Serializable
data class CompassDataStorage(val nextId: Int = -1, val data: MutableMap<Int, CompassData> = mutableMapOf())

class NbtEncoderTest {
    @Test
    fun hello() {
        val nbt = Nbt {
            serializersModule = NETWORK_SERIALIZATION_MODULE
        }
        val data = CompassDataStorage()
        data.data[1] = CompassData(0, mutableMapOf(1 to CompassScanItem(10)))
        val serialized = nbt.encodeToNbtElement(data)
        val deserialized = nbt.decodeFromNbtElement<CompassDataStorage>(serialized)
        println(deserialized)
    }
}