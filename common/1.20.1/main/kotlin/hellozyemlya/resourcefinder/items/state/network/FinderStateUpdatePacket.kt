package hellozyemlya.resourcefinder.items.state.network

import hellozyemlya.resourcefinder.MOD_NAMESPACE
import hellozyemlya.resourcefinder.items.state.PersistentFinderState
import hellozyemlya.resourcefinder.items.state.ClientFinderState
import hellozyemlya.resourcefinder.items.state.ClientScanRecord
import hellozyemlya.resourcefinder.items.state.ClientTargetRecord
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class FinderStateUpdatePacket : FabricPacket {
    public val clientState: ClientFinderState

    constructor(state: PersistentFinderState) {
        clientState = state.toClient()
    }

    constructor(buf: PacketByteBuf) {
        clientState = ClientFinderState(buf.readInt(),
            List(buf.readInt()) {
                ClientScanRecord(Registries.ITEM.get(buf.readIdentifier()), buf.readInt())
            },
            List(buf.readInt()) {
                ClientTargetRecord(Registries.ITEM.get(buf.readIdentifier()), buf.readBlockPos())
            },
        )
    }

    override fun write(buf: PacketByteBuf) {
        println("write ${Thread.currentThread().id}")
        buf.writeInt(clientState.id)

        buf.writeInt(clientState.scanList.size)

        clientState.scanList.forEach {
            buf.writeIdentifier(Registries.ITEM.getId(it.item))
            buf.writeInt(it.time)
        }

        buf.writeInt(clientState.targetList.size)

        clientState.targetList.forEach {
            buf.writeIdentifier(Registries.ITEM.getId(it.item))
            buf.writeBlockPos(it.pos)
        }
    }

    override fun getType(): PacketType<*> {
        return PACKET_TYPE
    }

    companion object {
        val ID = Identifier(MOD_NAMESPACE, "finder_state_channel")
        val PACKET_TYPE: PacketType<FinderStateUpdatePacket> = PacketType.create(ID, ::FinderStateUpdatePacket)
    }
}

