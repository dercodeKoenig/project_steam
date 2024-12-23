package ProjectSteam.Blocks.Mechanics.Gearbox;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_GEARBOX;

public class EntityWoodenGearbox extends EntityGearboxBase{
    public EntityWoodenGearbox(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_GEARBOX.get(), pos, blockState);

        maxStress = Config.INSTANCE.WOODEN_GEARBOX_MAX_STRESS;
        myInertia = Config.INSTANCE.WOODEN_GEARBOX_INERTIA;
        myFriction = Config.INSTANCE.WOODEN_GEARBOX_FRICTION;
    }
}
