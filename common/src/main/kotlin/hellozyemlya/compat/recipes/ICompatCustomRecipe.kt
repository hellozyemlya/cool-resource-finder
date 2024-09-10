package hellozyemlya.compat.recipes

import net.minecraft.item.ItemStack
import net.minecraft.util.collection.DefaultedList

interface ICompatCustomRecipe {
    fun matches(inputStacks: List<ItemStack>): Boolean
    fun craft(inputStacks: List<ItemStack>): ItemStack
    fun getRemainder(inputStacks: List<ItemStack>): DefaultedList<ItemStack>
    fun fits(width: Int, height: Int): Boolean
}