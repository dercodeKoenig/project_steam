package BetterPipes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;


public class PacketRequestInitialData {

    public interface clientOnload {
        void clientOnload(ServerPlayer player);
    }


    public PacketRequestInitialData(ResourceLocation dimension, BlockPos pos) {
        this.pos = pos;
        this.dimension = dimension;
    }

    ResourceLocation dimension;
    BlockPos pos;

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension);
        buf.writeBlockPos(pos);
    }

    public static PacketRequestInitialData read(FriendlyByteBuf buf) {
        return new PacketRequestInitialData(buf.readResourceLocation(), buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        Level l = ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        BlockEntity be = l.getBlockEntity(pos);
        if (be instanceof clientOnload c) {
            c.clientOnload((ServerPlayer) ctx.get().getSender());
        }
    }
}

