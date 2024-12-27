package ResearchSystem.EngineeringStation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import static ResearchSystem.Registry.ENTITY_ENGINEERING_STATION;

public class EntityEngineeringStation extends BlockEntity {

    public ItemStackHandlerWithStackAccess craftingInventory = new ItemStackHandlerWithStackAccess(9){
        @Override
        public void onContentsChanged(int slot){
            setChanged();
            updateCraftingContainerFromCraftingInventory();
            resultContainer.setItem(0,new ItemStack(Items.COAL,1));
        }
    };
    public AFuckingCraftingContainer craftingContainer = new AFuckingCraftingContainer(3,3,craftingInventory.getStacks());
    void updateCraftingContainerFromCraftingInventory(){
        for (int i = 0; i < craftingInventory.getSlots(); i++) {
            craftingContainer.setItem(i,craftingInventory.getStackInSlot(i));
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

    public void tick(){

    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityEngineeringStation) t).tick();
    }
}
