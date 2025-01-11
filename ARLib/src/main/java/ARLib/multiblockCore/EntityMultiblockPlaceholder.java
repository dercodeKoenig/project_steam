package ARLib.multiblockCore;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.guiModuleEnergy;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

import static ARLib.ARLibRegistry.ENTITY_ENERGY_INPUT_BLOCK;
import static ARLib.ARLibRegistry.ENTITY_PLACEHOLDER;

public class EntityMultiblockPlaceholder extends BlockEntity implements INetworkTagReceiver{

    public BlockState replacedState = Blocks.AIR.defaultBlockState();
    public boolean renderBlock = false;

    public EntityMultiblockPlaceholder(BlockPos p_155229_, BlockState p_155230_) {
        this(ENTITY_PLACEHOLDER.get(), p_155229_, p_155230_);
    }

    public EntityMultiblockPlaceholder(BlockEntityType type, BlockPos p_155229_, BlockState p_155230_) {
        super(type, p_155229_, p_155230_);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(!level.isClientSide) {

        }else{
            CompoundTag info = new CompoundTag();
            info.put("ping", new CompoundTag());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, info));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        renderBlock = tag.getBoolean("renderBlock");
        if (tag.contains("BlockState")) {
            CompoundTag blockStateNbt = tag.getCompound("BlockState");
            DataResult<BlockState> decodedBlockState = BlockState.CODEC.parse(NbtOps.INSTANCE, blockStateNbt);
            replacedState = decodedBlockState.getOrThrow();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("renderBlock", renderBlock);
        if (replacedState != null) {
            DataResult<CompoundTag> encodedBlockState = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, replacedState)
                    .map(nbtTag -> (CompoundTag) nbtTag);
            tag.put("BlockState", encodedBlockState.getOrThrow());
        }
    }

    @Override
    public void readServer(CompoundTag tag, ServerPlayer player) {
        if (tag.contains("ping")) {

            CompoundTag response = new CompoundTag();
            DataResult<CompoundTag> encodedBlockState = BlockState.CODEC.encodeStart(NbtOps.INSTANCE, replacedState)
                    .map(nbtTag -> (CompoundTag) nbtTag);
            response.put("BlockState", encodedBlockState.getOrThrow());
            response.putBoolean("renderBlock", renderBlock);
            PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(this, response));
        }
    }

    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("BlockState")) {
            CompoundTag blockStateNbt = tag.getCompound("BlockState");
            DataResult<BlockState> decodedBlockState = BlockState.CODEC.parse(NbtOps.INSTANCE, blockStateNbt);
            replacedState = decodedBlockState.getOrThrow();
        }
        if(tag.contains("renderBlock")) {
            renderBlock = tag.getBoolean("renderBlock");
        }
    }
}
