package Farms.TreeFarm;

import com.ibm.icu.util.LocaleMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class treescanner {
    private static final Logger log = LoggerFactory.getLogger(treescanner.class);

    public static void scanDefaultTree(Level level, BlockPos pos, BlockPos pMin, BlockPos pMax, Set<BlockPos> leaveSet, Set<BlockPos> logSet) {
        BlockState state = level.getBlockState(pos);
        if (
                pos.getX() >= pMin.getX() &&
                //pos.getY() >= pMin.getY() &&
                pos.getZ() >= pMin.getZ() &&
                pos.getX() <= pMax.getX() &&
                //pos.getY() <= pMax.getY() &&
                pos.getZ() <= pMax.getZ()
        ) {
            if (state.is(BlockTags.LOGS)) {
                if (!logSet.contains(pos)) {
                    logSet.add(pos);
                    for (Direction i : Direction.values()) {
                        scanDefaultTree(level, pos.relative(i), pMin, pMax, leaveSet, logSet);
                    }
                }
            }
            if (state.is(BlockTags.LEAVES)) {
                if (!leaveSet.contains(pos)) {
                    leaveSet.add(pos);
                    for (Direction i : Direction.values()) {
                        scanDefaultTree(level, pos.relative(i), pMin, pMax, leaveSet, logSet);
                    }
                }
            }
        }
    }
}
