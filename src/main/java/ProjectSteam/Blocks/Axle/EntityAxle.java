package ProjectSteam.Blocks.Axle;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

import static ProjectSteam.Registry.ENTITY_AXLE;

public class EntityAxle extends BlockEntity implements INetworkTagReceiver {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    int lastLight = 0;

    public EntityAxle(BlockPos pos, BlockState blockState) {
        super(ENTITY_AXLE.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level.isClientSide) {
            UUID from = Minecraft.getInstance().player.getUUID();
            CompoundTag tag = new CompoundTag();
            tag.putUUID("client_onload", from);
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, tag));
        }
    }

    @Override
    public void setRemoved() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer.close();
            });

        }
        super.setRemoved();
    }

    //public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
    //    ((EntityAxle) t).tick();
    //}

    @Override
    public void readServer(CompoundTag compoundTag) {
        if (compoundTag.contains("client_onload")) {
            UUID from = compoundTag.getUUID("client_onload");
            ServerPlayer playerFrom = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            CompoundTag updateTag = new CompoundTag();

            updateTag.putLong("time", System.currentTimeMillis());
            PacketDistributor.sendToPlayer(playerFrom, PacketBlockEntity.getBlockEntityPacket(this, updateTag));
        }
    }

    long lastUpdate = 0;

    @Override
    public void readClient(CompoundTag compoundTag) {
        if (compoundTag.contains(("time"))) {
            long updateTime = compoundTag.getLong("time");
            if (updateTime >= lastUpdate) {

            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

    }
}