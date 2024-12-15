package ProjectSteam.Blocks.mechanics.FlyWheel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.ENTITY_WOODEN_FLYWHEEL;

public class BlockWoodenFlyWheel extends BlockFlyWheelBase {
    public BlockWoodenFlyWheel() {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(1.0f));

    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_WOODEN_FLYWHEEL.get().create(pos, state);
    }
}