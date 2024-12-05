package ProjectSteam.Blocks.Axle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.*;

public class BlockAxle extends Block implements EntityBlock {

    public enum axis implements StringRepresentable {
        Y("y"),
        X("x"),
        Z("z");

        private final String name;

        axis(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static EnumProperty<axis> ROTATION_AXIS = EnumProperty.create("axis", axis.class);

    public BlockAxle(Properties properties) {
        super(properties);
        BlockState state = this.stateDefinition.any();
        state = state.setValue(ROTATION_AXIS, axis.Y);
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION_AXIS);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_AXLE.get().create(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Vec3 lookVec = placer.getLookAngle();
            axis newAxis;;
            float ymult = 0.8f;
            if (Math.abs(lookVec.x) > Math.abs(lookVec.y*ymult) && Math.abs(lookVec.x) > Math.abs(lookVec.z)) {
                newAxis = axis.X; // Dominant X-axis
            } else if (Math.abs(lookVec.z) > Math.abs(lookVec.x) && Math.abs(lookVec.z) > Math.abs(lookVec.y*ymult)) {
                newAxis = axis.Z; // Dominant Z-axis
            } else {
                newAxis = axis.Y; // Dominant Y-axis
            }


            // Set the block state with the correct axis
            level.setBlock(pos, state.setValue(ROTATION_AXIS, newAxis), 3);
        }

        super.setPlacedBy(level, pos, state, placer, stack); // Call the super method for any additional behavior
    }


    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    //@Override
    //public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    //    return EntityAxle::tick;
    //}
}