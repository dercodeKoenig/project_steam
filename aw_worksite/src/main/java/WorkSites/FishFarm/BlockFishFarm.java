package WorkSites.FishFarm;


import WorkSites.BlockWorkSiteBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static WorkSites.Registry.ENTITY_FISH_FARM;

public class BlockFishFarm extends BlockWorkSiteBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_FISH_FARM.get().create(blockPos, blockState);
    }
}
