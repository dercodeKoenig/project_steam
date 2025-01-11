package ARLib.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

// INetworkPacket.java
public interface INetworkTagReceiver {
    void readServer(CompoundTag tag, ServerPlayer sender);
    void readClient(CompoundTag tag);
}
