package ProjectSteam.api;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MechanicalPartBlockEntityBase extends BlockEntity implements IMechanicalBlock, INetworkTagReceiver, ITorqueConsumer, ITorqueProducer {
    public MechanicalPartBlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // a client can choose what part he will track as master so no the entire structure has to be
    // loaded on the client side. only the server must have the entire network loaded
    // clients periodically send if they track a block as master and the server will update them
    // with this blocks internal velocity
    public Map<UUID, Integer> clientsTrackingThisAsMaster = new HashMap<>();
    public int cttam_timeout = 100;
    public int lastPing = 999999;


    public Map<Direction, IMechanicalBlock> connectedParts = new HashMap<>();

    boolean hasReceivedUpdate;

    public double currentRotation;
    public double rotationReceivedDuringLastPropagation;

    public double internalVelocity;
    public double last_internalVelocity;

    public double myMass = 1;
    public double myForce = 0;
    public double myFriction = 0;
    public double getTorqueConsumed(){return myFriction;}
    public double getTorqueProduced(){return myForce;}
    /*
    public double getTorqueProduced(){
    return myForce * (1-velocity/maxvelocity);
    }
     */


    @Override
    public void onLoad() {
        if (level.isClientSide) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("client_onload", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, tag));
        }
        if (!level.isClientSide) {
            MechanicalData data = new MechanicalData();
            HashSet<BlockPos> w = new HashSet<>();
            connectedParts = getConnectedParts(this, null);
            if (connectedParts != null) {

                getPropagatedData(data, null, w);


                HashSet<BlockPos> worked = new HashSet<>();
                propagateVelocityUpdate(data.combinedTransformedMomentum / data.combinedTransformedMass, null, worked);

                System.out.println("target velocity:" + internalVelocity);
                System.out.println("");
            }
        }
    }


    public void tick() {
        if (level.isClientSide()) {
            if (!hasReceivedUpdate) {

                propagateTick();

                HashSet<BlockPos> workedPositions = new HashSet<>();
                propagateRotation(internalVelocity, null, workedPositions);

                lastPing++;
                if (lastPing > cttam_timeout / 2) {
                    lastPing = 0;
                    CompoundTag tag = new CompoundTag();
                    tag.putUUID("ping_is_master", Minecraft.getInstance().player.getUUID());
                    PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, tag));
                }
            }
        }

        if (!hasReceivedUpdate) {
            if (!level.isClientSide()) {

                boolean success = propagateTick();
                // if not success, one or more elements are not in loaded chunks so do not update them
                if (success) {
                    HashSet<BlockPos> workedPositions = new HashSet<>();
                    MechanicalData data = new MechanicalData();
                    getPropagatedData(data, null, workedPositions);
                    workedPositions.clear();

                    data.combinedTransformedMass = Math.max(data.combinedTransformedMass, 0.01);
                    internalVelocity += data.combinedTransformedForce  / data.combinedTransformedMass;
                    internalVelocity -= (data.combinedTransformedResistanceForce *Math.signum(internalVelocity)/ data.combinedTransformedMass);
                    System.out.println(internalVelocity+":"+getBlockPos()+":"+data.combinedTransformedForce+":"+data.combinedTransformedMass+":"+data.combinedTransformedResistanceForce);
                    if (Math.abs(internalVelocity) < 0.0001) internalVelocity = 0;

                    propagateRotation(internalVelocity, null, workedPositions);

                }
            }
        }
        hasReceivedUpdate = false;

        if (!level.isClientSide()) {
            for (UUID i : clientsTrackingThisAsMaster.keySet()) {
                // increment timeout counter
                clientsTrackingThisAsMaster.put(i, clientsTrackingThisAsMaster.get(i) + 1);
                if (clientsTrackingThisAsMaster.get(i) > cttam_timeout) {
                    clientsTrackingThisAsMaster.remove(i);
                    break; // break to prevent concurrent modification bs
                }
            }
            if (last_internalVelocity != internalVelocity) {
                last_internalVelocity = internalVelocity;
                CompoundTag updateTag = new CompoundTag();
                updateTag.putDouble("velocity", internalVelocity);
                for (UUID i : clientsTrackingThisAsMaster.keySet()) {
                    ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(i);
                    PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(this, updateTag));
                }
            }
        }
    }

    @Override
    public boolean propagateTick() {
        if (!hasReceivedUpdate) {
            hasReceivedUpdate = true;
            connectedParts = getConnectedParts(this, null);
            if(connectedParts == null)return false;
            for (IMechanicalBlock i : connectedParts.values()) {
                if(!i.propagateTick())return false;
            }

            // here you can do all the stuff you would do otherwise in the normal tick()
        }
        return true;
    }


    @Override
    public void getPropagatedData(MechanicalData data, @Nullable Direction requestedFrom,HashSet<BlockPos> workedPositions) {
            if(connectedParts == null)return;
            if (!workedPositions.contains(getBlockPos())) {
            workedPositions.add(getBlockPos());

            // update the connected parts
            for (Direction i : connectedParts.keySet()) {
                MechanicalData d = new MechanicalData();
                IMechanicalBlock b = connectedParts.get(i);
                b.getPropagatedData(d, i.getOpposite(), workedPositions);

                double rotationMultiplierToInside = getRotationMultiplierToInside(i);

                data.combinedTransformedForce += d.combinedTransformedForce / rotationMultiplierToInside;
                data.combinedTransformedMass += Math.abs(d.combinedTransformedMass / (rotationMultiplierToInside));
                data.combinedTransformedMomentum += d.combinedTransformedMomentum * Math.signum(rotationMultiplierToInside);
                data.combinedTransformedResistanceForce += Math.abs(d.combinedTransformedResistanceForce / rotationMultiplierToInside);
            }

            double rotationMultiplierToOutside = getRotationMultiplierToOutside(requestedFrom);

            if (this instanceof ITorqueProducer fp)
                data.combinedTransformedForce += fp.getTorqueProduced() / rotationMultiplierToOutside;
            if (this instanceof ITorqueConsumer fc)
                data.combinedTransformedResistanceForce += Math.abs(fc.getTorqueConsumed() / rotationMultiplierToOutside);

            double scaledMass = myMass / (rotationMultiplierToOutside);
            data.combinedTransformedMass += Math.abs(scaledMass);

            double myMomentum = internalVelocity * myMass;
            data.combinedTransformedMomentum += myMomentum * Math.signum(rotationMultiplierToOutside);

        }
    }

    @Override
    public void propagateRotation(double rotation, @Nullable Direction receivingFace, HashSet<BlockPos> workedPositions) {
            if(connectedParts == null)return;
            if (!level.isClientSide && workedPositions.contains(getBlockPos()) && Math.abs(rotation  * getRotationMultiplierToInside(receivingFace) - rotationReceivedDuringLastPropagation) > 0.00001) {
                // break this block because something is wrong with the network
                System.out.println(getBlockPos());
                level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            }
        if (!workedPositions.contains(getBlockPos())) {
            workedPositions.add(getBlockPos());

            rotationReceivedDuringLastPropagation = rotation  * getRotationMultiplierToInside(receivingFace);
            internalVelocity = rotationReceivedDuringLastPropagation;

            this.currentRotation += rotationReceivedDuringLastPropagation;
            if(this.currentRotation > 360)this.currentRotation-=360;
            if(this.currentRotation < 0)this.currentRotation+=360;

            // forward the transformed rotation to the other blocks
            for (Direction i : connectedParts.keySet()) {
                IMechanicalBlock b = connectedParts.get(i);
                b.propagateRotation(rotationReceivedDuringLastPropagation * getRotationMultiplierToOutside(i), i.getOpposite(), workedPositions);
            }
        }
    }

    @Override
    public void propagateVelocityUpdate(double velocity, @Nullable Direction receivingFace, HashSet<BlockPos> workedPositions) {
            if(connectedParts == null)return;
        if (!workedPositions.contains(getBlockPos())) {
            workedPositions.add(getBlockPos());


            System.out.println(getBlockPos()+":"+velocity);

            internalVelocity = velocity * getRotationMultiplierToInside(receivingFace);

            // forward the transformed rotation to the other blocks
            for (Direction i : connectedParts.keySet()) {
                IMechanicalBlock b = connectedParts.get(i);
                b.propagateVelocityUpdate(internalVelocity * getRotationMultiplierToOutside(i), i.getOpposite(), workedPositions);
            }
        }
    }

    @Override
    public void readClient(CompoundTag tag) {
        //System.out.println(tag);
        if (tag.contains("velocity"))
            internalVelocity = tag.getDouble("velocity");
    }

    @Override
    public void readServer(CompoundTag tag) {
        if (tag.contains("client_onload")) {

        }

        if (tag.contains("ping_is_master")) {
            UUID from = tag.getUUID("ping_is_master");
            clientsTrackingThisAsMaster.put(from, 0);

            CompoundTag updateTag = new CompoundTag();
            updateTag.putDouble("velocity", internalVelocity);
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(this, updateTag));
        }
    }
}
