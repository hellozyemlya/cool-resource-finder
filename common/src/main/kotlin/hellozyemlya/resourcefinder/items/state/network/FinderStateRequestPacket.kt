package hellozyemlya.resourcefinder.items.state.network

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

class FinderStateRequestPacket : FabricPacket {
    val id: Int

    constructor(id: Int) {
        this.id = id
    }

    constructor(buf: PacketByteBuf) {
        this.id = buf.readInt()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(id)
    }

    override fun getType(): PacketType<*> {
        return PACKET_TYPE
    }

    companion object {
        val ID = Identifier(MOD_NAMESPACE, "finder_state_request")
        val PACKET_TYPE: PacketType<FinderStateRequestPacket> = PacketType.create(ID, ::FinderStateRequestPacket)
    }
}