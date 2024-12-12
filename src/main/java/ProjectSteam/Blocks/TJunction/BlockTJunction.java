package ProjectSteam.Blocks.TJunction;

import ProjectSteam.core.AbstractMechanicalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static ProjectSteam.Registry.ENTITY_TJUNCTION;

public class BlockTJunction extends Block implements EntityBlock {

    public static EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class);
    public static EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);

    public static Map<Direction, BooleanProperty> solidBlockConnections = new HashMap<>();

    public static BooleanProperty INVERTED = BooleanProperty.create("inverted");

    static {
        for (Direction i : Direction.values()) {
            solidBlockConnections.put(i, BooleanProperty.create("solid_conn_"+i.getName()));
        }
    }

    public BlockTJunction() {
        super(Properties.of().noOcclusion().strength(1.0f));
        BlockState state = this.stateDefinition.any();
        state = state.setValue(AXIS, Direction.Axis.X);
        state = state.setValue(INVERTED, false);
        state = state.setValue(FACING, Direction.SOUTH);
        for (Direction i : Direction.values()) {
            state = state.setValue(solidBlockConnections.get(i), false);
        }
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
        builder.add(FACING);
        builder.add(INVERTED);
        for (Direction i : Direction.values()) {
            builder.add(solidBlockConnections.get(i));
        }
        super.createBlockStateDefinition(builder);
    }
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof EntityTJunction tj) {
                Direction cf = state.getValue(FACING);
                Direction nf = cf;
                Direction.Axis ca = state.getValue(AXIS);
                boolean isInverted = state.getValue(INVERTED);

                if (ca == Direction.Axis.X) {
                    if (cf == Direction.UP) nf = Direction.NORTH;
                    if (cf == Direction.NORTH) nf = Direction.DOWN;
                    if (cf == Direction.DOWN) nf = Direction.SOUTH;
                    if (cf == Direction.SOUTH) nf = Direction.UP;
                }
                if (ca == Direction.Axis.Z) {
                    if (cf == Direction.UP) nf = Direction.EAST;
                    if (cf == Direction.EAST) nf = Direction.DOWN;
                    if (cf == Direction.DOWN) nf = Direction.WEST;
                    if (cf == Direction.WEST) nf = Direction.UP;
                }
                //System.out.println(nf+":"+cf+":"+ca);
                state = state.setValue(FACING, nf);
                level.setBlock(pos, state, 3);

               tj.myMechanicalBlock. propagateResetRotation(0, null, new HashSet<AbstractMechanicalBlock>());

                return InteractionResult.SUCCESS_NO_ITEM_USED;
            }
        }
        return InteractionResult.PASS;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Vec3 lookVec = placer.getLookAngle();
            Direction.Axis newAxis = Direction.Axis.X;
            Direction newFacing = placer.getDirection().getOpposite();

            //if (Math.abs(lookVec.y) < 0.8) { // maybe later....
                if(Math.abs(lookVec.x) > Math.abs(lookVec.z)) {
                    newAxis = Direction.Axis.Z; // Dominant X-axis
                }
                if(Math.abs(lookVec.x) < Math.abs(lookVec.z)) {
                    newAxis = Direction.Axis.X; // Dominant Z-axis
                }
            //}else{
                //newAxis = Direction.Axis.Y;
            //}

            state = state.setValue(AXIS, newAxis);
            state = state.setValue(FACING, newFacing);
            level.setBlock(pos, updateFromNeighbourShapes(state, level, pos),3) ;
        }

        super.setPlacedBy(level, pos, state, placer, stack); // Call the super method for any additional behavior
    }
        @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_TJUNCTION.get().create(pos, state);
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityTJunction::tick;
    }
}