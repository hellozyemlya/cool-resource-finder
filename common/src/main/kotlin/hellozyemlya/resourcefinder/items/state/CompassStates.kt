package hellozyemlya.resourcefinder.items.state

import hellozyemlya.common.BasePersistentState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("finder_id_allocator")
class FinderIdAllocator : BasePersistentState() {
    @SerialName("next_finder_id")
    private var nextId: Int = 0

    fun allocateId(): Int {
        val result = ++nextId
        markDirty()
        return result
    }
}

@Serializable
@SerialName("finder_state")
class FinderState : BasePersistentState() {

}