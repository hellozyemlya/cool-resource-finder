package hellozyemlya.resourcefinder.items.state

import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos


data class ClientScanRecord(val item: Item, val time: Int)
data class ClientTargetRecord(val item: Item, val pos: BlockPos)
data class ClientFinderState(val id: Int, val scanList: List<ClientScanRecord>, val targetList: List<ClientTargetRecord>)