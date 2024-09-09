package hellozyemlya.resourcefinder.datagen

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class ModDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()
        pack.addProvider(::ModRecipeProvider)
        pack.addProvider(::UaLangProvider)
        pack.addProvider(::EngLangProvider)
    }
}