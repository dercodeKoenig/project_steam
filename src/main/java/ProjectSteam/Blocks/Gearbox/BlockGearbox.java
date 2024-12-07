package ProjectSteam.Blocks.Gearbox;

import ProjectSteam.api.IMechanicalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

import static ProjectSteam.Registry.ENTITY_AXLE;
import static ProjectSteam.Registry.ENTITY_GEARBOX;

public class BlockGearbox extends Block implements EntityBlock {

    public static EnumProperty<Direction.Axis> ROTATION_AXIS = EnumProperty.create("axis", Direction.Axis.class);


    public BlockGearbox() {
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Vec3 lookVec = placer.getLookAngle();
            Direction.Axis newAxis = Direction.Axis.Y;

            float ymult = 0.6f;
            if (Math.abs(lookVec.x) < Math.abs(lookVec.y * ymult) && Math.abs(lookVec.z) < Math.abs(lookVec.y * ymult)) {
                if(Math.abs(lookVec.x) > Math.abs(lookVec.z))
                    newAxis = Direction.Axis.X; // Dominant X-axis
                if(Math.abs(lookVec.x) < Math.abs(lookVec.z))
                    newAxis = Direction.Axis.Z; // Dominant Z-axis
            }

            // Set the block state with the correct axis
            level.setBlock(pos, state.setValue(ROTATION_AXIS, newAxis), 3);
        }

        super.setPlacedBy(level, pos, state, placer, stack); // Call the super method for any additional behavior
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