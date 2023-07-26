package hellozyemlya.resourcefinder.items

import hellozyemlya.common.*
import hellozyemlya.resourcefinder.registry.ResourceEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

// TODO bound it to server instance
object ResourceFinderStateManager {
    private final val states: MutableMap<Int, ResourceFinderState> = HashMap()

    fun get(id: Int): ResourceFinderState? {
        return states[id]
    }

    fun getOrCreate(id: Int, stack: ItemStack): ResourceFinderState {
        return if(states.contains(id)) {
            states[id]!!
        } else {
            val state = ResourceFinderState.fromNbt(id, stack.nbt)
            states[id] = state
            state
        }
    }
}

class ResourceFinderState(private val id: Int) {
    private final val scansFor: MutableMap<Item, Int> = HashMap()
    private var isActive: Boolean = false


    fun inventoryTick(entity: PlayerEntity, active: Boolean) {
        if(active) {
            isActive = true
            scansFor.entries.removeAll {
                val newTime = it.value - 1
                if(newTime <= 0) {
                    true
                } else {
                    it.setValue(newTime)
                    false
                }
            }
            val scans = scansFor.entries.joinToString { "${Registries.ITEM.getId(it.key)}:${it.value}" }
            println("scan for id: $id, scans: $scans")
            // todo send activation packet
        } else if(isActive) {
            // todo send deactivation packet
        }

        isActive = false
    }

    fun putResourceEntry(entry: ResourceEntry, time: Int) {
        scansFor[entry.group] = scansFor.getOrDefault(entry.group, 0) + time
    }

    fun writeToNbt(compound: NbtCompound) {
        compound.compoundList("scans_for") {
            clear()
            scansFor.forEach { (k, v) ->
                compound {
                    item("item", k)
                    int("time", v)
                }
            }
        }
    }

    companion object {
        fun fromNbt(id: Int, nbt: NbtCompound?): ResourceFinderState {
            val state = ResourceFinderState(id)

            if(nbt == null) {
                return state;
            }

            if(nbt.contains("scans_for")) {
                val list = nbt.getList("scans_for", NbtCompound.COMPOUND_TYPE.toInt())
                list.forEach {
                    val compound = it as NbtCompound
                    val item = Registries.ITEM.get(Identifier.tryParse(compound.getString("item")))
                    val time = compound.getInt("time")
                    state.scansFor[item] = time
                }
            }

            return state
        }
    }
}