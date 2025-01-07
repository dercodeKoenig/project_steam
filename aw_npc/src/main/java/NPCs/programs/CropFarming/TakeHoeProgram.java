package NPCs.programs.CropFarming;

import Farms.CropFarm.EntityCropFarm;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class TakeHoeProgram {

    CropFarmingProgram parentProgram;
    int workDelay = 0;
    int cachedHoeIndex = 0;

    public TakeHoeProgram(CropFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public boolean takeHoeToMainHand(){
        ItemStack stackInHand = parentProgram.worker.getMainHandItem();
        if(stackInHand.getItem() instanceof HoeItem) return true;
        if(!hasHoe()) return false;

        ItemStack hoeStack = parentProgram.worker.combinedInventory.getStackInSlot(cachedHoeIndex).copy();
        if(hoeStack.getItem() instanceof HoeItem){
            // this should always be true now
             parentProgram.worker.combinedInventory.setStackInSlot(cachedHoeIndex, stackInHand.copy());
             parentProgram.worker.setItemInHand(InteractionHand.MAIN_HAND, hoeStack);
             return true;
        }

        return false;
    }

    public boolean hasHoe() {
        if (parentProgram.worker.combinedInventory.getStackInSlot(cachedHoeIndex).getItem() instanceof HoeItem)
            return true;

        for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
            if(parentProgram.worker.combinedInventory.getStackInSlot(i).getItem() instanceof HoeItem){
                cachedHoeIndex = i;
                return true;
            }
        }

        return false;
    }

    public boolean canPickupHoeFromFarm(EntityCropFarm farm) {
        if(hasHoe()) return false;

        // check if he has space to hold the hoe
        boolean hasEmptySlot = false;
        for (int i = 0; i <parentProgram.worker.combinedInventory.getSlots() ; i++) {
            if (parentProgram.worker.combinedInventory.getStackInSlot(i).isEmpty()) {
                hasEmptySlot = true;
                break;
            }
        }
        if(!hasEmptySlot) return false;

        for (int j = 0; j < farm.inputsInventory.getSlots(); j++) {
            ItemStack stackInSlot = farm.inputsInventory.getStackInSlot(j);
            if (stackInSlot.getItem() instanceof HoeItem) {
                return true;
            }
        }
        for (int j = 0; j < farm.specialResourcesInventory.getSlots(); j++) {
            ItemStack stackInSlot = farm.specialResourcesInventory.getStackInSlot(j);
            if (stackInSlot.getItem() instanceof HoeItem) {
                return true;
            }
        }
        for (int j = 0; j < farm.mainInventory.getSlots(); j++) {
            ItemStack stackInSlot = farm.mainInventory.getStackInSlot(j);
            if (stackInSlot.getItem() instanceof HoeItem) {
                return true;
            }
        }
        return false;
    }

    public ExitCode run() {
        if (!canPickupHoeFromFarm(parentProgram.currentFarm)) {
            return ExitCode.EXIT_SUCCESS;
        }

        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, parentProgram.currentFarm.getBlockPos().getCenter());
        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, parentProgram.currentFarm.getBlockPos().getCenter());

        ExitCode pathFindExit = parentProgram.moveNearFarm(3);
        if (pathFindExit.isFailed())
            return ExitCode.EXIT_FAIL;
        else if (pathFindExit.isStillRunning()) {
            workDelay = 0;
            return ExitCode.SUCCESS_STILL_RUNNING;
        }

        if (workDelay > 20) {
            workDelay = 0;
            for (int j = 0; j < parentProgram.currentFarm.mainInventory.getSlots(); j++) {
                ItemStack stackInSlot = parentProgram.currentFarm.mainInventory.getStackInSlot(j);
                if (stackInSlot.getItem() instanceof HoeItem) {
                    for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
                        if(parentProgram.worker.combinedInventory.getStackInSlot(i).isEmpty()){
                            parentProgram.worker.combinedInventory.setStackInSlot(i,stackInSlot);
                            parentProgram.currentFarm.mainInventory.setStackInSlot(j, ItemStack.EMPTY);
                            parentProgram.currentFarm.setChanged();
                            parentProgram.worker.swing(ProgramUtils.moveItemStackToAnyHand(stackInSlot,parentProgram.worker));
                            return ExitCode.EXIT_SUCCESS;
                        }
                    }
                }
            }
            for (int j = 0; j < parentProgram.currentFarm.inputsInventory.getSlots(); j++) {
                ItemStack stackInSlot = parentProgram.currentFarm.inputsInventory.getStackInSlot(j);
                if (stackInSlot.getItem() instanceof HoeItem) {
                    for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
                        if(parentProgram.worker.combinedInventory.getStackInSlot(i).isEmpty()){
                            parentProgram.worker.combinedInventory.setStackInSlot(i,stackInSlot);
                            parentProgram.currentFarm.inputsInventory.setStackInSlot(j, ItemStack.EMPTY);
                            parentProgram.currentFarm.setChanged();
                            parentProgram.worker.swing(ProgramUtils.moveItemStackToAnyHand(stackInSlot,parentProgram.worker));
                            return ExitCode.EXIT_SUCCESS;
                        }
                    }
                }
            }

            for (int j = 0; j < parentProgram.currentFarm.specialResourcesInventory.getSlots(); j++) {
                ItemStack stackInSlot = parentProgram.currentFarm.specialResourcesInventory.getStackInSlot(j);
                if (stackInSlot.getItem() instanceof HoeItem) {
                    for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
                        if(parentProgram.worker.combinedInventory.getStackInSlot(i).isEmpty()){
                            parentProgram.worker.combinedInventory.setStackInSlot(i,stackInSlot);
                            parentProgram.currentFarm.specialResourcesInventory.setStackInSlot(j, ItemStack.EMPTY);
                            parentProgram.currentFarm.setChanged();
                            parentProgram.worker.swing(ProgramUtils.moveItemStackToAnyHand(stackInSlot,parentProgram.worker));
                            return ExitCode.EXIT_SUCCESS;
                        }
                    }
                }
            }
            return ExitCode.EXIT_FAIL; // should never trigger because first line in run() checks if it can take hoe
        }
        workDelay++;
        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
