package ResearchSystem.EngineeringStation;

import ARLib.ARLib;
import ARLib.utils.ItemUtils;
import ResearchSystem.ItemResearchBook;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

import static ResearchSystem.Registry.ENTITY_ENGINEERING_STATION;

public class EntityEngineeringStation extends BlockEntity {

    public CraftingContainerItemStackHandler craftingInventory = new CraftingContainerItemStackHandler(3,3){
        @Override
        public void onContentsChanged(int slot){
            EntityEngineeringStation.super.setChanged();
            updateCraftingContainerFromCraftingInventory();
        }
    };

    void updateCraftingContainerFromCraftingInventory() {
        if (ServerLifecycleHooks.getCurrentServer() == null || level == null) return;

        CraftingInput craftInput = craftingInventory.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> optional = ServerLifecycleHooks.getCurrentServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftInput, level);
        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> icraftingrecipe = optional.get();
            resultContainer.setRecipeUsed(icraftingrecipe);

            ItemStack result = icraftingrecipe.value().assemble(craftInput, level.registryAccess());
            resultContainer.setItem(0, result);
        } else {
            boolean foundMatch = false;
            for (EngineeringConfig.Recipe r : EngineeringConfig.INSTANCE.recipeList) {
                String[] shrinkedPattern = EngineeringConfig.shrink(r.pattern);
                if (craftInput.width() == shrinkedPattern[0].length() && craftInput.height() == shrinkedPattern.length) {
                    boolean matches = true;
                    for (int i = 0; i < craftInput.height(); ++i) {
                        for (int j = 0; j < craftInput.width(); ++j) {
                            EngineeringConfig.RecipeInput inp = r.keys.get(String.valueOf(shrinkedPattern[i].charAt(j)));
                            String id = inp.input.id;
                            ItemStack itemstack = craftInput.getItem(j, i);
                            if (!ItemUtils.matches(id, itemstack) || itemstack.getCount() < inp.input.amount) {
                                matches = false;
                            }
                        }
                    }
                    if (matches) {
                        ItemStack bookStack = bookInventory.getStackInSlot(0);
                        if(bookStack.getItem() instanceof ItemResearchBook irb) {
                            if (irb.getCompletedResearches_readOnly(bookStack).contains(r.requiredResearch)) {
                                resultContainer.setItem(0, ItemUtils.getItemStackFromId(r.output.id, r.output.amount, level.registryAccess()));
                                foundMatch = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!foundMatch) {
                resultContainer.setItem(0, ItemStack.EMPTY);
            }
        }
    }
    public ResultContainer resultContainer = new ResultContainer();

    ItemStackHandler bookInventory = new ItemStackHandler(1){
        @Override
        public void onContentsChanged(int slot){
            EntityEngineeringStation.super.setChanged();
            updateCraftingContainerFromCraftingInventory();
        }
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof ItemResearchBook;
        }
    };

    ItemStackHandler inputInventory = new ItemStackHandler(18){
        @Override
        public void onContentsChanged(int slot){
            setChanged();
        }
    };


    public EntityEngineeringStation( BlockPos pos, BlockState blockState) {
        super(ENTITY_ENGINEERING_STATION.get(), pos, blockState);
    }
@Override
public void onLoad() {
    if (!level.isClientSide) {
        updateCraftingContainerFromCraftingInventory();
    }
}

    public void tick(){

    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityEngineeringStation) t).tick();
    }
}
