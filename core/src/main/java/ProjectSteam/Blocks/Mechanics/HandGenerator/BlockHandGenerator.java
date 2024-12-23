package ProjectSteam.Blocks.Mechanics.HandGenerator;

import ProjectSteam.Config.Config;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static ProjectSteam.Registry.ENTITY_HAND_GENERATOR;

public class BlockHandGenerator extends Block implements EntityBlock {

    public static EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);

    public BlockHandGenerator() {
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_HAND_GENERATOR.get().create(pos, state);
    }
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity b = level.getBlockEntity(pos);
        if(b instanceof EntityHandGenerator h)
            if(h.onPlayerClicked(player.isShiftKeyDown())){
                player.causeFoodExhaustion(0.5f);
            }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            if(placer.isShiftKeyDown())
                level.setBlock(pos, state.setValue(FACING, placer.getDirection()), 3);
            else
                level.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 3);
        }

        super.setPlacedBy(level, pos, state, placer, stack); // Call the super method for any additional behavior
    }



    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Max Force: "+ Config.INSTANCE.HAND_GENERATOR_MAX_FORCE));
        tooltipComponents.add(Component.literal("Max Speed: "+ Config.INSTANCE.HAND_GENERATOR_MAX_SPEED));
        tooltipComponents.add(Component.literal("Max Stress: "+ Config.INSTANCE.HAND_GENERATOR_MAX_STRESS));
        tooltipComponents.add(Component.literal("Friction: "+Config.INSTANCE.HAND_GENERATOR_FRICTION));
        tooltipComponents.add(Component.literal("Inertia: "+ Config.INSTANCE.HAND_GENERATOR_INERTIA));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityHandGenerator::tick;
    }
}