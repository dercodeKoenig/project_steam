
























package BetterPipes;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.util.function.Supplier;

public class PacketFluidAmountUpdate {

    public PacketFluidAmountUpdate(BlockPos pos, int direction, int amount, long time) {
        this.pos = pos;
        this.direction = direction;
        this.amount = amount;
        this.time = time;
    }

    int amount;
    long time;
    BlockPos pos;
    int direction;


    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(direction);
        buf.writeInt(amount);
        buf.writeLong(time);
    }

    public static PacketFluidAmountUpdate read(FriendlyByteBuf buf) {
        return new PacketFluidAmountUpdate(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readLong());
    }
    @OnlyIn(Dist.CLIENT)
    public void _handle() {
        Level world = Minecraft.getInstance().level;
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof EntityPipe pipe) {
            if (direction == -1) {
                pipe.setFluidAmountInTank(amount, time);
            } else {
                pipe.connections.get(Direction.values()[direction]).setFluidAmountInTank(amount, time);
            }
        }
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
            _handle();
    }
}

