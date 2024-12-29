package BetterPipes;

import net.minecraft.nbt.CompoundTag;

// INetworkPacket.java
public interface INetworkTagReceiver {
    void readServer(CompoundTag tag);
    void readClient(CompoundTag tag);
}
