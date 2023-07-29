package hellozyemlya.resourcefinder.items.state

import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import hellozyemlya.serialization.annotations.McSerialize
import hellozyemlya.serialization.annotations.NbtIgnore
import hellozyemlya.serialization.annotations.PersistentStateArg
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.Item
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState


@McSerialize
interface FinderState {
    @PersistentStateArg
    val id: Int
    val scanList: MutableMap<Item, Int>

    @NbtIgnore
    val targets: MutableMap<Item, BlockPos>

    companion object
}

@McSerialize
abstract class PersistentFinderState : PersistentState(), FinderState {
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
                        targets[it.key] = posCandidate.get()
                    }
                }
            }

            markDirty()
            println("state ${Thread.currentThread().id}")
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
    
    companion object
}