package Farms.CropFarm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import static Farms.Registry.ENTITY_CROP_FARM;

public class BlockCropFarm extends Block implements EntityBlock {
    public BlockCropFarm() {
        super(Properties.of());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_CROP_FARM.get().create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            if (placer.isShiftKeyDown())
                level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, placer.getDirection()), 3);
            else
                level.setBlock(pos, state.setValue(BlockStateProperties.HORIZONTAL_FACING, placer.getDirection().getOpposite()), 3);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity m = level.getBlockEntity(pos);
        if (m instanceof EntityCropFarm c) {
            c.openMainGui();
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
