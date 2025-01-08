package NPCs.programs.CropFarming;

import WorkSites.CropFarm.EntityCropFarm;

import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;

public class UnloadInventoryProgram {
        MainCropFarmingProgram parentProgram;
        int scanInterval = 20 * 10;
    int keepSeedNum = 64*1;
    int keepFoodNum = 8;
    int keepHoeNum = 1;
        int requiredDistance = 2;

    int workDelay = 0;
    long lastScan = 0;
    ItemStack nextStackToUnload;

        public UnloadInventoryProgram(MainCropFarmingProgram parentProgram) {
            this.parentProgram = parentProgram;
        }


    public int countSeedsInInventory(){
        int seedItemsCount = 0;
        for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = parentProgram.worker.combinedInventory.getStackInSlot(i);
            if (EntityCropFarm.isItemValidSeed(stackInSlot)) {
                seedItemsCount += stackInSlot.getCount();
            }
        }
        return seedItemsCount;
    }

    public int countHoesInInventory(){
        int count = 0;
        for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = parentProgram.worker.combinedInventory.getStackInSlot(i);
            if (stackInSlot.getItem() instanceof HoeItem) {
                count += stackInSlot.getCount();
            }
        }
        return count;
    }

    public int countFoodItems(){
        int count = 0;
        for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
            ItemStack stackInSlot = parentProgram.worker.combinedInventory.getStackInSlot(i);
            if (stackInSlot.has(DataComponents.FOOD)) {
                count += stackInSlot.getCount();
            }
        }
        return count;
    }

        public boolean hasAnyItemExceptSeedAndHoeAndFood(){
            for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
                ItemStack stackInSlot = parentProgram.worker.combinedInventory.getStackInSlot(i);
                if(stackInSlot.has(DataComponents.FOOD))continue;
                if(EntityCropFarm.isItemValidSeed(stackInSlot)) continue;
                if(stackInSlot.getItem() instanceof HoeItem)continue;
                return true;
            }
            return false;
        }

        public ItemStack getNextItemStackToUnload(EntityCropFarm farm){
            for (int j = 0; j < parentProgram.worker.combinedInventory.getSlots(); j++) {
                ItemStack canExtract = parentProgram.worker.combinedInventory.extractItem(j, 1, true);
                if (!canExtract.isEmpty()) {
                    if (canExtract.has(DataComponents.FOOD)) {
                        if (countFoodItems() > keepFoodNum) {
                        } else continue;
                    }
                    if (EntityCropFarm.isItemValidSeed(canExtract)) {
                        if (countSeedsInInventory() > keepSeedNum) {
                        } else continue;
                    }
                    if (canExtract.getItem() instanceof HoeItem) {
                        if (countHoesInInventory() > keepHoeNum) {
                        } else continue;
                    }

                    // try to insert in any inventory
                    // farm will make sure no wrong items go in the inventories
                    for (int i = 0; i < farm.specialResourcesInventory.getSlots(); i++) {
                        ItemStack notInserted = farm.specialResourcesInventory.insertItem(i, canExtract, true);
                        if (notInserted.isEmpty()) {
                            return canExtract;
                        }
                    }
                    for (int i = 0; i < farm.inputsInventory.getSlots(); i++) {
                        ItemStack notInserted = farm.inputsInventory.insertItem(i, canExtract, true);
                        if (notInserted.isEmpty()) {
                            return canExtract;
                        }
                    }
                    for (int i = 0; i < farm.mainInventory.getSlots(); i++) {
                        ItemStack notInserted = farm.mainInventory.insertItem(i, canExtract, true);
                        if (notInserted.isEmpty()) {
                            // it should unload only one item but return the full stack because this is what worker will take in hand
                            return parentProgram.worker.combinedInventory.getStackInSlot(j);
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        }

    public boolean depositOneItemToFarm(EntityCropFarm farm, boolean simulate) {
        for (int j = 0; j < parentProgram.worker.combinedInventory.getSlots(); j++) {
            ItemStack canExtract = parentProgram.worker.combinedInventory.extractItem(j, 1, true);
            if (!canExtract.isEmpty()) {
                if (canExtract.has(DataComponents.FOOD)) {
                    if (countFoodItems() > keepFoodNum) {
                    } else continue;
                }
                if (EntityCropFarm.isItemValidSeed(canExtract)) {
                    if (countSeedsInInventory() > keepSeedNum) {
                    } else continue;
                }
                if (canExtract.getItem() instanceof HoeItem) {
                    if (countHoesInInventory() > keepHoeNum) {
                    } else continue;
                }

                // try to insert in any inventory
                // farm will make sure no wrong items go in the inventories
                for (int i = 0; i < farm.specialResourcesInventory.getSlots(); i++) {
                    ItemStack notInserted = farm.specialResourcesInventory.insertItem(i, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            ItemStack extracted = parentProgram.worker.combinedInventory.extractItem(j, 1, false);
                            farm.specialResourcesInventory.insertItem(i, extracted, false);
                        }
                        return true;
                    }
                }
                for (int i = 0; i < farm.inputsInventory.getSlots(); i++) {
                    ItemStack notInserted = farm.inputsInventory.insertItem(i, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            ItemStack extracted = parentProgram.worker.combinedInventory.extractItem(j, 1, false);
                            farm.inputsInventory.insertItem(i, extracted, false);
                        }
                        return true;
                    }
                }
                for (int i = 0; i < farm.mainInventory.getSlots(); i++) {
                    ItemStack notInserted = farm.mainInventory.insertItem(i, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            ItemStack extracted = parentProgram.worker.combinedInventory.extractItem(j, 1, false);
                            farm.mainInventory.insertItem(i, extracted, false);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean recalculateHasWork(EntityCropFarm farm) {
        nextStackToUnload = getNextItemStackToUnload(parentProgram.currentFarm);
        return !nextStackToUnload.isEmpty();
    }

    public ExitCode run() {
        long gameTime = parentProgram.worker.level().getGameTime();
        if (gameTime > lastScan + scanInterval) {
            lastScan = gameTime;
            recalculateHasWork(parentProgram.currentFarm);
        }

        if (nextStackToUnload.isEmpty()) {
            return ExitCode.EXIT_SUCCESS;
        }

        if(!ItemStack.isSameItemSameComponents(parentProgram.worker.getMainHandItem(), nextStackToUnload) &&
                !ItemStack.isSameItemSameComponents(parentProgram.worker.getOffhandItem(), nextStackToUnload)) {
            ProgramUtils.moveItemStackToAnyHand(nextStackToUnload, parentProgram.worker);
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

        if (workDelay > 20) {
            workDelay = 0;
            if(ItemStack.isSameItemSameComponents(parentProgram.worker.getMainHandItem(), nextStackToUnload)){
                parentProgram.worker.swing(InteractionHand.MAIN_HAND);
            }
            if(ItemStack.isSameItemSameComponents(parentProgram.worker.getOffhandItem(), nextStackToUnload)){
                parentProgram.worker.swing(InteractionHand.OFF_HAND);
            }
            depositOneItemToFarm(parentProgram.currentFarm, false);
            recalculateHasWork(parentProgram.currentFarm);
        }
        workDelay++;

        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
