package ProjectSteamCrafting.JEI;

import ARLib.network.PacketBlockEntity;
import ARLib.utils.RecipePart;
import ProjectSteamCrafting.MillStone.MillStoneConfig;
import ProjectSteamCrafting.Sieve.SieveConfig;
import ProjectSteamCrafting.SpinningWheel.SpinningWheelConfig;
import ProjectSteamCrafting.WoodMill.WoodMillConfig;
import ResearchSystem.Config.RecipeConfig;
import ResearchSystem.EngineeringStation.MenuEngineeringStation;
import ResearchSystem.jei.RealNiceJeiCategory;
import com.google.gson.Gson;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ProjectSteamCrafting.Registry.*;
import static ResearchSystem.Registry.ITEM_ENGINEERING_STATION;
import static ResearchSystem.Registry.MENU_ENGINEERING_STATION;

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
