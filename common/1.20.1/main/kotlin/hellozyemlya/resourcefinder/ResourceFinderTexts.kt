package hellozyemlya.resourcefinder

import net.minecraft.text.MutableText
import net.minecraft.text.Text

object ResourceFinderTexts {
    val SCAN_FOR
        get() = of("finds_what")
    val SCAN_JOIN
        get() = of("finds_join")

    private fun of(key: String): MutableText {
        return Text.translatable("${MOD_NAMESPACE}.${key}")
    }
}