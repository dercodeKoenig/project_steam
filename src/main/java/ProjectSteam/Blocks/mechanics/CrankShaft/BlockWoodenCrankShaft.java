package ProjectSteam.Blocks.Mechanics.CrankShaft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.ENTITY_WOODEN_CRANKSHAFT;

public class BlockWoodenCrankShaft extends BlockCrankShaftBase {
    public BlockWoodenCrankShaft() {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(1.0f));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_WOODEN_CRANKSHAFT.get().create(pos, state);
    }
}