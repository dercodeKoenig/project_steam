package NPCs.programs.TreeFarming;

import NPCs.WorkerNPC;
import NPCs.programs.ProgramUtils;
import WorkSites.TreeFarm.EntityTreeFarm;
import WorkSites.EntityWorkSiteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.HashMap;

import static NPCs.programs.ProgramUtils.*;

public class MainLumberjackProgram extends Goal {

    public HashMap<BlockPos, Long> workCheckedTracker = new HashMap<>();

    public WorkerNPC worker;
    public int timeoutForWorkCheck = 20 * 10;

    public TreeFarmingProgram treeFarmingProgram;
    public UnloadInventoryToFarmProgram unloadInventoryProgram;

    public MainLumberjackProgram(WorkerNPC worker) {
        this.worker = worker;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));

        unloadInventoryProgram = new UnloadInventoryToFarmProgram(worker);
        treeFarmingProgram = new TreeFarmingProgram(worker);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public boolean hasWorkAtCropFarm(BlockPos p) {

        BlockEntity e = worker.level().getBlockEntity(p);
        if (!(e instanceof EntityTreeFarm farm)) return false;

        if (treeFarmingProgram.recalculateHasWork(farm)) return true;

        // check if he can unload his inventory there
        if (unloadInventoryProgram.recalculateHasWork(farm)) return true;

        return false;
    }

    @Override
    public boolean canUse() {

        if(worker.level().isNight())return false;


        // make sure he does not just switch to this worksite while another worksite is active (if last position != null)
        // except he can switch to this program if the last worksite was of this program (eg after sleep, server restart)
        if (worker.lastWorksitePosition != null) {
            if (worker.level().isLoaded(worker.lastWorksitePosition)) {
                BlockEntity worksite = worker.level().getBlockEntity(worker.lastWorksitePosition);
                if (worksite instanceof EntityWorkSiteBase w) {
                    return true;
                }
            }
            return false;
        }

        //clean up entries that no longer exist
        for (BlockPos i : workCheckedTracker.keySet()) {
            if (!EntityTreeFarm.knownTreeFarms.contains(i)) {
                workCheckedTracker.remove(i);
                break;
            }
        }

        long gameTime = worker.level().getGameTime();
        for (BlockPos p : ProgramUtils.sortBlockPosByDistanceToNPC(EntityTreeFarm.knownTreeFarms, worker)) {

            if(ProgramUtils.distanceManhattan(worker, p.getCenter()) > 256) break;

            BlockEntity worksite = worker.level().getBlockEntity(p);
            if (worksite instanceof EntityWorkSiteBase w) {

                if (w.workersWorkingHereWithTimeout.size() >= w.maxWorkersAllowed)
                    //if (w.workersWorkingHereWithTimeout.size() >= 6)
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
        return worker.lastWorksitePosition != null && !worker.level().isNight();
    }

    @Override
    public void tick() {
        long t0 = System.nanoTime();
        int e = run();
        long t1 = System.nanoTime();
        //System.out.println((double)(t1-t0) / 1000 / 1000);
        if (e == EXIT_SUCCESS || e == EXIT_FAIL) worker.lastWorksitePosition = null;
    }

    public int run() {
        if (worker.lastWorksitePosition == null) return EXIT_FAIL;

        BlockEntity e = worker.level().getBlockEntity(worker.lastWorksitePosition);
        if (!(e instanceof EntityTreeFarm farm)) return EXIT_FAIL;

        farm.workersWorkingHereWithTimeout.put(worker, 0);

        // try to farm
            int cropFarmingExit = treeFarmingProgram.run(farm);
            if (cropFarmingExit == EXIT_FAIL) return EXIT_FAIL;
            if (cropFarmingExit == SUCCESS_STILL_RUNNING) return SUCCESS_STILL_RUNNING;

        // try to unload Inventory
        int tryUnloadExit = unloadInventoryProgram.run(farm);
        if (tryUnloadExit == EXIT_FAIL) return EXIT_FAIL;
        if (tryUnloadExit == SUCCESS_STILL_RUNNING) return SUCCESS_STILL_RUNNING;

        return EXIT_SUCCESS;
    }
}