package hellozyemlya.resourcefinder.items

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.serialization.codecs.UnboundedMapCodec
import hellozyemlya.compat.compatRegisterComponent
import hellozyemlya.resourcefinder.MOD_NAMESPACE
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import java.util.*

data class ScanTarget(val lifetimeTicks: Int, val color: Int = 0, val target: Optional<BlockPos> = Optional.empty())

enum class ScanMode(private val value: String) : StringIdentifiable {
    SPHERICAL("spherical"), CIRCULAR("circular");

    override fun asString(): String {
        return value
    }

    companion object {
        val CODEC = StringIdentifiable.createCodec(ScanMode::values)
    }
}

object FinderCodecs {
    private val SCAN_TARGET: Codec<ScanTarget> = RecordCodecBuilder.create { builder ->
        builder.group(
            Codec.INT.fieldOf("lifetimeTicks").forGetter(ScanTarget::lifetimeTicks),
            Codec.INT.fieldOf("color").forGetter(ScanTarget::color),
            BlockPos.CODEC.optionalFieldOf("target").forGetter(ScanTarget::target)
        ).apply(builder, ::ScanTarget)
    }
    val SCAN_TARGETS: UnboundedMapCodec<Identifier, ScanTarget> = Codec.unboundedMap(Identifier.CODEC, SCAN_TARGET)
}

object CompassComponents {
    val SCAN_TARGETS_COMPONENT =
        compatRegisterComponent(Identifier.of(MOD_NAMESPACE, "c_scan_targets")!!, FinderCodecs.SCAN_TARGETS)
    val SCAN_MODE_COMPONENT =
        compatRegisterComponent(Identifier.of(MOD_NAMESPACE, "c_scan_mode")!!, ScanMode.CODEC)
}