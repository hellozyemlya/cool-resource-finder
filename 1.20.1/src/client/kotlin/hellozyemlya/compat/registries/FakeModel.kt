package hellozyemlya.compat.registries

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier

fun registerFakeModel(namespace: String, itemId: String): ModelIdentifier {
    val fakeItemId = Identifier(namespace, itemId)
    val fakeModelId = ModelIdentifier(fakeItemId, "inventory")
    ModelLoadingPlugin.register { ctx ->
        ctx.addModels(fakeModelId)
    }
    return fakeModelId
}

fun transformTintRgb(color: Int): Int {
    return color
}