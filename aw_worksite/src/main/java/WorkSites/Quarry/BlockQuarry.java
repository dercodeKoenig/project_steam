package WorkSites.Quarry;


import WorkSites.BlockWorkSiteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static WorkSites.Registry.ENTITY_QUARRY;


public class BlockQuarry extends BlockWorkSiteBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_QUARRY.get().create(blockPos, blockState);
    }
}
