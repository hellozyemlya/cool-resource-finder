package hellozyemlya.compat.client

import net.minecraft.client.MinecraftClient

val MinecraftClient.compatTickDelta: Float
    get() = this.renderTickCounter.getTickDelta(true)