package ProjectSteam.Blocks.Mechanics.Axle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.ENTITY_WOODEN_AXLE;
import static ProjectSteam.Registry.ENTITY_WOODEN_AXLE_ENCASED;

public class BlockWoodenAxleEncased extends BlockAxleBase{
    public BlockWoodenAxleEncased() {
        super(Properties.of().noOcclusion().strength(1.0f));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_WOODEN_AXLE_ENCASED.get().create(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
         return Shapes.block();
    }

}
