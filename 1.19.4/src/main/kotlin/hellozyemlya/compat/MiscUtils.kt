package hellozyemlya.compat

import net.minecraft.util.StringHelper

@Suppress("UNUSED_PARAMETER")
fun formatTicks(ticks: Int, tickRate: Float): String {
    return StringHelper.formatTicks(ticks)
}