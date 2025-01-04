package Farms.Quarry;

import Farms.BlockFarmBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static Farms.Registry.ENTITY_FISH_FARM;
import static Farms.Registry.ENTITY_QUARRY;

public class BlockQuarry extends BlockFarmBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_QUARRY.get().create(blockPos, blockState);
    }
}
