package hellozyemlya.resourcefinder.items.state

import hellozyemlya.common.compound
import hellozyemlya.common.compoundList
import hellozyemlya.common.int
import hellozyemlya.common.item
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import kotlinx.serialization.SerialName
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState


class FinderIdAllocator() : PersistentState() {
    private constructor(id: Int) : this() {
        nextId = id
    }

    @SerialName("next_finder_id")
    private var nextId: Int = 0

    fun allocateId(): Int {
        val result = ++nextId
        markDirty()
        return result
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.putInt("next_finder_id", nextId)
        return nbt
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): FinderIdAllocator {
            return FinderIdAllocator(nbt.getInt("next_finder_id"))
        }
    }
}

class FinderState(private val id: Int) : PersistentState() {
    val scanList: MutableMap<Item, Int> = HashMap()
    private val targets: MutableMap<Item, BlockPos> = HashMap()

    private var isActive: Boolean = false

    fun inventoryTick(entity: ServerPlayerEntity, active: Boolean) {
        if (active) {
            isActive = true

            scanList.entries.removeAll {
                val newTime = it.value - 1
                if (newTime <= 0) {
                    true
                } else {
                    it.setValue(newTime)
                    false
                }
            }

            targets.clear()

            scanList.forEach {
                scanList.forEach {
                    val resourceRecord = ResourceRegistry.INSTANCE.getByGroup(it.key)
                    val posCandidate = resourceRecord.findClosest(16, entity.blockPos, entity.world)
                    if (posCandidate.isPresent) {
                        targets[it.key] =  posCandidate.get()
                    }
                }
            }

//            val scans = scanList.entries.joinToString { "${Registries.ITEM.getId(it.key)}:${it.value}" }
//            println("scan for id: $id, scans: $scans")
            markDirty()

            ServerPlayNetworking.send(entity, FinderStateUpdatePacket(this))
        } else if (isActive) {
            // todo send deactivation packet
        }

        isActive = false
    }

    fun putResourceEntry(entry: ResourceEntry, time: Int) {
        scanList[entry.group] = scanList.getOrDefault(entry.group, 0) + time
        markDirty()
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        nbt.run {
            int("id", id)

            if (scanList.isNotEmpty()) {
                compoundList("scan_list") {
                    scanList.forEach {
                        compound {
                            item("item", it.key)
                            int("time", it.value)
                        }
                    }
                }
            }
        }
        return nbt
    }

    fun toClient(): ClientFinderState {
        return ClientFinderState(
            id,
            scanList.map { ClientScanRecord(it.key, it.value) },
            targets.map { ClientTargetRecord(it.key, it.value) }
        )
    }

    companion object {
        fun fromNbt(nbt: NbtCompound): FinderState {
            val result = FinderState(nbt.int("next_finder_id"))

            nbt.compoundList("scan_list").forEach {
                result.scanList[it.item("item")] = it.int("time")
            }

            return result
        }
    }
}

data class ClientScanRecord(val item: Item, val time: Int)
data class ClientTargetRecord(val item: Item, val pos: BlockPos)
data class ClientFinderState(val id: Int, val scanList: List<ClientScanRecord>, val targetList: List<ClientTargetRecord>)