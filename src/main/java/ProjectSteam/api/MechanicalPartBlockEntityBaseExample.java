package ProjectSteam.api;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public abstract class MechanicalPartBlockEntityBaseExample extends BlockEntity implements IMechanicalBlock, INetworkTagReceiver {
    public MechanicalPartBlockEntityBaseExample(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    MechanicalBlockData myMechanicalData = new MechanicalBlockData(this);
    public MechanicalBlockData getMechanicalData(){return myMechanicalData;}


    public double myMass = 1;
    public double myForce = 0;
    public double myFriction = 0;

    public double getMass() {
        return myMass;
    }

    public double getTorqueResistance() {
        return myFriction;
    }

    public double getTorqueProduced() {
        return myForce;
    }
    /*
    public double getTorqueProduced(){
    return myForce * (1-velocity/maxvelocity);
    }
     */


    @Override
    public void onLoad() {
        super.onLoad();
            mechanicalOnload();
    }


    public void tick() {
mechanicalTick();
    }


    @Override
    public void readClient(CompoundTag tag) {
        mechanicalReadClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        mechanicalReadServer(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mechanicalLoadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        mechanicalSaveAdditional(tag, registries);
    }
}

