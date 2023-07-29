package hellozyemlya.resourcefinder.items.state.network

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.items.state.*
import hellozyemlya.serialization.generated.readFrom
import hellozyemlya.serialization.generated.writeTo
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class FinderStateUpdatePacket(val finderState: FinderState) : FabricPacket {
    constructor(buf: PacketByteBuf) : this(FinderState.readFrom(buf))

    override fun write(buf: PacketByteBuf) {
        finderState.writeTo(buf)
    }

    override fun getType(): PacketType<*> {
        return PACKET_TYPE
    }

    companion object {
        val ID = Identifier(MOD_NAMESPACE, "finder_state_channel")
        val PACKET_TYPE: PacketType<FinderStateUpdatePacket> = PacketType.create(ID, ::FinderStateUpdatePacket)
    }
}

