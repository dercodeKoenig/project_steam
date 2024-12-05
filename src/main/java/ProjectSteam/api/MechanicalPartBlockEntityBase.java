package ProjectSteam.api;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import com.ibm.icu.impl.Pair;
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

import static ProjectSteam.utils.calculateWeightedAverage;

public abstract class MechanicalPartBlockEntityBase extends BlockEntity implements IMechanicalBlock, INetworkTagReceiver {
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
    public double rotationAlreadyReceived;

    public double internalVelocity;
    public double last_internalVelocity;

    public double myMass = 1;
    public double myFriction = 0.1;
    public double myForce = 0.0;




    @Override
    public void onLoad() {
        if (level.isClientSide) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("client_onload", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, tag));
        }
        if(!level.isClientSide){
            List<Pair<Double, Double>> momentums = new ArrayList<>();
            HashSet<BlockPos> w = new HashSet<>();
            boolean success = gatherWeightedMomentums(momentums,null,w);
if(success) {
    MechanicalData data = new MechanicalData();
    w.clear();
    getPropagatedData(data, null, w);

    double myTargetMomentum = 0;
    for(Pair<Double, Double > i : momentums){
        myTargetMomentum+=i.first*i.second;
    }

    // little workaround bc i have only implemented to propagate rotation
    // but when a rotation is received they all should update their velocity
    HashSet<BlockPos> worked = new HashSet<>();
    propagateRotation(-myTargetMomentum / data.combinedMass, null, worked);
    worked.clear();
    propagateRotation(myTargetMomentum / data.combinedMass, null, worked);

    System.out.println("target velocity:" + internalVelocity);
}
        }
    }


    public void tick() {

        if (level.getGameTime() % 100 == 0) {
            if (level.isClientSide()) {

            } else {
                for (UUID i : clientsTrackingThisAsMaster.keySet()) {
                    System.out.println(getBlockPos() + " is tracked by " + i + " as master");
                }
            }
        }


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

                if (level.getGameTime() % 100 == 0) {
                    System.out.println(getBlockPos() + ": is server master");
                }

                boolean success = propagateTick();

                HashSet<BlockPos> workedPositions = new HashSet<>();
                if (success) {
                    MechanicalData data = new MechanicalData();
                    getPropagatedData(data, null, workedPositions);
                    workedPositions.clear();

                    data.combinedMass = Math.max(data.combinedMass, 0.01);
                    internalVelocity += data.combinedForce / data.combinedMass;
                    if (internalVelocity < 0.0001) internalVelocity = 0;

                } else internalVelocity = 0;

                propagateRotation(internalVelocity, null, workedPositions);
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
                i.propagateTick();
            }
        }
        return true;
    }


    @Override
    public void getPropagatedData(MechanicalData data, @Nullable Direction requestedFrom,HashSet<BlockPos> workedPositions) {
        if (!workedPositions.contains(getBlockPos())) {
            workedPositions.add(getBlockPos());

            // update the connected parts
            for (Direction i : connectedParts.keySet()) {
                MechanicalData d = new MechanicalData();
                IMechanicalBlock b = connectedParts.get(i);
                b.getPropagatedData(d, i.getOpposite(), workedPositions);

                double rotationMultiplierToInside = getRotationMultiplierToInside(i);

                data.combinedForce += d.combinedForce / rotationMultiplierToInside;
                data.combinedMass += d.combinedMass / (rotationMultiplierToInside);
            }

            double rotationMultiplierToOutside = getRotationMultiplierToOutside(requestedFrom);

            double actualForce = (-internalVelocity * myFriction + myForce) / rotationMultiplierToOutside;
            double scaledMass = myMass / (rotationMultiplierToOutside);

            data.combinedForce += actualForce;
            data.combinedMass += scaledMass;

        }
    }

    @Override
    public boolean propagateRotation(double rotation, @Nullable Direction receivingFace, HashSet<BlockPos> workedPositions) {
        if (workedPositions.contains(getBlockPos()) && Math.abs(rotation - rotationAlreadyReceived) > 0.00001) {
            // break this block because something is wrong with the network
            level.setBlock(getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            //return false;
        }
        if (!workedPositions.contains(getBlockPos())) {
            workedPositions.add(getBlockPos());

            rotationAlreadyReceived = rotation;
            internalVelocity = rotation * getRotationMultiplierToInside(receivingFace);

            this.currentRotation += rotation;
            if(this.currentRotation > 360)this.currentRotation-=360;
            if(this.currentRotation < 0)this.currentRotation=0;

            // forward the transformed rotation to the other blocks
            for (Direction i : connectedParts.keySet()) {
                IMechanicalBlock b = connectedParts.get(i);
                if(!b.propagateRotation(rotation * getRotationMultiplierToOutside(i), i.getOpposite(), workedPositions)){
                    return false;
                }
            }
        }
        return true;
    }


    @Override
    public boolean gatherWeightedMomentums(List<Pair<Double, Double>> momentums, @Nullable Direction requestedFrom, HashSet<BlockPos> workedPositions) {
        if (!workedPositions.contains(getBlockPos())) {
            workedPositions.add(getBlockPos());

            connectedParts = getConnectedParts(this, null);
            if (connectedParts == null) return false;

            List<Pair<Double, Double>> momentums2 = new ArrayList<>();

            // forward the transformed rotation to the other blocks
            for (Direction i : connectedParts.keySet()) {
                IMechanicalBlock b = connectedParts.get(i);
                double rotationMultiplierToInside = getRotationMultiplierToOutside(i);
                List<Pair<Double, Double>> momentums3 = new ArrayList<>();
                if (!b.gatherWeightedMomentums(momentums3, i.getOpposite(), workedPositions)) {
                    return false;
                }
                for (Pair<Double, Double> o : momentums3) {
                    momentums2.add(Pair.of(o.first / rotationMultiplierToInside, o.second / rotationMultiplierToInside));
                }
            }

            double rotationMultiplierToOutside = getRotationMultiplierToOutside(requestedFrom);

            for (Pair<Double, Double> o : momentums2) {
                momentums.add(Pair.of(o.first / rotationMultiplierToOutside, o.second / rotationMultiplierToOutside));
            }

            double myMomentum = internalVelocity * myMass / rotationMultiplierToOutside;
            Pair<Double, Double> scaledMomentumWithWeight = Pair.of(myMomentum, 1 / rotationMultiplierToOutside);
            momentums.add(scaledMomentumWithWeight);

        }
        return true;
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
