package ARLib.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

// INetworkPacket.java
public interface INetworkItemStackTagReceiver {
    void readServer(CompoundTag tag, ItemStack stack, UUID sender);
    void readClient(CompoundTag tag);
}
