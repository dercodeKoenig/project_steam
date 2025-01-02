package Farms.CropFarm;

import Farms.BlockFarmBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static Farms.Registry.ENTITY_CROP_FARM;

public class BlockCropFarm extends BlockFarmBase {
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_CROP_FARM.get().create(blockPos, blockState);
    }
}
