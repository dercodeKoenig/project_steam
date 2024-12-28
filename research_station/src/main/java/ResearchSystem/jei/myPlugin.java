package ResearchSystem.jei;

import ARLib.network.PacketBlockEntity;
import ARLib.utils.DimensionUtils;
import ARLib.utils.ItemUtils;
import ARLib.utils.RecipePart;
import ResearchSystem.EngineeringStation.MenuEngineeringStation;
import ResearchSystem.EngineeringStation.recipeConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mezz.jei.api.IModPlugin;
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
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import mezz.jei.api.JeiPlugin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.compress.harmony.unpack200.IMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ResearchSystem.Registry.ITEM_ENGINEERING_STATION;
import static ResearchSystem.Registry.MENU_ENGINEERING_STATION;

@JeiPlugin
public class myPlugin implements IModPlugin {

    IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("research_station", "plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        //this is delayed to register only after recipes are synced to client
        //registration.addRecipes(RealNiceJeiCategory.recipeType, recipeConfig.INSTANCE.recipeList);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new RealNiceJeiCategory());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ITEM_ENGINEERING_STATION.get()), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(ITEM_ENGINEERING_STATION.get(),RealNiceJeiCategory.recipeType);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        IUniversalRecipeTransferHandler<MenuEngineeringStation> whateverbs = new IUniversalRecipeTransferHandler<>() {
            @Override
            public @NotNull Class<MenuEngineeringStation> getContainerClass() {
                return MenuEngineeringStation.class;
            }

            @Override
            public @NotNull Optional<MenuType<MenuEngineeringStation>> getMenuType() {
                return Optional.of(MENU_ENGINEERING_STATION.get());
            }


            @Override
            /**
             * @param container   the container to act on
             * @param recipe      the raw recipe instance
             * @param recipeSlots the view of the recipe slots, with information about the ingredients
             * @param player      the player, to do the slot manipulation
             * @param maxTransfer if true, transfer as many items as possible. if false, transfer one set
             * @param doTransfer  if true, do the transfer. if false, check for errors but do not actually transfer the items
             * @return a recipe transfer error if the recipe can't be transferred. Return null on success.
             *
             * @since 19.8.1
             */
            @Nullable
            public IRecipeTransferError transferRecipe(
                    MenuEngineeringStation container,
                    Object recipe,
                    IRecipeSlotsView recipeSlots,
                    Player player,
                    boolean maxTransfer,
                    boolean doTransfer
            ) {
                // I send the recipe as json to the server so the server can place the items
                if (doTransfer) {
                    Gson gson = new Gson();
                    String data = "";
                    if (recipe instanceof recipeConfig.Recipe h) {
                        List<List<String>> requiredItems = new ArrayList<>();
                        for (int y = 0; y < h.pattern.size(); y++) {
                            for (int x = 0; x < h.pattern.get(y).length(); x++) {
                                List<String> allowedParts = new ArrayList<>();
                                String c = String.valueOf(h.pattern.get(y).charAt(x));
                                if (h.keys.containsKey(c)) {
                                    recipeConfig.RecipeInput i = h.keys.get(c);
                                    allowedParts.add(gson.toJson(i.input));
                                } else {
                                    allowedParts.add(gson.toJson(new RecipePart()));
                                }
                                requiredItems.add(allowedParts);
                            }
                        }
                        data = gson.toJson(requiredItems);
                    }
                    if (recipe instanceof RecipeHolder h) {
                        if (h.value() instanceof ShapelessRecipe s) {
                            List<Ingredient> ings = new ArrayList<>(s.getIngredients());
                            List<List<String>> requiredItems = new ArrayList<>();
                            for (int i = 0; i < 9; i++) {
                                List<String> allowedParts = new ArrayList<>();
                                if (i < ings.size()) {
                                    for (ItemStack allowed : ings.get(i).getItems()) {
                                        allowedParts.add(gson.toJson(new RecipePart(BuiltInRegistries.ITEM.getKey(allowed.getItem()).toString(), 1)));
                                    }
                                } else {
                                    allowedParts.add(gson.toJson(new RecipePart()));
                                }
                                requiredItems.add(allowedParts);
                            }
                            data = gson.toJson(requiredItems);
                        }
                    }

                    if (!data.isEmpty()) {
                        CompoundTag info = new CompoundTag();
                        info.putString("data", data);
                        info.putUUID("uuid", Minecraft.getInstance().player.getUUID());
                        CompoundTag moveItemsTag = new CompoundTag();
                        moveItemsTag.put("moveItems", info);
                        PacketDistributor.sendToServer(
                                PacketBlockEntity.getBlockEntityPacket(Minecraft.getInstance().level, container.CLIENT_myBlockPos, moveItemsTag)
                        );
                    }
                }
                return null;
            }
        };
        registration.addUniversalRecipeTransferHandler(whateverbs);
    }

    // I refuse to use the built in recipe system because it is complicated and i like to do things my way
    // so i made the recipe config execute this runnable on config sync
    public void onConfigReceived() {
        runtime.getRecipeManager().addRecipes(RealNiceJeiCategory.recipeType, recipeConfig.INSTANCE.recipeList);
        System.out.println("delayed jei registration completed");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        recipeConfig.jeiRunnableOnConfigLoad = this::onConfigReceived;
    }
}