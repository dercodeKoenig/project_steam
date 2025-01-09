package NPCs.programs.CropFarming;

import NPCs.WorkerNPC;
import NPCs.programs.MainFarmingProgram;
import WorkSites.CropFarm.EntityCropFarm;

import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TakeSeedsProgram {
    MainFarmingProgram parentProgram;
    int workDelay = 0;
    int scanInterval = 20 * 30;
    boolean hasWork = false;
    long lastScan = 0;
    int requiredDistance = 2;

    public TakeSeedsProgram(MainFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }


    public static boolean isValidSeedItem(EntityCropFarm farm, ItemStack stack) {
        boolean ret = (EntityCropFarm.isItemValidSeed(stack) &&
                canPlantAnywhere(stack, farm) != null &&
                PlantProgram.isAllowedToPlantItemByWorkOrder(stack, farm));
        //System.out.println("seed item:"+stack+":"+ret);
        return ret;
    }

    public static boolean workerHasAnyValidSeedItem(EntityCropFarm farm, WorkerNPC worker) {
        for (int i = 0; i < worker.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = worker.combinedInventory.getStackInSlot(i);
            if (isValidSeedItem(farm, stackInSlot)) return true;
        }
        return false;
    }

    public static BlockPos canPlantAnywhere(ItemStack seed, EntityCropFarm farm) {
        Item var5 = seed.getItem();
        for (BlockPos p : farm.positionsToPlant) {
            if (var5 instanceof BlockItem bi) {
                if (bi.getBlock().defaultBlockState().canSurvive(farm.getLevel(), p)) {
                    return p;
                }
            }
        }
        return null;
    }

    public static ItemStack takeOneSeedFromFarm(EntityCropFarm farm, WorkerNPC worker, boolean simulate) {
        // check if the farm has any seed and if worker has space for seed
        // farm can also have hoe in the inputs inventory, so verify
        for (int i = 0; i < farm.inputsInventory.getSlots(); i++) {
            ItemStack canExtract = farm.inputsInventory.extractItem(i, 1, true);
            if (!canExtract.isEmpty() && isValidSeedItem(farm, canExtract)) {
                for (int j = 0; j < worker.combinedInventory.getSlots(); j++) {
                    ItemStack notInserted = worker.combinedInventory.insertItem(j, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            // if it can be inserted, do the actual movement and break
                            ItemStack extracted = farm.inputsInventory.extractItem(i, 1, false);
                            worker.combinedInventory.insertItem(j, extracted, false);
                            InteractionHand movedTo = ProgramUtils.moveItemStackToAnyHand(worker.combinedInventory.getStackInSlot(j), worker);
                            worker.swing(movedTo);
                        }
                        return canExtract.copy();
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean recalculateHasWork(EntityCropFarm farm) {
        hasWork = true;

        if (farm.positionsToPlant.isEmpty()) hasWork = false;

        // if the worker already has one or more seed items he does not need to reload
        // but we want him not to load 1 item and run away and come back all the time
        // so if he is currently near the farm, count how many seeds he has and only if he has enough exit
        if (parentProgram.worker.cachedDistanceManhattanToWorksite > requiredDistance+2 || parentProgram.currentFarm == null) {
            if (workerHasAnyValidSeedItem(farm, parentProgram.worker))
                hasWork = false;
        } else {
            int seedItemsCount = 0;
            for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
                ItemStack stackInSlot = parentProgram.worker.combinedInventory.getStackInSlot(i);
                if (isValidSeedItem(farm, stackInSlot)) {
                    seedItemsCount += stackInSlot.getCount();
                }
            }
            if (seedItemsCount >= Math.min(farm.positionsToPlant.size(), 32)) {
                hasWork = false;
            }
        }


        // if previous filters did not prevent from wanting to take up items,
        // check if it can not pick up any seeds from the farm because inventory full or no seeds in farm, do not run
        if (hasWork) {
            if (takeOneSeedFromFarm(farm, parentProgram.worker, true) == ItemStack.EMPTY) {
                hasWork = false;
            }
        }
        return hasWork;
    }

    public ExitCode run() {
        long gameTime = parentProgram.worker.level().getGameTime();
        if (gameTime > lastScan + scanInterval) {
            lastScan = gameTime;
            recalculateHasWork(parentProgram.currentFarm);
        }
        if (!hasWork) {
            return ExitCode.EXIT_SUCCESS;
        }

        ExitCode pathFindExit = parentProgram.moveNearFarm(requiredDistance);
        if (pathFindExit.isFailed())
            return ExitCode.EXIT_FAIL;
        else if (pathFindExit.isStillRunning()) {
            workDelay = 0;
            return ExitCode.SUCCESS_STILL_RUNNING;
        }

        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, parentProgram.currentFarm.getBlockPos().getCenter());
        parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, parentProgram.currentFarm.getBlockPos().getCenter());

        if (workDelay > 10) {
            workDelay = 0;
            // try to take 1 seed item
            takeOneSeedFromFarm(parentProgram.currentFarm, parentProgram.worker, false);

            parentProgram.plantProgram.recalculateHasWork(parentProgram.currentFarm);
            parentProgram.unloadInventoryProgram.recalculateHasWork(parentProgram.currentFarm);
            recalculateHasWork(parentProgram.currentFarm);
        }
        workDelay++;

        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
