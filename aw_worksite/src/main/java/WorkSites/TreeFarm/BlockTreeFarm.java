package WorkSites.TreeFarm;


import WorkSites.BlockWorkSiteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static WorkSites.Registry.ENTITY_TREE_FARM;


public class BlockTreeFarm extends BlockWorkSiteBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_TREE_FARM.get().create(blockPos, blockState);
    }
}
