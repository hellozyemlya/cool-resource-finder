package hellozyemlya.resourcefinder.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import hellozyemlya.resourcefinder.ResourceRegistry;
import hellozyemlya.resourcefinder.items.recipes.ResourceFinderCompassChargeRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Accessor
    abstract Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();
    @Accessor
    abstract void setRecipes(Map<RecipeType<?>, Map<Identifier, Recipe<?>>> value);

    @Accessor
    abstract Map<Identifier, Recipe<?>> getRecipesById();
    @Accessor
    abstract void setRecipesById(Map<Identifier, Recipe<?>> value);

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
    at=@At("TAIL"))
    void injectCompassResources(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci){
        HashMap<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> recipes = Maps.newHashMap();
        getRecipes().forEach((recipeType, identifierRecipeMap) -> {
            ImmutableMap.Builder<Identifier, Recipe<?>> builder = ImmutableMap.builder();
            builder.putAll(identifierRecipeMap);
            recipes.put(recipeType, builder);
        });

        ImmutableMap.Builder<Identifier, Recipe<?>> recipesById = ImmutableMap.builder();
        recipesById.putAll(getRecipesById());


        ResourceRegistry.Companion.getINSTANCE().getRechargeRecipes().forEach(recipe -> {
            recipes.get(recipe.getType()).put(recipe.getId(), recipe);
            recipesById.put(recipe.getId(), recipe);
        });

        setRecipes(recipes.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build())));
        setRecipesById(recipesById.build());
    }
}
