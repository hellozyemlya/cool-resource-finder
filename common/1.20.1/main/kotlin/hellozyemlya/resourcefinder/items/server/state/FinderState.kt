package hellozyemlya.resourcefinder.items.server.state

import hellozyemlya.common.compound
import hellozyemlya.common.compoundList
import hellozyemlya.common.int
import hellozyemlya.common.item
import hellozyemlya.resourcefinder.items.state.ClientFinderState
import hellozyemlya.resourcefinder.items.state.ClientScanRecord
import hellozyemlya.resourcefinder.items.state.ClientTargetRecord
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

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
            val result = FinderState(nbt.int("id"))

            nbt.compoundList("scan_list").forEach {
                result.scanList[it.item("item")] = it.int("time")
            }

            return result
        }
    }
}