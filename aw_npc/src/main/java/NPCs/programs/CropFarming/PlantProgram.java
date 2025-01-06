package NPCs.programs.CropFarming;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import NPCs.programs.CropFarmingProgram;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlantProgram {
    CropFarmingProgram parentProgram;
    BlockPos currentPlantTarget = null;
    int workDelay = 0;

    public PlantProgram(CropFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public static ItemStack getStackToPlantAtPosition(EntityCropFarm farm, WorkerNPC worker, BlockPos p) {
        if (!farm.canPlant(p)) return ItemStack.EMPTY;

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

    public static boolean canPlantAny(EntityCropFarm farm, WorkerNPC worker) {
        for (BlockPos p : farm.positionsToPlant) {
            if (getStackToPlantAtPosition(farm, worker, p) != ItemStack.EMPTY) {
                return true;
            }
        }
        return false;
    }


    public ExitCode run() {
        if (parentProgram.currentFarm.positionsToPlant.contains(currentPlantTarget)) {
            ItemStack stackToPlant = getStackToPlantAtPosition(parentProgram.currentFarm, parentProgram.worker, currentPlantTarget);
            if (!stackToPlant.isEmpty()) {
                ExitCode pathFindExit = parentProgram.worker.moveToPosition(currentPlantTarget, 3);

                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, currentPlantTarget.getCenter());
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, currentPlantTarget.getCenter());

                parentProgram.worker.setItemInHand(InteractionHand.OFF_HAND, stackToPlant);

                if (pathFindExit.isFailed()) {
                    currentPlantTarget = null;
                    return ExitCode.SUCCESS_STILL_RUNNING;
                } else if (pathFindExit.isCompleted()) {
                    workDelay++;
                    if (workDelay > 20) {
                        workDelay = 0;
                        // time to plant
                        parentProgram.worker.level().setBlock(currentPlantTarget, ((BlockItem) (stackToPlant.getItem())).getBlock().defaultBlockState(), 3);
                        stackToPlant.shrink(1);
                        parentProgram.worker.swing(InteractionHand.OFF_HAND);
                        parentProgram.currentFarm.positionsToPlant.remove(currentPlantTarget);
                    }
                } else {
                    workDelay = 0;
                }

                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }
        currentPlantTarget = null;
        for (BlockPos i : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(parentProgram.currentFarm.positionsToPlant, parentProgram.worker)) {
            if (getStackToPlantAtPosition(parentProgram.currentFarm, parentProgram.worker, i) != ItemStack.EMPTY) {
                currentPlantTarget = i;
                if (!parentProgram.worker.moveToPosition(currentPlantTarget, 3).isFailed()) {
                    return ExitCode.SUCCESS_STILL_RUNNING;
                }
            }
        }

        return ExitCode.EXIT_SUCCESS;
    }
}
