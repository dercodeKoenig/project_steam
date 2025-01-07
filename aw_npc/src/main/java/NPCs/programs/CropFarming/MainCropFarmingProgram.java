package NPCs.programs.CropFarming;

import Farms.CropFarm.EntityCropFarm;
import NPCs.WorkerNPC;
import NPCs.programs.ExitCode;
import NPCs.programs.ProgramUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.HashMap;

public class MainCropFarmingProgram extends Goal {

    public HashMap<BlockPos, Long> workCheckedTracker = new HashMap<>();

    public WorkerNPC worker;
    public BlockPos currentFarmPos;
    public EntityCropFarm currentFarm;
    public double cachedDistanceManHattanToFarm;
    public int timeoutForWorkCheck = 20 * 10;
    public boolean canUse = true;

    public TakeHoeProgram takeHoeProgram;
    public TakeSeedsProgram takeSeedsProgram;
    public PlantProgram plantProgram;
    public TillProgram tillProgram;
    public HarvestProgram harvestProgram;
    public UnloadInventoryProgram unloadInventoryProgram;

    public MainCropFarmingProgram(WorkerNPC worker) {
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        takeHoeProgram = new TakeHoeProgram(this);
        takeSeedsProgram = new TakeSeedsProgram(this);
        plantProgram = new PlantProgram(this);
        tillProgram = new TillProgram(this);
        harvestProgram = new HarvestProgram(this);
        unloadInventoryProgram = new UnloadInventoryProgram(this);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public boolean hasWorkAtCropFarm(BlockPos p) {

        BlockEntity e = worker.level().getBlockEntity(p);
        if (!(e instanceof EntityCropFarm farm)) return false;

        // check if the farm has a hoe to take if worker does not already have one
        if (takeHoeProgram.canPickupHoeFromFarm(farm)) return true;

        // check if the worker has any seeds in inventory. if not, check if he can take a seed from the farm. if yes, this farm has work
        if (takeSeedsProgram.recalculateHasWork(farm)) return true;

        // if the worker can plant any seeds on the farm it has work
        if (plantProgram.canPlantAny(farm)) return true;

        // if something can be tilled....
        if (tillProgram.canTillAny(farm)) return true;

        // check if he can harvest anything
        if (harvestProgram.canHarvestAny(farm)) return true;

        // check if he can unload his inventory there
        if (unloadInventoryProgram.recalculateHasWork(farm)) return true;

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
            if (hasWorkAtCropFarm(p)) {
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

        currentFarm = farm;
        cachedDistanceManHattanToFarm = ProgramUtils.distanceManhattan(worker, currentFarmPos);


        // make sure you have a valid hoe item or fail
        ExitCode takeHoeExit = takeHoeProgram.run();
        if (takeHoeExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (takeHoeExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to restock seeds if required and possible
        ExitCode restockSeedExit = takeSeedsProgram.run();
        if (restockSeedExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (restockSeedExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to plant
        ExitCode tryPlantExit = plantProgram.run();
        if (tryPlantExit.isFailed()) return ExitCode.EXIT_FAIL; // this should never fail
        if (tryPlantExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to till
        ExitCode tryTillExit = tillProgram.run();
        if (tryTillExit.isFailed()) return ExitCode.EXIT_FAIL; // this should never fail
        if (tryTillExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to harvest
        ExitCode tryHarvestExit = harvestProgram.run();
        if (tryHarvestExit.isFailed()) return ExitCode.EXIT_FAIL; // this should never fail
        if (tryHarvestExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to unload Inventory
        ExitCode tryUnloadExit = unloadInventoryProgram.run();
        if (tryUnloadExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (tryUnloadExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        return ExitCode.EXIT_SUCCESS;
    }


    public ExitCode moveNearFarm(int precision) {
        return worker.moveToPosition(currentFarm.getBlockPos(), precision);
    }
}