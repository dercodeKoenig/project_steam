package NPCs.programs.CropFarming;

import WorkSites.CropFarm.EntityCropFarm;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class TakeHoeProgram {

    MainCropFarmingProgram parentProgram;
    int workDelay = 0;
    int cachedHoeIndex = 0;
    int requiredDistance = 2;

    public TakeHoeProgram(MainCropFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public boolean takeHoeToMainHand(){
        ItemStack stackInHand = parentProgram.worker.getMainHandItem();
        if(stackInHand.getItem() instanceof HoeItem) return true;
        if(!hasHoe()) return false;

        ItemStack hoeStack = parentProgram.worker.combinedInventory.getStackInSlot(cachedHoeIndex);
        if(hoeStack.getItem() instanceof HoeItem){
            ProgramUtils.moveItemStackToMainHand(hoeStack,parentProgram.worker);
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


        ExitCode pathFindExit = parentProgram.moveNearFarm(requiredDistance);
        if (pathFindExit.isFailed()) {
            return ExitCode.EXIT_FAIL;
        }
        else if (pathFindExit.isStillRunning()) {
            workDelay = 0;
            return ExitCode.SUCCESS_STILL_RUNNING;
        }

        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, parentProgram.currentFarm.getBlockPos().getCenter());
        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, parentProgram.currentFarm.getBlockPos().getCenter());


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
                            return ExitCode.SUCCESS_STILL_RUNNING;
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
                            return ExitCode.SUCCESS_STILL_RUNNING;
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
                            return ExitCode.SUCCESS_STILL_RUNNING;
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
