package hellozyemlya.common

import net.minecraft.client.util.math.MatrixStack

fun MatrixStack.pushPop(callback: () -> Unit) {
    this.push()
    callback()
    this.pop()
}