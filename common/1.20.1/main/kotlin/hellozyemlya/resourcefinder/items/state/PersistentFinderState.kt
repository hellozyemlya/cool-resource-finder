package hellozyemlya.resourcefinder.items.state

import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.items.server.FinderItemServerSide
import hellozyemlya.resourcefinder.items.state.network.FinderStateUpdatePacket
import hellozyemlya.resourcefinder.registry.ResourceEntry
import hellozyemlya.resourcefinder.registry.ResourceRegistry
import hellozyemlya.serialization.annotations.McSerialize
import hellozyemlya.serialization.annotations.NbtIgnore
import hellozyemlya.serialization.annotations.PersistentStateArg
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.Item
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState


@McSerialize
interface FinderState {
    @PersistentStateArg
    val id: Int
    val scanList: MutableMap<Item, Int>

    @NbtIgnore
    val targets: MutableMap<Item, BlockPos>

    fun putResourceEntry(entry: ResourceEntry, time: Int) {
        scanList[entry.group] = scanList.getOrDefault(entry.group, 0) + time
    }

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
                val resourceRecord = ResourceRegistry.INSTANCE.getByGroup(it.key)
                val posCandidate = resourceRecord.findClosest(16, entity.blockPos, entity.world)
                if (posCandidate.isPresent) {
                    targets[it.key] = posCandidate.get()
                }
            }

            markDirty()

            ResourceFinder.RESOURCE_FINDER_ITEM.getServerSide<FinderItemServerSide>()
                .sendFinderState(entity, this)
            
        } else if (isActive) {
            // todo send deactivation packet
        }

        isActive = false
    }

    override fun putResourceEntry(entry: ResourceEntry, time: Int) {
        super.putResourceEntry(entry, time)
        markDirty()
    }
    
    companion object
}