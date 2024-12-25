package ARLib.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static ARLib.ARLibRegistry.ENTITY_FLUID_OUTPUT_BLOCK;

public class EntityFluidOutputBlock extends EntityFluidInputBlock {
    public EntityFluidOutputBlock(BlockPos pos, BlockState blockState) {
        super(ENTITY_FLUID_OUTPUT_BLOCK.get(), pos, blockState);
    }
}
