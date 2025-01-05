package hellozyemlya.compat.registries

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.math.ColorHelper

fun registerFakeModel(namespace: String, itemId: String): ModelIdentifier {
    val fakeItemId = Identifier.of(namespace, "item/${itemId}")
    val fakeModelId = ModelIdentifier(fakeItemId, "fabric_resource")
    ModelLoadingPlugin.register { ctx ->
        ctx.addModels(fakeItemId)
    }
    return fakeModelId
}

fun transformTintRgb(color: Int): Int {
    return ColorHelper.Argb.withAlpha(255, color)
}