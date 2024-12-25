package ProjectSteamAW2Generators.StirlingGenerator;

import ProjectSteamAW2Generators.Config.Config;
import ProjectSteamAW2Generators.WaterWheel.EntityWaterWheelGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static ProjectSteamAW2Generators.Registry.ENTITY_STIRLING_GENERATOR;
import static ProjectSteamAW2Generators.Registry.ENTITY_WATERWHEEL_GENERATOR;

public class BlockStirlingGenerator extends Block implements EntityBlock {

    public BlockStirlingGenerator() {
        super(Properties.of().noOcclusion().strength(1.0f));
        BlockState state = this.stateDefinition.any();
        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        this.registerDefaultState(state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Max Force: "+ Config.INSTANCE.stirlingGenerator_maxForceMultiplier));
        tooltipComponents.add(Component.literal("Max Speed: "+ Config.INSTANCE.stirlingGenerator_maxForceMultiplier / Config.INSTANCE.stirlingGenerator_k + "rad/s"));
        tooltipComponents.add(Component.literal("Friction: "+Config.INSTANCE.stirlingGenerator_friction));
        tooltipComponents.add(Component.literal("Inertia: "+Config.INSTANCE.stirlingGenerator_inertia));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_STIRLING_GENERATOR.get().create(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            if(placer.isShiftKeyDown())
                level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, placer.getDirection()), 3);
            else
                level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, placer.getDirection().getOpposite()), 3);
        }
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof EntityStirlingGenerator s){
            s.openGui();
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity me = level.getBlockEntity(pos);
        if (me instanceof EntityStirlingGenerator s) {
            if (!me.getLevel().isClientSide) {
                ItemStack stack = s.inventory.getStackInSlot(0).copy();
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityStirlingGenerator::tick;
    }
}
