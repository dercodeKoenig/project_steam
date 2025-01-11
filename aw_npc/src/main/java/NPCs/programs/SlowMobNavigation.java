package NPCs.programs;

import NPCs.NPCBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;

import java.util.HashMap;
import java.util.Objects;

public class SlowMobNavigation {
    public NPCBase npc;
    int failTimeOut = 0;
    int removeInvalidTargetsTime = 20 * 120;
    HashMap<BlockPos, Long> unreachableBlocks = new HashMap<>();

    SlowPathFinder pathFinder;

    public SlowMobNavigation(NPCBase npc) {
        this.npc = npc;
        this.pathFinder = new SlowPathFinder(this.npc);
    }

    public boolean isPositionCachedAsInvalid(BlockPos target){
        if (unreachableBlocks.containsKey(target)) {
            if(unreachableBlocks.get(target) + removeInvalidTargetsTime < npc.level().getGameTime()){
                unreachableBlocks.remove(target);
            }else {
                return true;
            }
        }
        return false;
    }

    public ExitCode moveToPosition(BlockPos target, int precision, int maxDistace, int maxNodesVisit, int steps) {
        //System.out.println("move to "+target+":"+precision);

        if (target == null) return ExitCode.EXIT_FAIL;

        double distToTarget = ProgramUtils.distanceManhattan(npc, target.getCenter());
        if (distToTarget <= precision +2) {
            return ExitCode.EXIT_SUCCESS;
        }
        if(isPositionCachedAsInvalid(target)){
//            return ExitCode.EXIT_FAIL;
        }

        if (npc.getNavigation().getPath() == null ||
                !Objects.equals(npc.getNavigation().getPath().getTarget(), target)||
                npc.getNavigation().isDone()
        ) {
            failTimeOut = 0;
            //long t0 = System.nanoTime();
            SlowPathFinder.PathFindExit exit = pathFinder.findPath(target,maxDistace,precision,maxNodesVisit,steps);
            //long t1 = System.nanoTime();
            //System.out.println("navigator exit code: "+exit.exitCode+":"+(double)(t1-t0) / 1000 / 1000);

            if(exit.exitCode.isFailed()) {
                // target can not be reached
                unreachableBlocks.put(target, npc.level().getGameTime());
                return ExitCode.EXIT_FAIL;
            }
            if(exit.exitCode.isStillRunning()) {
                return ExitCode.SUCCESS_STILL_RUNNING;
            }
            if(exit.exitCode.isCompleted()) {
                npc.getNavigation().moveTo(exit.path,1);
                return ExitCode.SUCCESS_STILL_RUNNING;
            }
        }


        if (npc.getNavigation().isStuck() || npc.getNavigation().isDone()) {
            failTimeOut++;
            if (failTimeOut > 5 && npc.getNavigation().isStuck()) {
                // try to jump if we are stuck
                npc.getJumpControl().jump();
            }
            if (failTimeOut > 10) {
                npc.getNavigation().moveTo((Path)null, 1);
                unreachableBlocks.put(target, npc.level().getGameTime());
                return ExitCode.EXIT_FAIL;
            }
        } else {
            failTimeOut = 0;
        }
        return ExitCode.SUCCESS_STILL_RUNNING;
    }
}
