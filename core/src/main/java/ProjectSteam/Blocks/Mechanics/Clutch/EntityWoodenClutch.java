package ProjectSteam.Blocks.Mechanics.Clutch;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_CLUTCH;

public class EntityWoodenClutch extends EntityClutchBase {
    public EntityWoodenClutch(BlockPos pos, BlockState blockState) {
        super(ENTITY_CLUTCH.get(), pos, blockState);

        super.inertiaPerSide = Config.INSTANCE.WOODEN_CLUTCH_INERTIA_PER_SIDE;
        super.baseFrictionPerSide = Config.INSTANCE.WOODEN_CLUTCH_FRICTION_PER_SIDE;
        super.maxStress = Config.INSTANCE.WOODEN_CLUTCH_MAX_STRESS;
    }
}
