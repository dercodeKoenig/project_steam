package ResearchSystem.jei;

import ResearchSystem.EngineeringStation.recipeConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import mezz.jei.api.JeiPlugin;

@JeiPlugin
public class myPlugin implements IModPlugin {

    IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("research_station", "plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        //registration.addRecipes(RealNiceJeiCategory.recipeType, recipeConfig.INSTANCE.recipeList);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RealNiceJeiCategory());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        //registration.addRecipeCatalyst();
    }

    // I refuse to use the built in recipe system because it is complicated and i like to do things my way
    // so i made the recipe config execute this runnable on config sync
    public void onConfigReceived() {
        runtime.getRecipeManager().addRecipes(RealNiceJeiCategory.recipeType, recipeConfig.INSTANCE.recipeList);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        recipeConfig.jeiRunnableOnConfigLoad = this::onConfigReceived;
    }
}