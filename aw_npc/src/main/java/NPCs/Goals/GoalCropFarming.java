package NPCs.Goals;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
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
    int failCounter = 0;

    public GoalCropFarming(WorkerNPC worker) {
        super();
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        boolean hasHoe = worker.getMainHandItem().getItem() instanceof HoeItem;

        for (EntityCropFarm i : EntityCropFarm.knownCropFarms) {
            //System.out.println(i.getBlockPos());
            if (invalidCropFarmsTimer.containsKey(i)) {
                invalidCropFarmsTimer.put(i, invalidCropFarmsTimer.get(i) + 1);
                if (invalidCropFarmsTimer.get(i) > retryInvalidFarmTicks)
                    invalidCropFarmsTimer.remove(i);
                continue;
            }
            if (!hasHoe) {
                boolean farmHasHoe = false;
                for (int j = 0; j < i.mainInventory.getSlots(); j++) {
                    ItemStack stackInSlot = i.mainInventory.getStackInSlot(j);
                    if (stackInSlot.getItem() instanceof HoeItem) {
                        farmHasHoe = true;
                        break;
                    }
                }
                if (!farmHasHoe)
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
        failCounter = 0;
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

    void fail() {
        failCounter++;
        if (failCounter > 100) {
            stillValid = false;
            invalidCropFarmsTimer.put(currentFarm, 0);
            worker.getNavigation().stop();
        }
    }

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

    boolean programGetHoe() {
        BlockPos target = currentFarm.getBlockPos();
        if (distanceTo(target) > 3) {
            if (!moveToPosition(target, 2)) {
                unreachableBlocks.add(target);
                return false;
            }
            return true;
        } else {
            for (int j = 0; j < currentFarm.mainInventory.getSlots(); j++) {
                ItemStack stackInSlot = currentFarm.mainInventory.getStackInSlot(j);
                if (stackInSlot.getItem() instanceof HoeItem) {
                    ItemStack itemInHand = worker.getMainHandItem();
                    if(!itemInHand.isEmpty()){
                        worker.level().addFreshEntity(new ItemEntity(worker.level(),worker.getPosition(0).x,worker.getPosition(0).y,worker.getPosition(0).z,itemInHand.copy()));
                    }
                    worker.setItemInHand(InteractionHand.MAIN_HAND, stackInSlot);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void tick() {
        // slowly forget unreachable blocks
        if (worker.level().getGameTime() % 100 == 0 && !unreachableBlocks.isEmpty()) {
            unreachableBlocks.remove(unreachableBlocks.iterator().next());
        }

        if (distanceTo(currentFarm.getBlockPos()) > 128) {
            if (!moveToPosition(currentFarm.getBlockPos(), 128)) {
                fail();
            }
            return;
        }

        boolean hasHoe = worker.getMainHandItem().getItem() instanceof HoeItem;

        if (!hasHoe) {
            if (!programGetHoe())
                fail();
        }

        if (!programHarvest())
            fail();
    }
}
