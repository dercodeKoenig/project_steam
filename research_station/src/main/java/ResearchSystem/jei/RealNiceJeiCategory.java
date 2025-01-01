package ResearchSystem.jei;

import ARLib.utils.ItemUtils;
import ARLib.utils.RecipePart;
import ARLib.utils.RecipePartWithProbability;
import ResearchSystem.Config.RecipeConfig;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public  class RealNiceJeiCategory implements IRecipeCategory<RecipeConfig.Recipe> {

    public RealNiceJeiCategory() {
    }

    public static final RecipeType<RecipeConfig.Recipe> recipeType = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath("research_station", "recipe_crafting"),
            RecipeConfig.Recipe.class
    );

    @Override
    public RecipeType<RecipeConfig.Recipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("Research Recipe");
    }

    @Override
    public int getWidth() {
        return 140;
    }

    @Override
    public int getHeight() {
        return 90;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }



    void setSlotItemsFromRecipePart(RecipePart recipePart, IRecipeSlotBuilder     slot){
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeConfig.Recipe recipe, IFocusGroup focuses) {


        // Define inputs
        for (int y = 0; y < recipe.pattern.size(); y++) {
            String row = recipe.pattern.get(y);
            for (int x = 0; x < row.length(); x++) {
                Character c = row.charAt(x);
                RecipeConfig.RecipeInput input = recipe.keys.get(String.valueOf(c));
                if (input == null) continue; // empty slot, (' ')
                IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 10, y * 18 + 30);
                setSlotItemsFromRecipePart(input.input,slot);
            }
        }

        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 49);
        setSlotItemsFromRecipePart(recipe.output,slot);
    }

    @Override
    public void draw(RecipeConfig.Recipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Optional: Draw custom text or graphics, such as energy cost.
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable("Required Research:"),
                0, 0,
                0xFF404040, false
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                Component.translatable(recipe.requiredResearch),
                0, 10,
                0xFF404040, false
        );

        guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/arrow_right.png"),
                70, 45,
                24, 18,
                0f, 0f,
                16, 12,
                16, 12
        );
    }

    @Override
    public boolean isHandled(RecipeConfig.Recipe recipe) {
        // Define whether this recipe should be shown or filtered out.
        return true;
    }
}

