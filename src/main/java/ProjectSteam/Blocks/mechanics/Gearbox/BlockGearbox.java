package ProjectSteam.Blocks.mechanics.Gearbox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static ProjectSteam.Registry.ENTITY_GEARBOX;

public class BlockGearbox extends Block implements EntityBlock {

    public static EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
    public static Map<Direction, BooleanProperty> solidBlockConnections = new HashMap<>();

    static {
        for (Direction i : Direction.values()) {
            solidBlockConnections.put(i, BooleanProperty.create("solid_conn_"+i.getName()));
        }
    }


    public BlockGearbox() {
        super(Properties.of().noOcclusion().strength(1.0f));
        BlockState state = this.stateDefinition.any();
        state = state.setValue(FACING, Direction.SOUTH);
        for (Direction i : Direction.values()) {
            state = state.setValue(solidBlockConnections.get(i), false);
        }
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        for (Direction i : Direction.values()) {
            builder.add(solidBlockConnections.get(i));
        }
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            if(!placer.isShiftKeyDown())
                state =  state.setValue(FACING, placer.getDirection());
            else
                state =  state.setValue(FACING, placer.getDirection().getOpposite());
        }

        level.setBlock(pos, updateFromNeighbourShapes(state, level, pos),3) ;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        if(neighborState.isSolidRender(level, neighborPos)){
            state = state.setValue(solidBlockConnections.get(direction), true);
        }else{
            state = state.setValue(solidBlockConnections.get(direction), false);
        }
        return state;
    }

        @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_GEARBOX.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityGearbox::tick;
    }
}