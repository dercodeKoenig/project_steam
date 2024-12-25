package ARLib.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

// INetworkPacket.java
public interface INetworkItemStackTagReceiver {
    void readServer(CompoundTag tag, ItemStack stack);
    void readClient(CompoundTag tag);
}
