package NPCs.programs.CropFarming;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import NPCs.programs.CropFarmingProgram;
import NPCs.programs.ExitCode;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class TakeSeedsProgram {
    CropFarmingProgram parentProgram;
    int workDelay = 0;
    int scanInterval = 20*10;
    boolean hasWork = false;
    long lastScan = 0;

    int requiredDistance = 3;

    public TakeSeedsProgram(CropFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }


    public static boolean hasAnySeedItem(EntityCropFarm farm, WorkerNPC worker) {
        for (int i = 0; i < worker.inventory.getSlots(); i++) {
            ItemStack stackInSlot = worker.inventory.getStackInSlot(i);
            if (farm.isItemValidSeed(stackInSlot)) {
                return true;
            }
        }
        return false;
    }

    public static boolean takeOneSeedFromFarm(EntityCropFarm farm, WorkerNPC worker, boolean simulate) {
        // check if the farm has any seed and if worker has space for seed
        for (int i = 0; i < farm.inputsInventory.getSlots(); i++) {
            ItemStack canExtract = farm.inputsInventory.extractItem(i, 1, true);
            if (!canExtract.isEmpty()) {
                for (int j = 0; j < worker.inventory.getSlots(); j++) {
                    ItemStack notInserted = worker.inventory.insertItem(j, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            // if it can be inserted, do the actual movement and break
                            ItemStack extracted = farm.inputsInventory.extractItem(j, 1, false);
                            worker.inventory.insertItem(i, extracted, false);
                            worker.setItemInHand(InteractionHand.OFF_HAND, worker.inventory.getStackInSlot(i));
                            worker.swing(InteractionHand.OFF_HAND);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void recalculateHasWork(){
        hasWork = true;

        // if the worker already has one or more seed items he does not need to reload
        // but we want him not to load 1 item and run away and come back all the time
        // so if he is currently near the farm, count how many seeds he has and only if he has enough exit
        if (parentProgram.cachedDistanceToFarm > requiredDistance) {
            if (hasAnySeedItem(parentProgram.currentFarm, parentProgram.worker))
                hasWork = false;
        }
        else{
            int seedItemsCount = 0;
            for (int i = 0; i < parentProgram.worker.inventory.getSlots(); i++) {
                ItemStack stackInSlot = parentProgram.worker.inventory.getStackInSlot(i);
                if (parentProgram.currentFarm.isItemValidSeed(stackInSlot)) {
                    seedItemsCount += stackInSlot.getCount();
                }
            }
            if (seedItemsCount > 32) {
                hasWork = false;
            }
        }


        // if previous filters did not prevent from wanting to take up items,
        // check if it can not pick up any seeds from the farm because inventory full or no seeds in farm, do not run
        if(hasWork) {
            if (!takeOneSeedFromFarm(parentProgram.currentFarm, parentProgram.worker, true)) {
                hasWork = false;
            }
        }

    }

    public ExitCode run() {
        long gameTime = parentProgram.worker.level().getGameTime();
        if(gameTime > lastScan+scanInterval) {
            lastScan = gameTime;
            recalculateHasWork();
        }

        if(!hasWork){
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
            // try to take 1 seed item
            takeOneSeedFromFarm(parentProgram.currentFarm,parentProgram.worker, false);
            recalculateHasWork();
        }
        workDelay++;

        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
