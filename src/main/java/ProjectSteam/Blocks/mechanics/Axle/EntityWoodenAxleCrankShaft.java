package ProjectSteam.Blocks.mechanics.Axle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_AXLE_CRANKSHAFT;
import static ProjectSteam.Registry.ENTITY_AXLE_FLYWHEEL;

public class EntityWoodenAxleCrankShaft extends EntityWoodenAxle{

    public EntityWoodenAxleCrankShaft(BlockPos pos, BlockState blockState) {
        super(ENTITY_AXLE_CRANKSHAFT.get(), pos, blockState);
        maxStress = 100;
    }
}