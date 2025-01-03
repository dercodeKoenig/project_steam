package ProjectSteamCrafting.JEI;

import ProjectSteamCrafting.MillStone.MillStoneConfig;
import ProjectSteamCrafting.Sieve.SieveConfig;
import ProjectSteamCrafting.SpinningWheel.SpinningWheelConfig;
import ProjectSteamCrafting.WoodMill.WoodMillConfig;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static ProjectSteamCrafting.Registry.*;

@JeiPlugin
public class myPlugin implements IModPlugin {

    IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("projectsteam_crafting", "plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        //this is delayed to register only after recipes are synced to client
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SieveCategory());
        registration.addRecipeCategories(new SpinningWheelCategory());
        registration.addRecipeCategories(new WoodMillCategory());
        registration.addRecipeCategories(new MillStoneCategory());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(SIEVE.get()), SieveCategory.recipeType);
        registration.addRecipeCatalyst(new ItemStack(SPINNING_WHEEL.get()), SpinningWheelCategory.recipeType);
        registration.addRecipeCatalyst(new ItemStack(WOODMILL.get()), WoodMillCategory.recipeType);
        registration.addRecipeCatalyst(new ItemStack(MILLSTONE.get()), MillStoneCategory.recipeType);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        SieveConfig.PacketConfigSync.jeiRunnableOnConfigLoad = new Runnable() {
            @Override
            public void run() {
                runtime.getRecipeManager().addRecipes(SieveCategory.recipeType, SieveConfig.INSTANCE.recipes);
            }
        };
        SpinningWheelConfig.PacketConfigSync.jeiRunnableOnConfigLoad = new Runnable() {
            @Override
            public void run() {
                runtime.getRecipeManager().addRecipes(SpinningWheelCategory.recipeType, SpinningWheelConfig.INSTANCE.recipes);
            }
        };
        WoodMillConfig.PacketConfigSync.jeiRunnableOnConfigLoad = new Runnable() {
            @Override
            public void run() {
                runtime.getRecipeManager().addRecipes(WoodMillCategory.recipeType, WoodMillConfig.INSTANCE.recipes);
            }
        };
        MillStoneConfig.PacketConfigSync.jeiRunnableOnConfigLoad = new Runnable() {
            @Override
            public void run() {
                runtime.getRecipeManager().addRecipes(MillStoneCategory.recipeType, MillStoneConfig.INSTANCE.recipes);
            }
        };
    }
}
