package NPCs.programs;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.HashMap;

public class CropFarmingProgram extends Goal {

    public HashMap<BlockPos, Long> workCheckedTracker = new HashMap<>();

    public WorkerNPC worker;
    public BlockPos currentFarmPos;
    public double cachedDistanceToFarm;
    public int timeoutForWorkCheck = 20 * 10;
    public int timeoutForSeedScan = 20 * 10;
    BlockPos currentPlantTarget = null;
    int workDelay = 0;
    public boolean canUse = true;

    public CropFarmingProgram(WorkerNPC worker) {
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public static boolean hasWorkAtCropFarm(BlockPos p, WorkerNPC worker) {

        BlockEntity e = worker.level().getBlockEntity(p);
        if (!(e instanceof EntityCropFarm farm)) return false;

        // check if the farm has a hoe to take if worker does not already have one
        if (canPickupHoeFromFarm(farm, worker)) return true;

        // check if we do not have seeds but the farm has seeds so we can take them
        if (!hasAnySeedItem(farm, worker) && canPickupSeedsFromFarm(farm, worker)) return true;

        return false;
    }


    @Override
    public boolean canUse() {

        //clean up entries that no longer exist
        for (BlockPos i : workCheckedTracker.keySet()) {
            if (!EntityCropFarm.knownCropFarms.contains(i)) {
                workCheckedTracker.remove(i);
                break;
            }
        }

        long gameTime = worker.level().getGameTime();
        for (BlockPos p : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(EntityCropFarm.knownCropFarms, worker)) {
            if (workCheckedTracker.containsKey(p)) {
                if (workCheckedTracker.get(p) + timeoutForWorkCheck > gameTime)
                    continue;
            }
            workCheckedTracker.put(p, gameTime);
            if (hasWorkAtCropFarm(p, worker)) {
                currentFarmPos = p;
                return true;
            }

        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse;
    }

    @Override
    public void start() {
        canUse = true;
        workDelay = 0;
    }

    @Override
    public void tick() {
        long t0 = System.nanoTime();
        ExitCode e = run();
        long t1 = System.nanoTime();
        //System.out.println((double)(t1-t0) / 1000 / 1000);
        if (e.isEnd()) canUse = false;
    }

    public ExitCode run() {

        BlockEntity e = worker.level().getBlockEntity(currentFarmPos);
        if (!(e instanceof EntityCropFarm farm)) return ExitCode.EXIT_FAIL;

        cachedDistanceToFarm = worker.getPosition(0).distanceTo(currentFarmPos.getCenter());

        // first make sure you are anywhere near the farm
        ExitCode moveNearFarmExit = moveNearFarm(128, farm);
        if (moveNearFarmExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (moveNearFarmExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;


        // make sure you have a valid hoe item or fail
        ExitCode takeHoeExit = takeHoe(farm);
        if (takeHoeExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (takeHoeExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;


        // try to restock seeds if required and possible
        ExitCode restockSeedExit = restockSeedItemsIfPossibleAndRequired(farm);
        if (restockSeedExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (restockSeedExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to plant
        ExitCode tryPlantExit = tryPlant(farm);
        if (tryPlantExit.isFailed()) return ExitCode.EXIT_FAIL; // this should never fail
        if (tryPlantExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;


        return ExitCode.EXIT_SUCCESS;
    }


    public ExitCode moveNearFarm(int precision, EntityCropFarm currentFarm) {
        if (cachedDistanceToFarm > precision + 1) {
            return worker.moveToPosition(currentFarm.getBlockPos(), precision);
        }
        return ExitCode.EXIT_SUCCESS;
    }

    public static ItemStack getStackToPlantAtPosition(EntityCropFarm farm, WorkerNPC worker, BlockPos p) {
        if(!farm.canPlant(p)) return ItemStack.EMPTY;

        for (int z = -1; z <= 1; ++z) {
            if (worker.level().getBlockState(p.offset(0, 0, z)).getBlock() instanceof StemBlock || worker.level().getBlockState(p.offset(0, 0, z)).getBlock() instanceof AttachedStemBlock) {
                return ItemStack.EMPTY;
            }
        }

        for (int x = -1; x <= 1; ++x) {
            if (worker.level().getBlockState(p.offset(x, 0, 0)).getBlock() instanceof StemBlock || worker.level().getBlockState(p.offset(x, 0, 0)).getBlock() instanceof AttachedStemBlock) {
                return ItemStack.EMPTY;
            }
        }

        for (int i = 0; i < worker.inventory.getSlots(); ++i) {
            ItemStack s = worker.inventory.getStackInSlot(i);
            if (!s.isEmpty() && farm.isItemValidSeed(s)) {
                Item var5 = s.getItem();
                if (var5 instanceof BlockItem) {
                    BlockItem bi = (BlockItem) var5;
                    if (bi.getBlock().defaultBlockState().canSurvive(worker.level(), p)) {
                        return s;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean canPlant(EntityCropFarm farm, WorkerNPC worker) {
        if (farm.positionsToPlant.isEmpty()) return false;
        if (!hasAnySeedItem(farm, worker)) return false;

        for (BlockPos p : farm.positionsToPlant) {
            if (getStackToPlantAtPosition(farm, worker, p) != ItemStack.EMPTY) {
                return true;
            }
        }

        return true;
    }


    public ExitCode tryPlant(EntityCropFarm currentFarm) {
        if (currentFarm.positionsToPlant.contains(currentPlantTarget)) {
            ItemStack stackToPlant = getStackToPlantAtPosition(currentFarm, worker, currentPlantTarget);
            if (!stackToPlant.isEmpty()) {
                ExitCode pathFindExit = worker.moveToPosition(currentPlantTarget, 3);

                worker.lookAt(EntityAnchorArgument.Anchor.EYES,currentPlantTarget.getCenter());
                worker.lookAt(EntityAnchorArgument.Anchor.FEET,currentPlantTarget.getCenter());

                worker.setItemInHand(InteractionHand.OFF_HAND, stackToPlant);

                if (pathFindExit.isFailed()) {
                    currentPlantTarget = null;
                    return ExitCode.SUCCESS_STILL_RUNNING;
                } else if (pathFindExit.isCompleted()) {
                    workDelay++;
                    if (workDelay > 20) {
                        workDelay = 0;
                        // time to plant
                        worker.level().setBlock(currentPlantTarget, ((BlockItem) (stackToPlant.getItem())).getBlock().defaultBlockState(), 3);
                        stackToPlant.shrink(1);
                        worker.swing(InteractionHand.OFF_HAND);
                        currentFarm.positionsToPlant.remove(currentPlantTarget);
                    }
                }

                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }
        currentPlantTarget = null;
        for (BlockPos i : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(currentFarm.positionsToPlant, worker)) {
            if (getStackToPlantAtPosition(currentFarm, worker, i) != ItemStack.EMPTY) {
                currentPlantTarget = i;
                if (!worker.moveToPosition(currentPlantTarget, 3).isFailed()) {
                    return ExitCode.SUCCESS_STILL_RUNNING;
                }
            }
        }

        return ExitCode.EXIT_SUCCESS;
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

    public static boolean canPickupSeedsFromFarm(EntityCropFarm farm, WorkerNPC worker) {
        // check if the farm has any seed and if worker has space for seed
        for (int i = 0; i < farm.inputsInventory.getSlots(); i++) {
            ItemStack stackInSlot = farm.inputsInventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                for (int j = 0; j < worker.inventory.getSlots(); j++) {
                    ItemStack notInserted = worker.inventory.insertItem(j, stackInSlot, true);
                    if (notInserted.getCount() < stackInSlot.getCount()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // TODO maybe not check every tick?
    public ExitCode restockSeedItemsIfPossibleAndRequired(EntityCropFarm currentFarm) {

        // when close to farm we can assume he is currently taking seeds, so do not interrupt
        if(cachedDistanceToFarm > 3)
            if (hasAnySeedItem(currentFarm, worker))
                return ExitCode.EXIT_SUCCESS;

        if (!canPickupSeedsFromFarm(currentFarm, worker)) {
            return ExitCode.EXIT_SUCCESS;
        }

        worker.lookAt(EntityAnchorArgument.Anchor.EYES,currentFarm.getBlockPos().getCenter());
        worker.lookAt(EntityAnchorArgument.Anchor.FEET,currentFarm.getBlockPos().getCenter());

        // at this point we should navigate to the farm to take seeds
        ExitCode pathFindExit = moveNearFarm(3, currentFarm);
        if (pathFindExit.isFailed())
            return ExitCode.EXIT_FAIL;
        else if (pathFindExit.isStillRunning())
            return ExitCode.SUCCESS_STILL_RUNNING;

        if (workDelay > 20) {
            workDelay = 0;


            // try to take 1 seed item
            boolean tookSeed = false;
            A:
            {
                for (int j = 0; j < currentFarm.inputsInventory.getSlots(); j++) {
                    // simulate extract 1 item
                    ItemStack canExtract = currentFarm.inputsInventory.extractItem(j, 1, true);
                    if (!canExtract.isEmpty()) {
                        // try to insert the item in any slot
                        for (int i = 0; i < worker.inventory.getSlots(); i++) {
                            // simulate insert
                            ItemStack canNotInsert = worker.inventory.insertItem(i, canExtract, true);
                            if (canNotInsert.isEmpty()) {
                                // if it can be inserted, do the actual movement and break
                                ItemStack extracted = currentFarm.inputsInventory.extractItem(j, 1, false);
                                worker.inventory.insertItem(i, extracted, false);
                                tookSeed = true;
                                worker.swing(InteractionHand.MAIN_HAND);
                                break A;
                            }
                        }
                    }
                }
            }
            if (!tookSeed) {
                return ExitCode.EXIT_SUCCESS;
            }

            int seedItemsCount = 0;
            for (int i = 0; i < worker.inventory.getSlots(); i++) {
                ItemStack stackInSlot = worker.inventory.getStackInSlot(i);
                if (currentFarm.isItemValidSeed(stackInSlot)) {
                    seedItemsCount += stackInSlot.getCount();
                }
            }
            if (seedItemsCount > 32) {
                return ExitCode.EXIT_SUCCESS;
            }
        }
        workDelay++;

        return ExitCode.SUCCESS_STILL_RUNNING;
    }

    public static boolean canPickupHoeFromFarm(EntityCropFarm farm, WorkerNPC worker) {
        if (!worker.getMainHandItem().isEmpty()) return false;
        for (int j = 0; j < farm.mainInventory.getSlots(); j++) {
            ItemStack stackInSlot = farm.mainInventory.getStackInSlot(j);
            if (stackInSlot.getItem() instanceof HoeItem) {
                return true;
            }
        }
        return false;
    }

    public ExitCode takeHoe(EntityCropFarm currentFarm) {
        if (!canPickupHoeFromFarm(currentFarm, worker)) return ExitCode.EXIT_SUCCESS;

        worker.lookAt(EntityAnchorArgument.Anchor.EYES,currentFarm.getBlockPos().getCenter());
        worker.lookAt(EntityAnchorArgument.Anchor.FEET,currentFarm.getBlockPos().getCenter());

        ExitCode pathFindExit = moveNearFarm(3, currentFarm);
        if (pathFindExit.isFailed())
            return ExitCode.EXIT_FAIL;
        else if (pathFindExit.isStillRunning())
            return ExitCode.SUCCESS_STILL_RUNNING;

        if (workDelay > 20) {
            workDelay = 0;
            for (int j = 0; j < currentFarm.mainInventory.getSlots(); j++) {
                ItemStack stackInSlot = currentFarm.mainInventory.getStackInSlot(j);
                if (stackInSlot.getItem() instanceof HoeItem) {
                    worker.setItemInHand(InteractionHand.MAIN_HAND, stackInSlot.copy());
                    currentFarm.mainInventory.setStackInSlot(j, ItemStack.EMPTY);
                    currentFarm.setChanged();
                    worker.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
        workDelay++;

        return ExitCode.SUCCESS_STILL_RUNNING;
    }

}


/*
    int waitBeforeHarvest = 0;
    boolean programHarvest() {
        if (!currentFarm.positionsToHarvest.isEmpty()) {
            for (BlockPos currentHarvestTarget : currentFarm.positionsToHarvest) {
                if (unreachableBlocks.contains(currentHarvestTarget)) {
                    continue;
                }
                if (distanceTo(currentHarvestTarget) > 3) {
                    if (!moveToPosition(currentHarvestTarget, 2)) {
                        unreachableBlocks.add(currentHarvestTarget);
                    }
                    waitBeforeHarvest = 0;
                } else {
                    waitBeforeHarvest++;
                    if(waitBeforeHarvest > 20) {
                        waitBeforeHarvest = 0;
                        currentFarm.positionsToHarvest.remove(currentHarvestTarget);
                        currentFarm.harvestPosition(currentHarvestTarget);
                        worker.swing(InteractionHand.MAIN_HAND);
                    }
                }
                return true;
            }
        }
        return false;
    }
 */