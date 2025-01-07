package NPCs.programs;

import NPCs.WorkerNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;

import java.util.HashMap;
import java.util.Objects;

public class SlowMobNavigation {
    public WorkerNPC worker;
    int failTimeOut = 0;
    int removeInvalidTargetsTime = 20 * 120;
    HashMap<BlockPos, Long> unreachableBlocks = new HashMap<>();

    SlowPathFinder pathFinder;

    public SlowMobNavigation(WorkerNPC worker) {
        this.worker = worker;
        this.pathFinder = new SlowPathFinder(this.worker);
    }

    public boolean isPositionCachedAsInvalid(BlockPos target){
        if (unreachableBlocks.containsKey(target)) {
            if(unreachableBlocks.get(target) + removeInvalidTargetsTime < worker.level().getGameTime()){
                unreachableBlocks.remove(target);
            }else {
                return true;
            }
        }
        return false;
    }

    public ExitCode moveToPosition(BlockPos target, int precision, int maxDistace, int maxNodesVisit, int steps) {
        if (target == null) return ExitCode.EXIT_FAIL;

        double distToTarget = ProgramUtils.distanceManhattan(worker, target);
        if (distToTarget <= precision + 1) {
            return ExitCode.EXIT_SUCCESS;
        }
        if(isPositionCachedAsInvalid(target)){
            return ExitCode.EXIT_FAIL;
        }

        if (worker.getNavigation().getPath() == null ||
                !Objects.equals(worker.getNavigation().getPath().getTarget(), target)) {
            failTimeOut = 0;
            long t0 = System.nanoTime();
            SlowPathFinder.PathFindExit exit = pathFinder.findPath(target,maxDistace,precision,maxNodesVisit,steps);
            long t1 = System.nanoTime();
            System.out.println("navigator exit code: "+exit.exitCode+":"+(double)(t1-t0) / 1000 / 1000);

            if(exit.exitCode.isFailed()) {
                // target can not be reached
                unreachableBlocks.put(target,worker.level().getGameTime());
                return ExitCode.EXIT_FAIL;
            }
            if(exit.exitCode.isStillRunning()) {
                return ExitCode.SUCCESS_STILL_RUNNING;
            }
            if(exit.exitCode.isCompleted()) {
                worker.getNavigation().moveTo(exit.path,1);
                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }


        if (worker.getNavigation().isStuck() || worker.getNavigation().isDone()) {
            failTimeOut++;
            if (failTimeOut > 5 && worker.getNavigation().isStuck()) {
                // try to jump if we are stuck
                worker.getJumpControl().jump();
            }
            if (failTimeOut > 10) {
                worker.getNavigation().moveTo((Path)null, 1);
                unreachableBlocks.put(target, worker.level().getGameTime());
                return ExitCode.EXIT_FAIL;
            }
        } else {
            failTimeOut = 0;
        }
        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
