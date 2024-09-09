package hellozyemlya.mccompat

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import hellozyemlya.resourcefinder.MOD_NAMESPACE
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*


data class ScanTarget(val lifetimeTicks: Int, val color: Int = 0, val target: Optional<BlockPos> = Optional.empty())

object CustomCodecs {
    val SCAN_TARGET: Codec<ScanTarget> = RecordCodecBuilder.create { builder ->
        builder.group(
            Codec.INT.fieldOf("lifetimeTicks").forGetter(ScanTarget::lifetimeTicks),
            Codec.INT.fieldOf("color").forGetter(ScanTarget::color),
            BlockPos.CODEC.optionalFieldOf("target").forGetter(ScanTarget::target)
        ).apply(builder, ::ScanTarget)
    }
    val SCAN_TARGETS = Codec.unboundedMap(Identifier.CODEC, SCAN_TARGET)
}

object CompassComponents {
    val TICK_TIMEOUT_COMPONENT = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MOD_NAMESPACE, "scan_timeout"),
        ComponentType.builder<Int>().codec(Codec.INT).build()
    )
    val SCAN_TARGETS_COMPONENT = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MOD_NAMESPACE, "scan_targets"),
        ComponentType.builder<Map<Identifier, ScanTarget>>().codec(CustomCodecs.SCAN_TARGETS).build()
    )
}