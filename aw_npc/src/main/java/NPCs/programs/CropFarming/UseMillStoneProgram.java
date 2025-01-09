package NPCs.programs.CropFarming;

import AOSWorkshopExpansion.MillStone.EntityMillStone;
import AOSWorkshopExpansion.MillStone.MillStoneConfig;
import ARLib.utils.ItemUtils;
import NPCs.WorkerNPC;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import WorkSites.CropFarm.EntityCropFarm;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashMap;

public class UseMillStoneProgram {

    public static HashMap<BlockPos, Long> millstonesInUseWithLastUseTime = new HashMap<>();

    MainFarmingProgram parentProgram;
    EntityMillStone currentMillstone;
    int workDelay = 0;
    int scanInterval = 20 * 20;
    long lastScan = 0;
    boolean canTakeOutputs = false;
    ItemStack canPutInputsFromInventory = ItemStack.EMPTY;
    ItemStack canPutInputsFromFarm = ItemStack.EMPTY;
    int stackSizeToTakeFromFarm = 16;
    int requiredDistance = 2;
    int requiredDistanceToMillStone = 3;

    public UseMillStoneProgram(MainFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }


    public static boolean isItemValidRecipeOutput(ItemStack item) {
        for (MillStoneConfig.MillStoneRecipe r : MillStoneConfig.INSTANCE.recipes) {
            if (ItemUtils.matches(r.outputItem.id, item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemValidRecipeInput(ItemStack item) {
        for (MillStoneConfig.MillStoneRecipe r : MillStoneConfig.INSTANCE.recipes) {
            if (ItemUtils.matches(r.inputItem.id, item)) {
                return true;
            }
        }
        return false;
    }


    public static ItemStack takeOneValidMillStoneInputFromFarm(EntityCropFarm farm, WorkerNPC worker, boolean simulate) {
        for (int i = 0; i < farm.mainInventory.getSlots(); i++) {
            ItemStack canExtract = farm.mainInventory.extractItem(i, 1, true);
            if (!canExtract.isEmpty() && isItemValidRecipeInput(canExtract)) {
                for (int j = 0; j < worker.combinedInventory.getSlots(); j++) {
                    ItemStack notInserted = worker.combinedInventory.insertItem(j, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            ItemStack extracted = farm.mainInventory.extractItem(i, 1, false);
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


    public static ItemStack unloadOneItemIntoMillstone(EntityMillStone millstone, WorkerNPC worker, boolean simulate) {
        IItemHandler inventory = worker.combinedInventory;
        for (int j = 0; j < inventory.getSlots(); j++) {
            ItemStack canExtract = inventory.extractItem(j, 1, true);
            ItemStack stackCopyToReturn = inventory.getStackInSlot(j).copy();
            if (!canExtract.isEmpty() && isItemValidRecipeInput(canExtract)) {

                for (int i = 0; i < millstone.inventory.getSlots(); i++) {
                    ItemStack notInserted = millstone.inventory.insertItem(i, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            if (worker.getMainHandItem().getItem().equals(canExtract. getItem()))
                                worker.swing(InteractionHand.MAIN_HAND);
                            else if (worker.getOffhandItem().getItem().equals(canExtract.getItem()))
                                worker.swing(InteractionHand.OFF_HAND);

                            ItemStack extracted = inventory.extractItem(j, 1, false);
                            millstone.inventory.insertItem(i, extracted, false);
                        }
                        return stackCopyToReturn;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack canUnloadOneItemIntoMillstone(IItemHandler inventory, EntityMillStone millstone, int requiredMinStockInInvventory) {
        for (int j = 0; j < inventory.getSlots(); j++) {
            ItemStack canExtract = inventory.extractItem(j, 1, true);
            ItemStack stackCopyToReturn = inventory.getStackInSlot(j).copy();
            if (!canExtract.isEmpty() && isItemValidRecipeInput(canExtract) && ProgramUtils.countItems(canExtract.getItem(), inventory) > requiredMinStockInInvventory) {

                for (int i = 0; i < millstone.inventory.getSlots(); i++) {
                    ItemStack notInserted = millstone.inventory.insertItem(i, canExtract, true);
                    if (notInserted.isEmpty()) {
                        return stackCopyToReturn;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean takeItemOutOfMillStone(EntityMillStone millStone, WorkerNPC worker, boolean simulate) {
        for (int i = 0; i < millStone.inventory.getSlots(); i++) {
            ItemStack canExtract = millStone.inventory.extractItem(i, 1, true);

            if (!canExtract.isEmpty() && isItemValidRecipeOutput(canExtract)) {
                for (int j = 0; j < worker.combinedInventory.getSlots(); j++) {
                    ItemStack notInserted = worker.combinedInventory.insertItem(j, canExtract, true);
                    if (notInserted.isEmpty()) {
                        if (!simulate) {
                            ItemStack extracted = millStone.inventory.extractItem(i, 1, false);
                            worker.combinedInventory.insertItem(j, extracted, false);
                            worker.swing(ProgramUtils.moveItemStackToAnyHand(worker.combinedInventory.getStackInSlot(j), worker));
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean recalculateHasWork(EntityCropFarm farm) {

        // i will place millstone first in the program list so that other programs do not interrupt
        // while he is walking to millstone.
        // this way it is likely that the main program will end before this one can start
        // but when the main program re-scans for work it will notice if the millstone program has work
        // and because it will run first it can not be interrupted

        if (parentProgram.harvestProgram.hasWork ||
                parentProgram.takeSeedsProgram.hasWork ||
                parentProgram.plantProgram.hasWork ||
                parentProgram.tillProgram.hasWork) {
            currentMillstone = null;
            return false;
        }

        long gametime = parentProgram.worker.level().getGameTime();
        // remove blocked millstone entries after some ticks in case worker died or is no longer using it
        for (BlockPos p : millstonesInUseWithLastUseTime.keySet()) {
            if (millstonesInUseWithLastUseTime.get(p) > gametime) {
                millstonesInUseWithLastUseTime.remove(p);
                break;
            }
        }

        if (currentMillstone != null) {
            canTakeOutputs = takeItemOutOfMillStone(currentMillstone, parentProgram.worker, true);

            // only take from farm if more than 64 are present
            // if we are close to the farm we can reduce the filter because we want the worker to take the entire batch and not just one item
            int d = 64;
            if(ProgramUtils.distanceManhattan(parentProgram.worker,farm.getBlockPos().getCenter()) <= requiredDistance+2)
                d -= stackSizeToTakeFromFarm;
            canPutInputsFromFarm = canUnloadOneItemIntoMillstone(farm.mainInventory, currentMillstone, d);

            canPutInputsFromInventory = unloadOneItemIntoMillstone(currentMillstone,parentProgram.worker, true);

            // if we can put inputs from farm check if the worker can take from farm ( in case inventory full)
            if (!canPutInputsFromFarm.isEmpty()) {
                if (takeOneValidMillStoneInputFromFarm(farm, parentProgram.worker, true).isEmpty()) {
                    canPutInputsFromFarm = ItemStack.EMPTY;
                }
            }

            boolean hasWork = !canPutInputsFromFarm.isEmpty() || canTakeOutputs || !canPutInputsFromInventory.isEmpty();
            if (!hasWork) currentMillstone = null;
            return hasWork;

        } else {
            for (BlockPos p : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(EntityMillStone.knownBlockEntities, parentProgram.worker)) {
                if (ProgramUtils.distanceManhattan(parentProgram.worker, p.getCenter()) > 64) {
                    break;
                }

                // check if the millstone is cached as unreachable
                if (parentProgram.worker.slowMobNavigation.isPositionCachedAsInvalid(p))
                    continue;

                // check if items can be taken out of the millstone
                BlockEntity be = parentProgram.worker.level().getBlockEntity(p);
                if (be instanceof EntityMillStone millStone) {
                    canTakeOutputs = takeItemOutOfMillStone(millStone, parentProgram.worker, true);

                    // only take from farm if more than 64 are present
                    // if we are close to the farm we can reduce the filter because we want the worker to take the entire batch and not just one item
                    int d = 64;
                    if(ProgramUtils.distanceManhattan(parentProgram.worker,farm.getBlockPos().getCenter()) <= requiredDistance+2)
                        d -= stackSizeToTakeFromFarm;
                    canPutInputsFromFarm = canUnloadOneItemIntoMillstone(farm.mainInventory, millStone, d);

                    canPutInputsFromInventory = unloadOneItemIntoMillstone(millStone,parentProgram.worker, true);

                    // if we can put inputs from farm check if the worker can take from farm ( in case inventory full)
                    if (!canPutInputsFromFarm.isEmpty()) {
                        if (takeOneValidMillStoneInputFromFarm(farm, parentProgram.worker, true).isEmpty()) {
                            canPutInputsFromFarm = ItemStack.EMPTY;
                        }
                    }

                    boolean hasWork = !canPutInputsFromFarm.isEmpty() || canTakeOutputs || !canPutInputsFromInventory.isEmpty();
                    if (hasWork) currentMillstone = millStone;
                    return hasWork;
                }
            }
        }
        currentMillstone = null;
        canPutInputsFromFarm = ItemStack.EMPTY;
        canPutInputsFromInventory = ItemStack.EMPTY;
        canTakeOutputs = false;
        return false;
    }

    public ExitCode run() {

        long gameTime = parentProgram.worker.level().getGameTime();
        if (gameTime > lastScan + scanInterval) {
            lastScan = gameTime;
            recalculateHasWork(parentProgram.currentFarm);
        }

        if (currentMillstone == null) return ExitCode.EXIT_SUCCESS;


        // first try to take a batch of items from the farm to carry to millstone
        // note that it can cause the worker to deposit one item and run back to farm to restock the batch
        // because of this, only consider this if we are more than x block away from millstone
        if (!canPutInputsFromFarm.isEmpty() && ProgramUtils.distanceManhattan(parentProgram.worker, currentMillstone.getBlockPos().getCenter()) > 10) {
            // count how many items of insertable type i have in inventory already do decide if i should take more
            // i may have more items to insert in inventory because both only return the first valid item but this is not a problem
            int itemsToInsertTotal =
                    ProgramUtils.countItems(canPutInputsFromFarm.getItem(), parentProgram.worker.combinedInventory)
                            + ProgramUtils.countItems(canPutInputsFromInventory.getItem(), parentProgram.worker.combinedInventory);

            if (itemsToInsertTotal < stackSizeToTakeFromFarm) {
                // can take more before going to millstone
                ExitCode moveNearFarm = parentProgram.moveNearFarm(requiredDistance);
                if (moveNearFarm.isFailed())
                    return ExitCode.EXIT_FAIL; // moving to farm should never fail and if it does it is bad and should cancel the entire farming program
                if (moveNearFarm.isStillRunning())
                    return ExitCode.SUCCESS_STILL_RUNNING;

                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, parentProgram.currentFarm.getBlockPos().getCenter());
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, parentProgram.currentFarm.getBlockPos().getCenter());

                if (workDelay > 10) {
                    workDelay = 0;
                    takeOneValidMillStoneInputFromFarm(parentProgram.currentFarm, parentProgram.worker, false);
                    recalculateHasWork(parentProgram.currentFarm);
                }
                workDelay++;

                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }


        // now carry the items to millstone
        if (!canPutInputsFromInventory.isEmpty()) {

            //take the item in hand, just for visuals
            if (!ItemStack.isSameItemSameComponents(parentProgram.worker.getMainHandItem(), canPutInputsFromInventory) &&
                    !ItemStack.isSameItemSameComponents(parentProgram.worker.getOffhandItem(), canPutInputsFromInventory)) {
                ProgramUtils.moveItemStackToAnyHand(canPutInputsFromInventory, parentProgram.worker);
            }

            ExitCode pathFindExit = parentProgram.worker.slowMobNavigation.moveToPosition(
                    currentMillstone.getBlockPos(),
                    requiredDistanceToMillStone,
                    parentProgram.worker.slowNavigationMaxDistance,
                    parentProgram.worker.slowNavigationMaxNodes,
                    parentProgram.worker.slowNavigationStepPerTick
            );


            if (pathFindExit.isFailed()) {
                currentMillstone = null;
                return ExitCode.EXIT_SUCCESS;
            } else if (pathFindExit.isCompleted()) {
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, currentMillstone.getBlockPos().getCenter());
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, currentMillstone.getBlockPos().getCenter());

                if (workDelay > 10) {
                    workDelay = 0;
                    unloadOneItemIntoMillstone(currentMillstone, parentProgram.worker, false);
                    recalculateHasWork(parentProgram.currentFarm);
                }
                workDelay++;
            }
            return ExitCode.SUCCESS_STILL_RUNNING;
        }


        // now check if outputs can be taken out of millstone
        if (canTakeOutputs) {
            ExitCode pathFindExit = parentProgram.worker.slowMobNavigation.moveToPosition(
                    currentMillstone.getBlockPos(),
                    requiredDistanceToMillStone,
                    parentProgram.worker.slowNavigationMaxDistance,
                    parentProgram.worker.slowNavigationMaxNodes,
                    parentProgram.worker.slowNavigationStepPerTick
            );


            if (pathFindExit.isFailed()) {
                currentMillstone = null;
                return ExitCode.EXIT_SUCCESS;
            } else if (pathFindExit.isCompleted()) {
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, currentMillstone.getBlockPos().getCenter());
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, currentMillstone.getBlockPos().getCenter());

                if (workDelay > 10) {
                    workDelay = 0;
                    takeItemOutOfMillStone(currentMillstone, parentProgram.worker, false);
                    recalculateHasWork(parentProgram.currentFarm);
                }
                workDelay++;
            }
            return ExitCode.SUCCESS_STILL_RUNNING;
        }

        return ExitCode.EXIT_SUCCESS;
    }
}
