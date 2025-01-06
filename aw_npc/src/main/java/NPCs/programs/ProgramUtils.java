package NPCs.programs;

import NPCs.WorkerNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public class ProgramUtils {
    public static TreeSet<BlockPos> sortBlockPosByDistanceToWorkerNPC(Collection<BlockPos> list, WorkerNPC worker) {
        Vec3 position = worker.getPosition(0);
        TreeSet<BlockPos> sorted = new TreeSet<>(new Comparator<BlockPos>() {
            @Override
            public int compare(BlockPos o1, BlockPos o2) {
                double d1 = o1.getCenter().distanceTo(position);
                double d2 = o2.getCenter().distanceTo(position);
                if (d1 > d2) return 1;
                if (d1 < d2) return -1;
                else return 0;
            }
        });
        sorted.addAll(list);
        return sorted;
    }
}
