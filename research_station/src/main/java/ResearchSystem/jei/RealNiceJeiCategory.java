package ResearchSystem.jei;

import ARLib.utils.ItemUtils;
import ResearchSystem.EngineeringStation.recipeConfig;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public  class RealNiceJeiCategory implements IRecipeCategory<recipeConfig.Recipe> {

    public RealNiceJeiCategory() {
    }
     public static final RecipeType<recipeConfig.Recipe> recipeType = new RecipeType<>(
             ResourceLocation.fromNamespaceAndPath("research_station", "recipe_crafting"),
    recipeConfig.Recipe.class
        );

    @Override
    public RecipeType<recipeConfig.Recipe> getRecipeType() {
return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("Research Recipe");
    }
    @Override
    public int getWidth(){
        return 140;
    }
    @Override
    public int getHeight(){
        return 90;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, recipeConfig.Recipe recipe, IFocusGroup focuses) {


        // Define inputs
        for (int y = 0; y < recipe.pattern.size(); y++) {
            String row = recipe.pattern.get(y);
            for (int x = 0; x < row.length(); x++) {
                Character c = row.charAt(x);
                recipeConfig.RecipeInput input = recipe.keys.get(String.valueOf(c));
                if (input == null) continue; // empty slot, (' ')
                String id = input.input.id;
                int num = input.input.amount;
                ItemStack required = ItemUtils.getItemStackFromid(id, num);
                IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x * 18+10, y * 18+30);
                if (required != null) {
                    slot.addItemStack(required);
                } else {
                    ItemStack[] thisCouldAllBeEasier = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.tryParse(id))).getItems();
                    for (int i = 0; i < thisCouldAllBeEasier.length; i++) {
                        thisCouldAllBeEasier[i].setCount(num);
                        slot.addItemStack(thisCouldAllBeEasier[i]);
                    }
                }
            }
        }


        String id = recipe.output.id;
        int num = recipe.output.amount;
        ItemStack required = ItemUtils.getItemStackFromid(id, num);
        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 49);

        if (required != null) {
            slot.addItemStack(required);
        } else {
            ItemStack[] thisCouldAllBeEasier = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.tryParse(id))).getItems();
            for (int i = 0; i < thisCouldAllBeEasier.length; i++) {
                thisCouldAllBeEasier[i].setCount(num);
                slot.addItemStack(thisCouldAllBeEasier[i]);
            }
        }


    }

    @Override
    public void draw(recipeConfig.Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Optional: Draw custom text or graphics, such as energy cost.
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("Required Research:"),
                0, 0,
                0xFF404040,false
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable(recipe.requiredResearch),
                0, 10,
                0xFF404040,false
        );

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                guiGraphics.blit(
                        ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/gui_item_slot_background.png"),
                        x*18+10, y*18+30,
                        18, 18,
                        0, 0,
                        18, 18,
                        18, 18
                );
            }
        }
        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/arrow_right.png"),
                70, 45,
                24, 18,
                0f, 0f,
                16, 12,
                16, 12
        );
        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/gui_item_slot_background.png"),
                105, 45,
                24, 24,
                0, 0,
                18, 18,
                18, 18
        );
    }

    @Override
    public boolean isHandled(recipeConfig.Recipe recipe) {
        // Define whether this recipe should be shown or filtered out.
        return true;
    }
}

