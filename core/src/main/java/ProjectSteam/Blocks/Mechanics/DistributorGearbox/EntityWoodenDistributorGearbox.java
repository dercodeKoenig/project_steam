package ProjectSteam.Blocks.Mechanics.DistributorGearbox;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_DISTRIBUTOR_GEARBOX;

public class EntityWoodenDistributorGearbox extends EntityDistributorGearboxBase{

    public EntityWoodenDistributorGearbox(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_DISTRIBUTOR_GEARBOX.get(), pos, blockState);

        maxStress = Config.INSTANCE.WOODEN_DISTRIBUTOR_GEARBOX_MAX_STRESS;
        myInertia = Config.INSTANCE.WOODEN_DISTRIBUTOR_GEARBOX_INERTIA;
        myFriction = Config.INSTANCE.WOODEN_DISTRIBUTOR_GEARBOX_FRICTION;
    }
}
