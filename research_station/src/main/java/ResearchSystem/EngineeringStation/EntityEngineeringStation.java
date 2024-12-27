package ResearchSystem.EngineeringStation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;

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
            resultContainer.setItem(0,result);
        }else{
            resultContainer.setItem(0,ItemStack.EMPTY);
        }

    }
    public ResultContainer resultContainer = new ResultContainer();

    ItemStackHandler bookInventory = new ItemStackHandler(1){
        @Override
        public void onContentsChanged(int slot){
            setChanged();
        }
    };


    public EntityEngineeringStation( BlockPos pos, BlockState blockState) {
        super(ENTITY_ENGINEERING_STATION.get(), pos, blockState);
    }
@Override
public void onLoad(){
updateCraftingContainerFromCraftingInventory();
}

    public void tick(){

    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityEngineeringStation) t).tick();
    }
}
