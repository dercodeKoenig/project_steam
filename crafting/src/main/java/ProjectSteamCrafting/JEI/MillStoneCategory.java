package ProjectSteamCrafting.JEI;


import ARLib.utils.ItemUtils;
import ARLib.utils.RecipePart;
import ARLib.utils.RecipePartWithProbability;
import ProjectSteamCrafting.MillStone.MillStoneConfig;
import ProjectSteamCrafting.Sieve.SieveConfig;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.elements.DrawableResource;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;

public  class MillStoneCategory implements IRecipeCategory<MillStoneConfig.MillStoneRecipe> {

    public MillStoneCategory() {
    }

    public static final RecipeType<MillStoneConfig.MillStoneRecipe> recipeType = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath("projectsteam_crafting", "millstone_recipe"),
            MillStoneConfig.MillStoneRecipe.class
    );

    @Override
    public RecipeType<MillStoneConfig.MillStoneRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("MillStone Recipe");
    }

    @Override
    public int getWidth() {
        return 70;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }

    void setSlotItemsFromRecipePart(RecipePart recipePart,     IRecipeSlotBuilder     slot){
        String id = recipePart.id;
        int num = recipePart.amount;
        ItemStack required = ItemUtils.getItemStackFromid(id, num);
        if (required != null) {
            slot.addItemStack(required);
        } else {
            ItemStack[] thisCouldAllBeEasier = Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.tryParse(id))).getItems();
            for (int i = 0; i < thisCouldAllBeEasier.length; i++) {
                thisCouldAllBeEasier[i].setCount(num);
                slot.addItemStack(thisCouldAllBeEasier[i]);
            }
        }
        slot.setBackground(new DrawableResource(ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/gui_item_slot_background.png"),0,0,18,18,0,0,0,0,18,18),0,0);
        slot.addRichTooltipCallback(new IRecipeSlotRichTooltipCallback() {
            @Override
            public void onRichTooltip(IRecipeSlotView iRecipeSlotView, ITooltipBuilder iTooltipBuilder) {
                if(recipePart instanceof RecipePartWithProbability rp) {
                    iTooltipBuilder.add(Component.literal(String.valueOf(rp.p*100)+"%"));
                }
            }
        });
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MillStoneConfig.MillStoneRecipe recipe, IFocusGroup focuses) {

        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, 0, 20);
        setSlotItemsFromRecipePart(recipe.inputItem,slot);

        IRecipeSlotBuilder slotOut = builder.addSlot(RecipeIngredientRole.OUTPUT, 50, 20);
        setSlotItemsFromRecipePart(recipe.outputItem,slotOut);
    }

    @Override
    public void draw(MillStoneConfig.MillStoneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/arrow_down.png"),
                25, 20,
                12, 12,
                0f, 0f,
                12, 16,
                11, 16
        );
    }

    @Override
    public boolean isHandled(MillStoneConfig.MillStoneRecipe recipe) {
        // Define whether this recipe should be shown or filtered out.
        return true;
    }
}

