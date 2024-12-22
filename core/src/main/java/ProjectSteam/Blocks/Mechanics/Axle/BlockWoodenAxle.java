package ProjectSteam.Blocks.Mechanics.Axle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.ENTITY_WOODEN_AXLE;

public class BlockWoodenAxle extends BlockAxleBase{
    public BlockWoodenAxle() {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(1.0f));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_WOODEN_AXLE.get().create(pos, state);
    }
}
