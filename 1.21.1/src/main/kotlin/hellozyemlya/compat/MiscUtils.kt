package hellozyemlya.compat

import net.minecraft.util.StringHelper

fun formatTicks(ticks: Int, tickRate: Float): String {
    return StringHelper.formatTicks(ticks, tickRate)
}