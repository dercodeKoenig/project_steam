package NPCs.programs.CropFarming;

import Farms.CropFarm.EntityCropFarm;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TillProgram {

    CropFarmingProgram parentProgram;
    BlockPos currentTillTarget = null;
    int workDelay = 0;

    public TillProgram(CropFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public boolean canTillPosition(EntityCropFarm farm,  BlockPos p) {
        // first check if the position is valid to grow anything (not blocked by other blocks or stem plants)
        // this is required because getStackToPlantAtPosition will return EMPTY if the position is completely invalid for planting
        if (!farm.canPlant(p)) return false;

        // check if he can not plant anything there
        if (parentProgram.plantProgram.getStackToPlantAtPosition(farm, p) != ItemStack.EMPTY)
            return false;

        // if the position is valid for planting but he can currently not plant there, this position is valid for till

        BlockState s = parentProgram.worker.level().getBlockState(p.below());
        Block b = s.getBlock();
        if (b.equals(Blocks.DIRT) || b.equals(Blocks.DIRT_PATH) || b.equals(Blocks.GRASS_BLOCK)) {
            return true;
        }
        return false;
    }

    public boolean canTillAny(EntityCropFarm target) {
        if (!parentProgram.takeHoeProgram.hasHoe()) return false;
        for (BlockPos p : target.positionsToPlant) {
            if (canTillPosition(target, p))
                return true;
        }
        return false;
    }


    public ExitCode run() {
        if (!parentProgram.takeHoeProgram.takeHoeToMainHand()) {
            return ExitCode.EXIT_SUCCESS;
        }

        if (parentProgram.currentFarm.positionsToPlant.contains(currentTillTarget)) {
            if (canTillPosition(parentProgram.currentFarm, currentTillTarget)) {
                ExitCode pathFindExit = parentProgram.worker.moveToPosition(currentTillTarget, 3);

                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, currentTillTarget.getCenter());
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, currentTillTarget.getCenter());

                if (pathFindExit.isFailed()) {
                    currentTillTarget = null;
                    return ExitCode.SUCCESS_STILL_RUNNING;
                } else if (pathFindExit.isCompleted()) {
                    workDelay++;
                    if (workDelay > 20) {
                        workDelay = 0;
                        // time to Till
                        parentProgram.worker.level().setBlock(currentTillTarget.below(), Blocks.FARMLAND.defaultBlockState(), 3);
                        parentProgram.worker.swing(InteractionHand.MAIN_HAND);
                        parentProgram.currentFarm.positionsToPlant.remove(currentTillTarget);
                        ProgramUtils.damageMainHandItem(parentProgram.worker);
                    }
                } else {
                    workDelay = 0;
                }

                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }
        currentTillTarget = null;
        for (BlockPos i : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(parentProgram.currentFarm.positionsToPlant, parentProgram.worker)) {
            if (canTillPosition(parentProgram.currentFarm, i)) {
                if (!parentProgram.worker.moveToPosition(i, 3).isFailed()) {
                    currentTillTarget = i;
                    return ExitCode.SUCCESS_STILL_RUNNING;
                } else {
                }
            }
        }

        return ExitCode.EXIT_SUCCESS;
    }
}
