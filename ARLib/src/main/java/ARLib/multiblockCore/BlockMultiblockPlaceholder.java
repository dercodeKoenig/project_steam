package ARLib.multiblockCore;

import ARLib.network.PacketBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import static ARLib.ARLibRegistry.ENTITY_PLACEHOLDER;

public class BlockMultiblockPlaceholder extends BlockMultiblockPart implements EntityBlock {
    //public final Map<BlockPos, BlockState> replacedStates = new HashMap<>();

    public BlockMultiblockPlaceholder(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return ENTITY_PLACEHOLDER.get().create(blockPos, blockState);
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity me = world.getBlockEntity(pos);
        if(me instanceof  EntityMultiblockPlaceholder emp){
            return emp.replacedState.getBlock().defaultBlockState().getShape(world,pos, context);
        }
         return Shapes.create(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    }

    // This method will drop the replaced block when the placeholder block is broken
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!world.isClientSide) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof EntityMultiblockPlaceholder tileE) {
                if (willHarvest) {
                    ItemStack stack = new ItemStack(tileE.replacedState.getBlock());
                    popResource(world, pos, stack);
                }
            }
        }
        return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
    }
}