package ProjectSteam.api;

import ARLib.network.PacketBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;

public interface IMechanicalBlock {

    double getMass();

    double getTorqueResistance();

    double getTorqueProduced();

    MechanicalBlockData getMechanicalData();

    /**
     * to check if the block south to me (z+1) is connected to me i will ask him connectsAtFace(NORTH)
     **/
    boolean connectsAtFace(Direction face, @Nullable BlockState myState);


    default double getRotationMultiplierToInside(@Nullable Direction receivingFace) {
        return 1;
    }

    default double getRotationMultiplierToOutside(@Nullable Direction outputFace) {
        return 1/getRotationMultiplierToInside(outputFace);
    }


    default void onPropagatedTickEnd() {
        // whatever you need to do
    }

    /**
     * called at the start of the tick update
     */
    default void propagateTickBeforeUpdate() {
        MechanicalBlockData myData = getMechanicalData();
        if (!myData.hasReceivedUpdate) {
            myData.hasReceivedUpdate = true;
            myData.connectedParts = getConnectedParts(myData.me, null);

            for (IMechanicalBlock i : myData.connectedParts.values()) {
                i.propagateTickBeforeUpdate();
            }

            onPropagatedTickEnd();
        }
    }


    default void getPropagatedData(MechanicalFlowData data, @org.jetbrains.annotations.Nullable Direction requestedFrom, HashSet<BlockPos> workedPositions) {
        MechanicalBlockData myData = getMechanicalData();
        BlockEntity myTile = myData.me;
        if (!workedPositions.contains(myTile.getBlockPos())) {
            workedPositions.add(myTile.getBlockPos());

            MechanicalFlowData myInputFlowData = new MechanicalFlowData();
            // update the connected parts
            for (Direction i : myData.connectedParts.keySet()) {
                MechanicalFlowData d = new MechanicalFlowData();
                IMechanicalBlock b = myData.connectedParts.get(i);
                b.getPropagatedData(d, i.getOpposite(), workedPositions);

                double rotationMultiplierToInside = getRotationMultiplierToInside(i);

                myInputFlowData.combinedTransformedForce += d.combinedTransformedForce / rotationMultiplierToInside;
                myInputFlowData.combinedTransformedMass += Math.abs(d.combinedTransformedMass / (rotationMultiplierToInside));
                myInputFlowData.combinedTransformedMomentum += d.combinedTransformedMomentum * Math.signum(rotationMultiplierToInside);
                myInputFlowData.combinedTransformedResistanceForce += Math.abs(d.combinedTransformedResistanceForce / rotationMultiplierToInside);
            }

            double rotationMultiplierToOutside = getRotationMultiplierToOutside(requestedFrom);

            myInputFlowData.combinedTransformedForce +=getTorqueProduced();
            myInputFlowData.combinedTransformedMass += getMass();
            myInputFlowData.combinedTransformedMomentum += myData.internalVelocity * getMass();
            myInputFlowData.combinedTransformedResistanceForce +=getTorqueResistance();


            data.combinedTransformedForce += myInputFlowData.combinedTransformedForce / rotationMultiplierToOutside;
            data.combinedTransformedResistanceForce += Math.abs( myInputFlowData.combinedTransformedResistanceForce / rotationMultiplierToOutside);
            data.combinedTransformedMass += Math.abs(myInputFlowData.combinedTransformedMass / rotationMultiplierToOutside);
            data.combinedTransformedMomentum += myInputFlowData.combinedTransformedMomentum * Math.signum(rotationMultiplierToOutside);

        }
    }

    default void applyRotations(HashSet<BlockPos> workedPositions) {
        MechanicalBlockData myData = getMechanicalData();
        BlockEntity myTile = myData.me;
        Level level = myTile.getLevel();
        if (myData.connectedParts == null) return;

        if (!workedPositions.contains(myTile.getBlockPos())) {
            workedPositions.add(myTile.getBlockPos());

            myData.currentRotation += myData.internalVelocity;
            if (myData.currentRotation > 360) myData.currentRotation -= 360;
            if (myData.currentRotation < 0) myData.currentRotation += 360;

            // forward the transformed rotation to the other blocks
            for (Direction i : myData.connectedParts.keySet()) {
                IMechanicalBlock b = myData.connectedParts.get(i);
                b.applyRotations(workedPositions);
            }
        }
    }

    default void propagateVelocityUpdate(double velocity, @org.jetbrains.annotations.Nullable Direction receivingFace, HashSet<BlockPos> workedPositions) {
        MechanicalBlockData myData = getMechanicalData();
        BlockEntity myTile = myData.me;
        Level level = myTile.getLevel();
        if (!level.isClientSide && workedPositions.contains(myTile.getBlockPos()) && Math.abs(velocity * getRotationMultiplierToInside(receivingFace) - myData.internalVelocity) > 0.00001) {
            // break this block because something is wrong with the network
            System.out.println("breaking the network because something is wrong: this tile received a different velocity update in the same tick:" + myTile.getBlockPos());
            System.out.println("current reveiced rotation from face "+receivingFace+":"+velocity * getRotationMultiplierToInside(receivingFace)+". Last received velocity: "+myData.internalVelocity);

            BlockPos pos = myTile.getBlockPos();
            ItemEntity m = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(level.getBlockState(pos).getBlock(), 1));
            // TODO spawn the entity

            level.setBlock(myTile.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);

        }

        if (!workedPositions.contains(myTile.getBlockPos())) {
            workedPositions.add(myTile.getBlockPos());

            myData.internalVelocity = velocity;
            if (receivingFace != null)
                myData.internalVelocity *= getRotationMultiplierToInside(receivingFace);

            // forward the transformed rotation to the other blocks
            for (Direction i : myData.connectedParts.keySet()) {
                IMechanicalBlock b = myData.connectedParts.get(i);
                b.propagateVelocityUpdate(myData.internalVelocity * getRotationMultiplierToOutside(i), i.getOpposite(), workedPositions);
            }
        }
    }


    default Map<Direction, IMechanicalBlock> getConnectedParts(BlockEntity mechanicalBlockBE, @Nullable BlockState myBlockState) {

        Map<Direction, IMechanicalBlock> connectedBlocks = new HashMap<>();

        if (myBlockState == null)
            myBlockState = mechanicalBlockBE.getLevel().getBlockState(mechanicalBlockBE.getBlockPos());

        for (Direction i : Direction.values()) {
            if (((IMechanicalBlock) mechanicalBlockBE).connectsAtFace(i, myBlockState)) {

                BlockEntity other = mechanicalBlockBE.getLevel().getBlockEntity(mechanicalBlockBE.getBlockPos().relative(i));
                if (other instanceof IMechanicalBlock othermechBlock && othermechBlock.connectsAtFace(i.getOpposite(), null)) {
                    connectedBlocks.put(i, othermechBlock);
                }
            }
        }
        return connectedBlocks;
    }


    default void mechanicalOnload() {
        if (getMechanicalData().me.getLevel().isClientSide()) {

        }
        if (!getMechanicalData().me.getLevel().isClientSide()) {
            MechanicalFlowData data = new MechanicalFlowData();
            HashSet<BlockPos> w = new HashSet<>();
            getMechanicalData().connectedParts = getConnectedParts(getMechanicalData().me, null);

            getPropagatedData(data, null, w);

            HashSet<BlockPos> worked = new HashSet<>();
            propagateVelocityUpdate(data.combinedTransformedMomentum / data.combinedTransformedMass, null, worked);

            System.out.println("target velocity:" + getMechanicalData().internalVelocity);
        }
    }


    default void mechanicalTick() {
        MechanicalBlockData myData = getMechanicalData();
        BlockEntity myTile = myData.me;
        if (myTile.getLevel().isClientSide()) {
            if (!myData.hasReceivedUpdate) {
                propagateTickBeforeUpdate();
                HashSet<BlockPos> workedPositions = new HashSet<>();
                propagateVelocityUpdate(myData.internalVelocity, null, workedPositions);
                workedPositions.clear();
                applyRotations(workedPositions);

                myData.lastPing++;
                if (myData.lastPing > myData.cttam_timeout / 2) {
                    myData.lastPing = 0;
                    CompoundTag tag = new CompoundTag();
                    tag.putUUID("ping_is_master", Minecraft.getInstance().player.getUUID());
                    PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(myTile, tag));
                }
            }
        }

        if (!myData.hasReceivedUpdate) {
            if (!myTile.getLevel().isClientSide()) {

                propagateTickBeforeUpdate();

                HashSet<BlockPos> workedPositions = new HashSet<>();
                MechanicalFlowData data = new MechanicalFlowData();
                getPropagatedData(data, null, workedPositions);
                workedPositions.clear();

                data.combinedTransformedMass = Math.max(data.combinedTransformedMass, 0.01);
                myData.internalVelocity += data.combinedTransformedForce / data.combinedTransformedMass;
                myData.internalVelocity -= (data.combinedTransformedResistanceForce * Math.signum(myData.internalVelocity) / data.combinedTransformedMass);
                //System.out.println(myData.internalVelocity + ":" + myTile.getBlockPos() + ":" + data.combinedTransformedForce + ":" + data.combinedTransformedMass + ":" + data.combinedTransformedResistanceForce);
                if (Math.abs(myData.internalVelocity) < 0.0001) myData.internalVelocity = 0;

                propagateVelocityUpdate(myData.internalVelocity, null, workedPositions);
                workedPositions.clear();
                applyRotations(workedPositions);

            }
        }
        myData.hasReceivedUpdate = false;

        if (!myTile.getLevel().isClientSide()) {
            for (UUID i : myData.clientsTrackingThisAsMaster.keySet()) {
                // increment timeout counter
                myData.clientsTrackingThisAsMaster.put(i, myData.clientsTrackingThisAsMaster.get(i) + 1);
                if (myData.clientsTrackingThisAsMaster.get(i) > myData.cttam_timeout) {
                    myData.clientsTrackingThisAsMaster.remove(i);
                    break; // break to prevent concurrent modification bs
                }
            }
            if (myData.last_internalVelocity != myData.internalVelocity) {
                myData.last_internalVelocity = myData.internalVelocity;
                myData.me.setChanged();
                CompoundTag updateTag = new CompoundTag();
                updateTag.putDouble("velocity", myData.internalVelocity);
                for (UUID i : myData.clientsTrackingThisAsMaster.keySet()) {
                    ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(i);
                    PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(myTile, updateTag));
                }
            }
        }
    }

    default void mechanicalReadServer(CompoundTag tag) {
        if (tag.contains("ping_is_master")) {
            UUID from = tag.getUUID("ping_is_master");
            getMechanicalData().clientsTrackingThisAsMaster.put(from, 0);

            CompoundTag updateTag = new CompoundTag();
            updateTag.putDouble("velocity", getMechanicalData().internalVelocity);
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(getMechanicalData().me, updateTag));
        }
    }

    default void mechanicalReadClient(CompoundTag tag) {
        if (tag.contains("velocity"))
            getMechanicalData().internalVelocity = tag.getDouble("velocity");
    }


    default void mechanicalLoadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        getMechanicalData().internalVelocity = tag.getDouble("internalVelocity");
    }


    default void mechanicalSaveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
         tag.putDouble("internalVelocity", getMechanicalData().internalVelocity);
    }
}


