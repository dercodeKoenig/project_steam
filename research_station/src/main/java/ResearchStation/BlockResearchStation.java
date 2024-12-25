package ResearchStation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import static ResearchStation.Registry.ENTITY_RESEARCH_STATION;

public class BlockResearchStation extends Block implements EntityBlock {

    public static BooleanProperty HAS_BOOK = BooleanProperty.create("has_book");

    public BlockResearchStation() {
        super(Properties.of().noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(HAS_BOOK,false).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,context.getHorizontalDirection().getOpposite());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
        builder.add(HAS_BOOK);
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_RESEARCH_STATION.get().create(blockPos,blockState);
    }
}
