package ARLib.blocks;

import ARLib.blockentities.EntityFluidInputBlock;
import ARLib.multiblockCore.EntityMultiblockMaster;
import ARLib.multiblockCore.BlockMultiblockPart;
import ARLib.network.PacketBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static ARLib.ARLibRegistry.*;

public class BlockFluidInputBlock extends BlockMultiblockPart implements EntityBlock {

    public BlockFluidInputBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ENTITY_FLUID_INPUT_BLOCK.get().create(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!world.isClientSide) {
            if (super.useWithoutItem(state, world, pos, player, hitResult) == InteractionResult.PASS) {
                CompoundTag info = new CompoundTag();
                info.putByte("openGui", (byte) 0);
                PacketDistributor.sendToPlayer((ServerPlayer) player, PacketBlockEntity.getBlockEntityPacket(world, pos, info));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return EntityFluidInputBlock::tick;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this,1));
        return drops;
    }
}
