package WorkSites.FishFarm;


import WorkSites.BlockWorkSiteBase;
import WorkSites.CropFarm.EntityCropFarm;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static WorkSites.Registry.ENTITY_FISH_FARM;

public class BlockFishFarm extends BlockWorkSiteBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_FISH_FARM.get().create(blockPos, blockState);
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity e = level.getBlockEntity(pos);
        if(e instanceof EntityFishFarm farm){
            for (int i = 0; i <farm.inputsInventory.getSlots() ; i++) {
                Block.popResource(level,pos,farm.inputsInventory.getStackInSlot(i).copy());
                farm.inputsInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
            for (int i = 0; i <farm.specialResourcesInventory.getSlots() ; i++) {
                Block.popResource(level,pos,farm.specialResourcesInventory.getStackInSlot(i).copy());
                farm.specialResourcesInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
            for (int i = 0; i <farm.mainInventory.getSlots() ; i++) {
                Block.popResource(level,pos,farm.mainInventory.getStackInSlot(i).copy());
                farm.mainInventory.setStackInSlot(i, ItemStack.EMPTY);
            }
            farm.setChanged();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

}
