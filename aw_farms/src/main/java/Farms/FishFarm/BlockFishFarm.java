package Farms.FishFarm;

import Farms.BlockFarmBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static Farms.Registry.ENTITY_CROP_FARM;
import static Farms.Registry.ENTITY_FISH_FARM;

public class BlockFishFarm extends BlockFarmBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_FISH_FARM.get().create(blockPos, blockState);
    }
}
