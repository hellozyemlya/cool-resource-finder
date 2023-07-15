package hellozyemlya.resourcefinder.items.recipes

import com.google.gson.JsonObject
import hellozyemlya.resourcefinder.ResourceFinder
import hellozyemlya.resourcefinder.ResourceRegistry
import hellozyemlya.resourcefinder.items.ResourceFinderCompass
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.CraftingRecipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.book.CraftingRecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.*
import java.util.stream.IntStream

class ResourceFinderCompassChargeRecipe(private val id: Identifier) : CraftingRecipe {
    override fun matches(inventory: CraftingInventory, world: World): Boolean {
        val allStacks = IntStream.range(0, inventory.size())
            .mapToObj { slot: Int -> inventory.getStack(slot) }
            .filter { itemStack: ItemStack -> !itemStack.isEmpty }.toList()
        val compassStack = allStacks.stream()
            .filter { itemStack: ItemStack -> itemStack.item === ResourceFinder.RESOURCE_FINDER_ITEM }
            .findFirst()
        if (compassStack.isEmpty) return false
        val otherStack = allStacks.stream()
            .filter { itemStack: ItemStack -> itemStack.item !== ResourceFinder.RESOURCE_FINDER_ITEM }
            .findFirst()
        if (otherStack.isEmpty) return false
        val resourceEntry: Optional<ResourceRegistry.ResourceEntry> =
            ResourceRegistry.INSTANCE.getByChargingItem(otherStack.get().item)
        return !resourceEntry.isEmpty
    }

    override fun craft(inventory: CraftingInventory, registryManager: DynamicRegistryManager): ItemStack {
        val allStacks = IntStream.range(0, inventory.size())
            .mapToObj { slot: Int -> inventory.getStack(slot) }
            .filter { itemStack: ItemStack -> !itemStack.isEmpty }.toList()
        val compassStack = allStacks.stream()
            .filter { itemStack: ItemStack -> itemStack.item === ResourceFinder.RESOURCE_FINDER_ITEM }
            .findFirst()
        if (compassStack.isEmpty) return ItemStack.EMPTY
        val otherStack = allStacks.stream()
            .filter { itemStack: ItemStack -> itemStack.item !== ResourceFinder.RESOURCE_FINDER_ITEM }
            .findFirst()
        if (otherStack.isEmpty) return ItemStack.EMPTY
        val resourceEntry: Optional<ResourceRegistry.ResourceEntry> =
            ResourceRegistry.INSTANCE.getByChargingItem(otherStack.get().item)
        if (resourceEntry.isEmpty) return ItemStack.EMPTY
        val newStack = compassStack.get().copy()

        val scanNbt = ResourceFinderCompass.ScanNbt(newStack)
        val scanEntry = scanNbt.createOrGetEntryFor(resourceEntry.get())
        scanEntry.lifetime += resourceEntry.get().getChargeTicks(otherStack.get().item)
        scanNbt.write(newStack)

        return newStack
    }

    override fun fits(width: Int, height: Int): Boolean {
        return true
    }

    override fun getOutput(registryManager: DynamicRegistryManager): ItemStack {
        return ResourceFinder.RESOURCE_FINDER_ITEM.defaultStack
    }

    override fun getId(): Identifier {
        return id
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return Serializer.INSTANCE
    }

    override fun getCategory(): CraftingRecipeCategory {
        return CraftingRecipeCategory.EQUIPMENT
    }

    class Serializer : RecipeSerializer<ResourceFinderCompassChargeRecipe> {
        // Turns json into Recipe
        override fun read(id: Identifier, json: JsonObject): ResourceFinderCompassChargeRecipe {
            return ResourceFinderCompassChargeRecipe(id)
        }

        // Turns Recipe into PacketByteBuf
        override fun write(packetData: PacketByteBuf, recipe: ResourceFinderCompassChargeRecipe) {}

        // Turns PacketByteBuf into Recipe
        override fun read(id: Identifier, packetData: PacketByteBuf): ResourceFinderCompassChargeRecipe? {
            return null
        }

        companion object {
            val INSTANCE: Serializer = Serializer()
            val ID = Identifier("resource_finder:compass3d_recharge")
        }
    }
}
