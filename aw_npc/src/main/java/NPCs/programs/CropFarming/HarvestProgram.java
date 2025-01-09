package NPCs.programs.CropFarming;

import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import WorkSites.CropFarm.EntityCropFarm;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class HarvestProgram {
    MainFarmingProgram parentProgram;
    BlockPos currentHarvestTarget = null;
    int workDelay = 0;
    int scanInterval = 20 * 20;
    long lastScan = 0;
    boolean hasWork = false;
    int requiredFreeSlotsToHarvest = 3;
    int requiredDistance = 2;

    public HarvestProgram(MainFarmingProgram parentProgram) {
        this.parentProgram = parentProgram;
    }

    public boolean recalculateHasWork(EntityCropFarm target) {
        hasWork = true;
        if (!parentProgram.takeHoeProgram.hasHoe() && !parentProgram.takeHoeProgram.canPickupHoeFromFarm(target))
            hasWork = false;
        if (target.positionsToHarvest.isEmpty()) hasWork = false;

        if (hasWork) {
            int numEmptySlots = 0;
            for (int i = 0; i < parentProgram.worker.combinedInventory.getSlots(); i++) {
                if (parentProgram.worker.combinedInventory.getStackInSlot(i).isEmpty()) numEmptySlots++;
            }
            if (numEmptySlots < requiredFreeSlotsToHarvest) hasWork = false;
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

        ExitCode takeHoeExit = parentProgram.takeHoeProgram.run();
        if (takeHoeExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;
        if (takeHoeExit.isFailed()) return ExitCode.EXIT_SUCCESS; // nothing to do because no hoe available

        parentProgram.takeHoeProgram.takeHoeToMainHand();

        if (parentProgram.currentFarm.positionsToHarvest.contains(currentHarvestTarget)) {
            ExitCode pathFindExit = parentProgram.worker.slowMobNavigation.moveToPosition(
                    currentHarvestTarget,
                    requiredDistance,
                    parentProgram.worker.slowNavigationMaxDistance,
                    parentProgram.worker.slowNavigationMaxNodes,
                    parentProgram.worker.slowNavigationStepPerTick
            );


            if (pathFindExit.isFailed()) {
                currentHarvestTarget = null;
                return ExitCode.SUCCESS_STILL_RUNNING;
            } else if (pathFindExit.isCompleted()) {

                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.EYES, currentHarvestTarget.getCenter());
                parentProgram.worker.lookAt(EntityAnchorArgument.Anchor.FEET, currentHarvestTarget.getCenter());

                workDelay++;
                if (workDelay > 20) {
                    workDelay = 0;
                    // time to Harvest
                    // check again to make sure everything will work
                    // i will harvest even if it not fit all in inventory
                    // i require 3 free slots to harvest
                    if (parentProgram.currentFarm.canHarvestPosition(currentHarvestTarget)) {
                        BlockState s = parentProgram.worker.level().getBlockState(currentHarvestTarget);
                        LootParams.Builder b = (new LootParams.Builder((ServerLevel) parentProgram.worker.level())).withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_HOE)).withParameter(LootContextParams.ORIGIN, parentProgram.worker.getPosition(0));
                        List<ItemStack> drops = s.getDrops(b);
                        for (ItemStack i : drops) {
                            for (int j = 0; j < parentProgram.worker.combinedInventory.getSlots(); ++j) {
                                i = parentProgram.worker.combinedInventory.insertItem(j, i, false);
                            }
                        }
                        parentProgram.worker.level().destroyBlock(currentHarvestTarget, false);
                        parentProgram.worker.swing(InteractionHand.MAIN_HAND);
                        ProgramUtils.damageMainHandItem(parentProgram.worker);
                        parentProgram.unloadInventoryProgram.recalculateHasWork(parentProgram.currentFarm);
                    }
                    parentProgram.currentFarm.positionsToHarvest.remove(currentHarvestTarget);

                    recalculateHasWork(parentProgram.currentFarm);
                }
            } else {
                workDelay = 0;
            }

            return ExitCode.SUCCESS_STILL_RUNNING;
        }
        currentHarvestTarget = null;
        for (BlockPos i : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(parentProgram.currentFarm.positionsToHarvest, parentProgram.worker)) {
            if (!parentProgram.worker.slowMobNavigation.isPositionCachedAsInvalid(i)) {
                currentHarvestTarget = i;
                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }
        return ExitCode.EXIT_SUCCESS;
    }
}
