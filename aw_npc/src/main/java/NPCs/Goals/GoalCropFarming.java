package NPCs.Goals;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.*;

public class GoalCropFarming extends Goal {
    public HashMap<EntityCropFarm, Integer> invalidCropFarmsTimer = new HashMap<>();
    public int retryInvalidFarmTicks = 20 * 60;

    public WorkerNPC worker;
    public EntityCropFarm currentFarm;
    public boolean stillValid = false;
    Path currentPath;
    HashSet<BlockPos> unreachableBlocks = new HashSet<>();

    public GoalCropFarming(WorkerNPC worker) {
        super();
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        for (EntityCropFarm i : EntityCropFarm.knownCropFarms) {
            //System.out.println(i.getBlockPos());
            if (invalidCropFarmsTimer.containsKey(i)) {
                invalidCropFarmsTimer.put(i, invalidCropFarmsTimer.get(i) + 1);
                if (invalidCropFarmsTimer.get(i) > retryInvalidFarmTicks)
                    invalidCropFarmsTimer.remove(i);
                continue;
            }
            if (i.getBlockPos().getCenter().distanceTo(this.worker.getPosition(0)) < 512) {
                if (!i.positionsToHarvest.isEmpty()) {
                    currentFarm = i;
                    return true;
                }
                if (!i.positionsToPlant.isEmpty()) {
                    currentFarm = i;
                    return true;
                }
                if (!i.positionsToBoneMeal.isEmpty()) {
                    currentFarm = i;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        stillValid = true;
    }

    @Override
    public boolean canContinueToUse() {
        return stillValid && !currentFarm.isRemoved();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public boolean moveToPosition(BlockPos p, int precision) {
        if (p == null) return false;
        if (currentPath == null || !currentPath.getTarget().equals(p)) {
            System.out.println("recalculate path");
            currentPath = worker.getNavigation().createPath(p, precision);
            if (currentPath == null || currentPath.getTarget().getCenter().distanceTo(p.getCenter()) > precision)
                return false;
        }
        worker.getNavigation().moveTo(currentPath, 1);
        return true;
    }

    public double distanceTo(BlockPos target) {
        return worker.getPosition(0).distanceTo(target.getCenter());
    }

    void fail(){
        stillValid = false;
        invalidCropFarmsTimer.put(currentFarm, 0);
        worker.getNavigation().stop();
    }

    boolean programHarvest(){
        if (!currentFarm.positionsToHarvest.isEmpty()) {
            for (BlockPos currentHarvestTarget : currentFarm.positionsToHarvest) {
                if (unreachableBlocks.contains(currentHarvestTarget)) {
                    continue;
                }
                if (distanceTo(currentHarvestTarget) > 3) {
                    if (!moveToPosition(currentHarvestTarget, 2)) {
                        unreachableBlocks.add(currentHarvestTarget);
                    }
                } else {
                    currentFarm.positionsToHarvest.remove(currentHarvestTarget);
                    currentFarm.harvestPosition(currentHarvestTarget);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void tick() {

        if (distanceTo(currentFarm.getBlockPos()) > 64) {
            if (!moveToPosition(currentFarm.getBlockPos(), 64)) {
                fail();
            }
            return;
        }


      if(!programHarvest())
          fail();
    }
}
