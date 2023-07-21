package hellozyemlya.resourcefinder.mixin;

import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    private static void init() {
        Items.AIR.superPuperMethod();
    }
}
