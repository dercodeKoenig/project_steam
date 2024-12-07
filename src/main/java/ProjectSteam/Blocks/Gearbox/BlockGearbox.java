package ProjectSteam.Blocks.Gearbox;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static ProjectSteam.Registry.ENTITY_DISTRIBUTOR_GEARBOX;
import static ProjectSteam.Registry.ENTITY_GEARBOX;

public class BlockGearbox extends Block implements EntityBlock {

    public static EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);


    public BlockGearbox() {
        super(Properties.of().noOcclusion().strength(1.0f));
        BlockState state = this.stateDefinition.any();
        state = state.setValue(FACING, Direction.SOUTH);
        this.registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Vec3 lookVec = placer.getLookAngle();
            Direction out = Direction.SOUTH;

            float ymult = 0.6f;
            if (Math.abs(lookVec.x) < Math.abs(lookVec.y * ymult) && Math.abs(lookVec.z) < Math.abs(lookVec.y * ymult)) {
                if(lookVec.y > 0)
                    out = Direction.DOWN;
                if(lookVec.y < 0)
                    out = Direction.UP;
            }else{
                out = placer.getDirection().getClockWise();
            }

            // Set the block state with the correct axis
            level.setBlock(pos, state.setValue(FACING, out), 3);
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