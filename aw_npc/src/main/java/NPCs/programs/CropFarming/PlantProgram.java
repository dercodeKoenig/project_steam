package NPCs.programs.CropFarming;

import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import WorkSites.CropFarm.EntityCropFarm;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Objects;

public class PlantProgram {

    public static HashMap<BlockPos, Long> positionsInUseWithLastUseTime = new HashMap<>();

    MainFarmingProgram parentProgram;
    BlockPos currentPlantTarget = null;
    int workDelay = 0;
    int scanInterval = 20 * 20;
    long lastScan = 0;
    boolean hasWork = false;
    int requiredDistance = 2;

    public PlantProgram(MainFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public static boolean isAllowedToPlantItemByWorkOrder(ItemStack seed, EntityCropFarm farm) {
        // check if seed is whitelisted in work order for current farm
        return true;
    }

    public ItemStack getStackToPlantAtPosition(EntityCropFarm farm, BlockPos p) {
        if (!farm.canPlant(p)) return ItemStack.EMPTY;

        for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); ++i) {
            ItemStack s = parentProgram.worker.combinedInventory.getStackInSlot(i);
            if (!s.isEmpty() && TakeSeedsProgram.isValidSeedItem(farm,s, parentProgram.worker)) {
                Item var5 = s.getItem();
                if (var5 instanceof BlockItem bi) {
                    if (bi.getBlock().defaultBlockState().canSurvive(parentProgram.worker.level(), p)) {
                        return s;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean recalculateHasWork(EntityCropFarm farm) {
        hasWork = false;

        if (TakeSeedsProgram.workerHasAnyValidSeedItem(farm, parentProgram.worker)) {
            for (BlockPos p : farm.positionsToPlant) {

                if(parentProgram.worker.slowMobNavigation.isPositionCachedAsInvalid(p))
                    continue;

                // skip this position if another worker already locks it
                if(!Objects.equals(p,currentPlantTarget))
                    if(positionsInUseWithLastUseTime.containsKey(p) && positionsInUseWithLastUseTime.get(p)+5>parentProgram.worker.level().getGameTime())
                        continue;
                if (getStackToPlantAtPosition(farm, p) != ItemStack.EMPTY) {
                    hasWork = true;
                }
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

        if (parentProgram.currentFarm.positionsToPlant.contains(currentPlantTarget)) {
            ItemStack stackToPlant = getStackToPlantAtPosition(parentProgram.currentFarm, currentPlantTarget);
            if (!stackToPlant.isEmpty()) {

                positionsInUseWithLastUseTime.put(currentPlantTarget,parentProgram.worker.level().getGameTime());


                ExitCode pathFindExit = parentProgram.worker.slowMobNavigation.moveToPosition(
                        currentPlantTarget,
                        requiredDistance,
                        parentProgram.worker.slowNavigationMaxDistance,
                        parentProgram.worker.slowNavigationMaxNodes,
                        parentProgram.worker.slowNavigationStepPerTick
                );

                if (!ItemStack.isSameItemSameComponents(parentProgram.worker.getMainHandItem(), stackToPlant) &&
                        !ItemStack.isSameItemSameComponents(parentProgram.worker.getOffhandItem(), stackToPlant)) {
                    ProgramUtils.moveItemStackToAnyHand(stackToPlant, parentProgram.worker);
                }

                if (pathFindExit.isFailed()) {
                    currentPlantTarget = null;
                    return ExitCode.SUCCESS_STILL_RUNNING;
                } else if (pathFindExit.isCompleted()) {
                    parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, currentPlantTarget.getCenter());
                    parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, currentPlantTarget.getCenter());

                    workDelay++;
                    if (workDelay > 20) {
                        workDelay = 0;
                        // time to plant
                        parentProgram.worker.level().setBlock(currentPlantTarget, ((BlockItem) (stackToPlant.getItem())).getBlock().defaultBlockState(), 3);
                        stackToPlant.shrink(1);
                        if (parentProgram.worker.getMainHandItem().getItem().equals(stackToPlant.getItem()))
                            parentProgram.worker.swing(InteractionHand.MAIN_HAND);
                        else if (parentProgram.worker.getOffhandItem().getItem().equals(stackToPlant.getItem()))
                            parentProgram.worker.swing(InteractionHand.OFF_HAND);
                        parentProgram.currentFarm.positionsToPlant.remove(currentPlantTarget);

                        parentProgram. recalculateHasWorkForAll();
                    }
                } else {
                    workDelay = 0;
                }

                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }
        currentPlantTarget = null;
        for (BlockPos i : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(parentProgram.currentFarm.positionsToPlant, parentProgram.worker)) {
            if(positionsInUseWithLastUseTime.containsKey(i) && positionsInUseWithLastUseTime.get(i) + 5 > parentProgram.worker.level().getGameTime())
                continue;

            if (getStackToPlantAtPosition(parentProgram.currentFarm, i) != ItemStack.EMPTY) {
                if (!parentProgram.worker.slowMobNavigation.isPositionCachedAsInvalid(i)) {
                    currentPlantTarget = i;
                    positionsInUseWithLastUseTime.put(currentPlantTarget,parentProgram.worker.level().getGameTime());
                    return ExitCode.SUCCESS_STILL_RUNNING;
                }else{

                }
            }
        }

        return ExitCode.EXIT_SUCCESS;
    }
}
