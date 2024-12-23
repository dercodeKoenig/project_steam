package ProjectSteam.Blocks.Mechanics.TJunction;

import ProjectSteam.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static ProjectSteam.Registry.ENTITY_WOODEN_TJUNCTION;

public class EntityWoodenTJuntion extends EntityTJunctionBase {
    public EntityWoodenTJuntion(BlockPos pos, BlockState blockState) {
        super(ENTITY_WOODEN_TJUNCTION.get(),pos, blockState);

        maxStress = Config.INSTANCE.WOODEN_T_JUNCTION_MAX_STRESS;
        myInertia = Config.INSTANCE.WOODEN_T_JUNCTION_INERTIA;
        myFriction = Config.INSTANCE.WOODEN_T_JUNCTION_FRICTION;
    }
}
