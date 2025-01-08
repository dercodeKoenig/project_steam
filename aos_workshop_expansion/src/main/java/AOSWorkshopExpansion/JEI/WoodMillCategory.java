package AOSWorkshopExpansion.JEI;


import AOSWorkshopExpansion.Main;
import ARLib.utils.ItemUtils;
import ARLib.utils.RecipePart;
import ARLib.utils.RecipePartWithProbability;
import AOSWorkshopExpansion.WoodMill.WoodMillConfig;
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

public  class WoodMillCategory implements IRecipeCategory<WoodMillConfig.WoodMillRecipe> {

    public WoodMillCategory() {
    }

    public static final RecipeType<WoodMillConfig.WoodMillRecipe> recipeType = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(Main.MODID, "wood_mill_recipe"),
            WoodMillConfig.WoodMillRecipe.class
    );

    @Override
    public RecipeType<WoodMillConfig.WoodMillRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("Wood Mill Recipe");
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 120;
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
    public void setRecipe(IRecipeLayoutBuilder builder, WoodMillConfig.WoodMillRecipe recipe, IFocusGroup focuses) {


        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, 50, 0);
        setSlotItemsFromRecipePart(recipe.inputItem,slot);

        int n = 0;
        int w = 7;
        for(RecipePartWithProbability r : recipe.outputItems){
            int x = n % w * 18 + 10;
            int y = n / w * 18 + 40;
            IRecipeSlotBuilder slotOutput = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y);
            setSlotItemsFromRecipePart(r,slotOutput);
            n++;
        }
    }

    @Override
    public void draw(WoodMillConfig.WoodMillRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/arrow_down.png"),
                63, 20,
                12, 12,
                0f, 0f,
                12, 16,
                12, 16
        );
    }

    @Override
    public boolean isHandled(WoodMillConfig.WoodMillRecipe recipe) {
        // Define whether this recipe should be shown or filtered out.
        return true;
    }
}

