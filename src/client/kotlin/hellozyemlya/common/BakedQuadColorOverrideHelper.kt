package hellozyemlya.common

import hellozyemlya.resourcefinder.common.BakedQuadColorOverride
import hellozyemlya.resourcefinder.common.BakedQuadColorProvider
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

val providersCache = Int2ObjectOpenHashMap<BakedQuadColorProvider>()

/**
 * All [net.minecraft.client.render.model.BakedQuad] instances rendered within [block] will use given [color]
 * if their color index is not -1.
 */
fun withQuadsColor(color: Int, block: () -> Unit) {
    val previous = BakedQuadColorOverride.getOverride()
    if (!providersCache.containsKey(color)) {
        providersCache[color] =
            BakedQuadColorProvider { _, _ -> color }
    }
    BakedQuadColorOverride.setOverride(providersCache[color])
    block()
    BakedQuadColorOverride.setOverride(previous)
}