package NPCs.programs;

import NPCs.WorkerNPC;
import NPCs.programs.CropFarming.*;
import WorkSites.CropFarm.EntityCropFarm;
import WorkSites.EntityWorkSiteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.HashMap;

public class MainFarmingProgram extends Goal {

    public HashMap<BlockPos, Long> workCheckedTracker = new HashMap<>();

    public WorkerNPC worker;
    public EntityCropFarm currentFarm;
    public int timeoutForWorkCheck = 20 * 10;

    public TakeSeedsProgram takeSeedsProgram;
    public PlantProgram plantProgram;
    public TillProgram tillProgram;
    public HarvestProgram harvestProgram;
    public UnloadInventoryProgram unloadInventoryProgram;
    public TakeHoeProgram takeHoeProgram;

    public MainFarmingProgram(WorkerNPC worker) {
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        plantProgram = new PlantProgram(this);
        tillProgram = new TillProgram(this);
        harvestProgram = new HarvestProgram(this);
        unloadInventoryProgram = new UnloadInventoryProgram(this);
        takeSeedsProgram = new TakeSeedsProgram(this);
        takeHoeProgram = new TakeHoeProgram(this);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public boolean hasWorkAtCropFarm(BlockPos p) {

        BlockEntity e = worker.level().getBlockEntity(p);
        if (!(e instanceof EntityCropFarm farm)) return false;

        // if the worker can plant any seeds on the farm it has work
        if (plantProgram.recalculateHasWork(farm)) return true;

        // if the worker can take any seeds that can be planted on the farm after taking it has work
        if (takeSeedsProgram.recalculateHasWork(farm)) return true;

        // if something can be tilled....
        if (tillProgram.recalculateHasWork(farm)) return true;

        // check if he can harvest anything
        if (harvestProgram.recalculateHasWork(farm)) return true;

        // check if he can unload his inventory there
        if (unloadInventoryProgram.recalculateHasWork(farm)) return true;

        return false;
    }


    @Override
    public boolean canUse() {

        // make sure he does not just switch to this worksite while another worksite is active (if last position != null)
        // except he can switch to this program if the last worksite was of this program (eg after sleep, server restart)
        if(worker.lastWorksitePosition != null){
            if(worker.level().isLoaded(worker.lastWorksitePosition)) {
                BlockEntity worksite = worker.level().getBlockEntity(worker.lastWorksitePosition);
                if (worksite instanceof EntityWorkSiteBase w) {
                    return true;
                }
            }
            return false;
        }

        //clean up entries that no longer exist
        for (BlockPos i : workCheckedTracker.keySet()) {
            if (!EntityCropFarm.knownCropFarms.contains(i)) {
                workCheckedTracker.remove(i);
                break;
            }
        }

        long gameTime = worker.level().getGameTime();
        for (BlockPos p : ProgramUtils.sortBlockPosByDistanceToWorkerNPC(EntityCropFarm.knownCropFarms, worker)) {
            BlockEntity worksite = worker.level().getBlockEntity(p);
            if(worksite instanceof EntityWorkSiteBase w) {

                if (w.workersWorkingHereWithTimeout.size() >= w.maxWorkersAllowed)
                    continue;

                if (workCheckedTracker.containsKey(p)) {
                    if (workCheckedTracker.get(p) + timeoutForWorkCheck > gameTime)
                        continue;
                }

                workCheckedTracker.put(p, gameTime);
                if (hasWorkAtCropFarm(p)) {
                    worker.lastWorksitePosition = p;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return worker.lastWorksitePosition != null;
    }

    @Override
    public void tick() {
        long t0 = System.nanoTime();
        ExitCode e = run();
        long t1 = System.nanoTime();
        //System.out.println((double)(t1-t0) / 1000 / 1000);
        if (e.isEnd()) worker.lastWorksitePosition = null;
    }

    public ExitCode run() {
        if (worker.lastWorksitePosition == null) return  ExitCode.EXIT_FAIL;

        BlockEntity e = worker.level().getBlockEntity(worker.lastWorksitePosition);
        if (!(e instanceof EntityCropFarm farm)) return ExitCode.EXIT_FAIL;

        farm.workersWorkingHereWithTimeout.put(worker, 0);

        currentFarm = farm;

        // try to harvest
        ExitCode tryHarvestExit = harvestProgram.run();
        if (tryHarvestExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (tryHarvestExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to take seeds if required
        ExitCode takeSeedExit = takeSeedsProgram.run();
        if(takeSeedExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;
        if(takeSeedExit.isFailed()) return ExitCode.EXIT_SUCCESS;

        // try to plant
        ExitCode tryPlantExit = plantProgram.run();
        if (tryPlantExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (tryPlantExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to till
        ExitCode tryTillExit = tillProgram.run();
        if (tryTillExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (tryTillExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        // try to unload Inventory
        ExitCode tryUnloadExit = unloadInventoryProgram.run();
        if (tryUnloadExit.isFailed()) return ExitCode.EXIT_FAIL;
        if (tryUnloadExit.isStillRunning()) return ExitCode.SUCCESS_STILL_RUNNING;

        return ExitCode.EXIT_SUCCESS;
    }


    public ExitCode moveNearFarm(int precision) {
        return worker.slowMobNavigation.moveToPosition(
                currentFarm.getBlockPos(),
                precision,
                worker.slowNavigationMaxDistance,
                worker.slowNavigationMaxNodes,
                worker.slowNavigationStepPerTick
        );
    }
}