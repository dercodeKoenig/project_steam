package ProjectSteam.Blocks.Axle;

import ProjectSteam.api.IMechanicalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.*;

public class BlockAxle extends Block implements EntityBlock {

    public static EnumProperty<Direction.Axis> ROTATION_AXIS = EnumProperty.create("axis", Direction.Axis.class);

    public BlockAxle() {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(1.0f));
        BlockState state = this.stateDefinition.any();
        state = state.setValue(ROTATION_AXIS, Direction.Axis.Y);
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION_AXIS);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_AXLE.get().create(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Vec3 lookVec = placer.getLookAngle();
            Direction.Axis newAxis;;

            if (Math.abs(lookVec.y)< 0.7) {
                newAxis = placer.getDirection().getClockWise().getAxis();
            } else {
                newAxis = Direction.Axis.Y; // Dominant Y-axis
            }


            // Set the block state with the correct axis
            level.setBlock(pos, state.setValue(ROTATION_AXIS, newAxis), 3);
        }

        super.setPlacedBy(level, pos, state, placer, stack); // Call the super method for any additional behavior

        state = level.getBlockState(pos);
        level.setBlock(pos, updateFromNeighbourShapes(state, level, pos),3) ;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof IMechanicalBlock mechBlock) {
            if (mechBlock.getConnectedParts(tile, state).isEmpty()) {
                BlockEntity neighbor = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(direction));
                if (neighbor instanceof IMechanicalBlock otherMechBlock) {
                    if (otherMechBlock.connectsAtFace(direction.getOpposite(), null)) {
                        state = state.setValue(ROTATION_AXIS, direction.getAxis());
                    }
                }
            }
        }
        return state;
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.create(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    }

    
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityAxle::tick;
    }
}