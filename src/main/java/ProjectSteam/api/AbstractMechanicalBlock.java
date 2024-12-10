package ProjectSteam.api;

import ARLib.network.PacketBlockEntity;
import com.ibm.icu.impl.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;

public abstract class AbstractMechanicalBlock {

    public static int tps = 20;

    public int id;

    public IMechanicalBlockProvider me;

    public Map<UUID, Integer> clientsTrackingThisAsMaster = new HashMap<>();
    public int cttam_timeout = 100;
    public int lastPing = 999999;


    public Map<Direction, AbstractMechanicalBlock> connectedParts = new HashMap<>();

    public boolean hasReceivedUpdate;

    public double currentRotation;

    public double internalVelocity;
    public double last_internalVelocity;

    public double stress = 0;
    public double lastConsumedForce = 0;
    public double lastConsumedForce_filled1 = 0; // for positive and negative force.
    public double lastConsumedForce_filled2 = 0; // is probably not 100% physically correct but i need to keep it simple and can not do full simulation for performance reasons
    public double lastAddedForce = 0;
    public Deque<nodeInfo> forceDistributionDeq = new ArrayDeque<>();


    public AbstractMechanicalBlock(int id, IMechanicalBlockProvider me) {
        this.id=id;this.me = me;
    }

    public abstract double getMaxStress();

    public abstract double getMass(Direction face, @Nullable BlockState myBlockState);

    public abstract double getTorqueResistance(Direction face, @Nullable BlockState myBlockState);

    public abstract double getTorqueProduced(Direction face, @Nullable BlockState myBlockState);
    /**
     * to check if the block south to me (z+1) is connected to me i will ask him connectsAtFace(NORTH)
     **/
    
    public abstract double getRotationMultiplierToInside(@Nullable Direction receivingFace, @Nullable BlockState myState);

    public double getRotationMultiplierToOutside(@Nullable Direction outputFace, @Nullable BlockState myState) {
        return 1/getRotationMultiplierToInside(outputFace, myState);
    }


    public abstract void onPropagatedTickEnd() ;

    /**
     * called at the start of the tick update
     */
    public void propagateTickBeforeUpdate() {
        
        if (!hasReceivedUpdate) {
            hasReceivedUpdate = true;
            connectedParts = me.getConnectedParts(me, this);

            for (AbstractMechanicalBlock i : connectedParts.values()) {
                i.propagateTickBeforeUpdate();
            }

            onPropagatedTickEnd();
        }
    }


    public void getPropagatedData(MechanicalFlowData data, @org.jetbrains.annotations.Nullable Direction requestedFrom, HashSet<AbstractMechanicalBlock> workedPositions) {
        
        BlockEntity myTile = me.getBlockEntity();
        if (!workedPositions.contains(this)) {
            workedPositions.add(this);

            BlockState myState = myTile.getLevel().getBlockState(myTile.getBlockPos());

            MechanicalFlowData myInputFlowData = new MechanicalFlowData();
            // update the connected parts
            for (Direction i : connectedParts.keySet()) {
                MechanicalFlowData d = new MechanicalFlowData();
                AbstractMechanicalBlock b = connectedParts.get(i);
                b.getPropagatedData(d, i.getOpposite(), workedPositions);

                double rotationMultiplierToInside = getRotationMultiplierToInside(i, myState);

                myInputFlowData.combinedTransformedForce += d.combinedTransformedForce / rotationMultiplierToInside;
                myInputFlowData.combinedTransformedMass += Math.abs(d.combinedTransformedMass / (rotationMultiplierToInside));
                myInputFlowData.combinedTransformedMomentum += d.combinedTransformedMomentum * Math.signum(rotationMultiplierToInside);
                myInputFlowData.combinedTransformedResistanceForce += Math.abs(d.combinedTransformedResistanceForce / rotationMultiplierToInside);
            }

            double rotationMultiplierToOutside = getRotationMultiplierToOutside(requestedFrom, myState);

            myInputFlowData.combinedTransformedForce +=getTorqueProduced(requestedFrom, myState);
            myInputFlowData.combinedTransformedMass += getMass(requestedFrom, myState);
            myInputFlowData.combinedTransformedMomentum += internalVelocity * getMass(requestedFrom, myState);
            myInputFlowData.combinedTransformedResistanceForce +=getTorqueResistance(requestedFrom, myState);


            data.combinedTransformedForce += myInputFlowData.combinedTransformedForce / rotationMultiplierToOutside;
            data.combinedTransformedResistanceForce += Math.abs( myInputFlowData.combinedTransformedResistanceForce / rotationMultiplierToOutside);
            data.combinedTransformedMass += Math.abs(myInputFlowData.combinedTransformedMass / rotationMultiplierToOutside);
            data.combinedTransformedMomentum += myInputFlowData.combinedTransformedMomentum * Math.signum(rotationMultiplierToOutside);

        }
    }

    public void applyRotations() {
        currentRotation += internalVelocity;
        double eqs = 2 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9;
        if (currentRotation > 360 * eqs) currentRotation -= 360 * eqs;
        if (currentRotation < -360 * eqs) currentRotation += 360 * eqs;
    }

    public void propagateVelocityUpdate(double velocity, @org.jetbrains.annotations.Nullable Direction receivingFace, HashSet<AbstractMechanicalBlock> workedPositions, boolean ignorePreviousUpdates, boolean resetStress) {
        BlockEntity myTile = me.getBlockEntity();
        Level level = myTile.getLevel();
        BlockState myState = level.getBlockState(myTile.getBlockPos());
        if (!ignorePreviousUpdates && !level.isClientSide && workedPositions.contains(Pair.of(myTile.getBlockPos(), id)) && Math.abs(velocity * getRotationMultiplierToInside(receivingFace, myState) - internalVelocity) > 0.00001) {
            // break this block because something is wrong with the network
            System.out.println("breaking the network because something is wrong: this tile received a different velocity update in the same tick:" + myTile.getBlockPos()+", id: "+id);
            System.out.println("current reveiced rotation from face "+receivingFace+":"+velocity * getRotationMultiplierToInside(receivingFace, myState)+". Last received velocity: "+internalVelocity);

           level.destroyBlock(myTile.getBlockPos(),true);
            return;
        }

        if (!workedPositions.contains(this)) {
            workedPositions.add(this);

            double lastVelocity = internalVelocity;

            double currentProducedForceBeforeVelocityChange = getTorqueProduced(receivingFace, myState);
            double currentResistanceBeforeVelocityChange = getTorqueResistance(receivingFace, myState);
            double currentMassBeforeVelocityChange = getMass(receivingFace, myState);

            internalVelocity = velocity;
            if (receivingFace != null) {
                internalVelocity *= getRotationMultiplierToInside(receivingFace, myState);
            }

            // forward the transformed rotation to the other blocks
            for (Direction i : connectedParts.keySet()) {
                AbstractMechanicalBlock b = connectedParts.get(i);
                double outputVelocity = internalVelocity * getRotationMultiplierToOutside(i, myState);
                b.propagateVelocityUpdate(outputVelocity, i.getOpposite(), workedPositions, ignorePreviousUpdates, resetStress);
            }


            // all for stress calculation later
            if ( resetStress && !me.getBlockEntity().getLevel().isClientSide()) {
                forceDistributionDeq.clear();
                stress = 0;
                lastConsumedForce_filled1 = 0;
                lastConsumedForce_filled2 = 0;
                double acceleration = (internalVelocity - lastVelocity) * tps;
                double requiredForce1 = currentMassBeforeVelocityChange * acceleration * Math.signum(lastVelocity);
                double requiredForce2 = currentResistanceBeforeVelocityChange * Math.abs(Math.signum(lastVelocity)) + requiredForce1;
                double absResistance = Math.max(requiredForce2, 0);
                double inducedForce = Math.max(-requiredForce2, 0) * Math.signum(lastVelocity);

                double totalForceWorking = inducedForce + currentProducedForceBeforeVelocityChange;
                if (Math.abs(totalForceWorking) >= absResistance) {
                    totalForceWorking -= absResistance * Math.signum(totalForceWorking);
                    absResistance = 0;
                }
                if (Math.abs(totalForceWorking) < 0.01)
                    //ignore
                    totalForceWorking = 0;

                lastConsumedForce = absResistance;
                lastAddedForce = totalForceWorking;
                //System.out.println(me.getBlockEntity().getBlockState().getBlock() + ":" + me.getBlockEntity().getBlockPos() + ":" + lastAddedForce + ":" + lastConsumedForce);
            }
        }
    }

    public void aggregateConnectedParts(Set<AbstractMechanicalBlock> parts){
        if(!parts.contains(this)){
            parts.add(this);
            for (Direction i : connectedParts.keySet()) {
                AbstractMechanicalBlock b = connectedParts.get(i);
                b.aggregateConnectedParts(parts);
            }
        }
    }



    public void mechanicalOnload() {
        if (me.getBlockEntity().getLevel().isClientSide()) {

        }
        if (!me.getBlockEntity().getLevel().isClientSide()) {
            MechanicalFlowData data = new MechanicalFlowData();
            connectedParts =me. getConnectedParts(me, this);
            HashSet<AbstractMechanicalBlock> worked = new HashSet<>();
            getPropagatedData(data, null, worked);
            worked.clear();

            double target_velocity = 0;
            if(data.combinedTransformedMass != 0){
                target_velocity = data.combinedTransformedMomentum / data.combinedTransformedMass;
            }
            System.out.println("target velocity:" +target_velocity);
            propagateVelocityUpdate(target_velocity,  null, worked, true, false);

        }
    }

    public class nodeInfo{
        public Direction nextInputFace;
        public AbstractMechanicalBlock nextTarget;
        public forceDistributionNode node;
    }

    public class forceDistributionNode{
        public AbstractMechanicalBlock daddy;
        public Set<AbstractMechanicalBlock> path = new LinkedHashSet<>();
        public List<Pair<AbstractMechanicalBlock, Double>> pathWithForceTransformations = new ArrayList<>();
        public double lastOutputForceMultiplier = 1;
        public double currentEffectiveForceMultiplier = 1;
        public forceDistributionNode(AbstractMechanicalBlock me){
            daddy = me;
        }
        public forceDistributionNode copy(){
            forceDistributionNode n = new forceDistributionNode(daddy);
            n.path.addAll(path);
            n.lastOutputForceMultiplier = lastOutputForceMultiplier;
            n.currentEffectiveForceMultiplier = currentEffectiveForceMultiplier;
            n.pathWithForceTransformations.addAll(pathWithForceTransformations);
            return n;
        }
    }

    void addStressBackwards(forceDistributionNode n, double stress){
        for (int i = n.pathWithForceTransformations.size()-1; i >=0 ; i--) {
            n.pathWithForceTransformations.get(i).first.stress += Math.abs(stress);
            stress /= Math.abs(n.pathWithForceTransformations.get(i).second);
            if(n.pathWithForceTransformations.get(i).first.stress > n.pathWithForceTransformations.get(i).first.getMaxStress()){
                n.pathWithForceTransformations.get(i).first.me.getBlockEntity().getLevel().destroyBlock(n.pathWithForceTransformations.get(i).first.me.getBlockEntity().getBlockPos(),true);
            }
        }
    }
    public void walkDistributeForce(Direction receivingFace, forceDistributionNode n) {
        if (!n.path.contains(this)) {
            forceDistributionNode myNode = n.copy();
            myNode.path.add(this);
            double forceMultiplierForNode = 1 * myNode.lastOutputForceMultiplier;
            if (receivingFace != null) {
                forceMultiplierForNode *= 1 / getRotationMultiplierToInside(receivingFace, null);
            }
            myNode.pathWithForceTransformations.add(Pair.of(this, forceMultiplierForNode));

            myNode.currentEffectiveForceMultiplier *= forceMultiplierForNode;

            double currentEffectiveForce = myNode.currentEffectiveForceMultiplier * myNode.daddy.lastAddedForce;

            if (currentEffectiveForce > 0) {
                double toSubtract = lastConsumedForce - lastConsumedForce_filled1;
                toSubtract = Math.min(toSubtract, currentEffectiveForce);
                myNode.daddy.lastAddedForce -= toSubtract / myNode.currentEffectiveForceMultiplier;
                lastConsumedForce_filled1 += toSubtract;
                addStressBackwards(myNode, toSubtract);
            }
            if (currentEffectiveForce < 0) {
                double toSubtract = lastConsumedForce - lastConsumedForce_filled2;
                toSubtract = Math.min(toSubtract, -currentEffectiveForce);
                myNode.daddy.lastAddedForce += toSubtract / myNode.currentEffectiveForceMultiplier;
                lastConsumedForce_filled2 += toSubtract;
                addStressBackwards(myNode, toSubtract);
            }
            if (lastAddedForce != 0) {
                if (Math.signum(lastAddedForce) != Math.signum(currentEffectiveForce)) {
                    double toSubtract;
                    toSubtract = Math.min(Math.abs(lastAddedForce), Math.abs(currentEffectiveForce));
                    myNode.daddy.lastAddedForce -= toSubtract * Math.signum(currentEffectiveForce) / myNode.currentEffectiveForceMultiplier;
                    addStressBackwards(myNode, toSubtract);
                }
            }
            if (Math.abs(currentEffectiveForce) > 0.01) {
                for (Direction i : connectedParts.keySet()) {
                    forceDistributionNode newNode = myNode.copy();
                    newNode.lastOutputForceMultiplier = 1 / getRotationMultiplierToOutside(i, null);
                    nodeInfo info = new nodeInfo();
                    info.nextTarget = connectedParts.get(i);
                    info.node = newNode;
                    info.nextInputFace = i.getOpposite();
                    forceDistributionDeq.addLast(info);
                }
            }else{
                myNode.daddy.lastAddedForce = 0;
            }
        }
    }

    public void mechanicalTick() {
        
        BlockEntity myTile = me.getBlockEntity();
        if (myTile.getLevel().isClientSide()) {
            if (!hasReceivedUpdate) {
                propagateTickBeforeUpdate();
                HashSet<AbstractMechanicalBlock> workedPositions = new HashSet<>();
                propagateVelocityUpdate(internalVelocity,  null, workedPositions, false, false);

                lastPing++;
                if (lastPing > cttam_timeout / 2) {
                    lastPing = 0;
                    CompoundTag tag = new CompoundTag();
                    tag.putUUID("ping_is_master", Minecraft.getInstance().player.getUUID());
                    tag.putInt("id", id);
                    PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(myTile, tag));
                }
            }
        }

        if (!hasReceivedUpdate) {
            if (!myTile.getLevel().isClientSide()) {

                propagateTickBeforeUpdate();

                HashSet<AbstractMechanicalBlock> workedPositions = new HashSet<>();
                MechanicalFlowData data = new MechanicalFlowData();
                getPropagatedData(data, null, workedPositions);
                workedPositions.clear();

                double t = (double) 1 /tps;

                data.combinedTransformedMass = Math.max(data.combinedTransformedMass, 0.01);
                double newVelocity = internalVelocity;
                newVelocity += data.combinedTransformedForce / data.combinedTransformedMass * t;
                float signBefore = (float) Math.signum(newVelocity);
                newVelocity -= data.combinedTransformedResistanceForce * Math.signum(newVelocity) / data.combinedTransformedMass * t;
                float signAfter = (float) Math.signum(newVelocity);
                if (Math.abs(newVelocity) < 0.0001) newVelocity = 0;

                if ((signAfter < 0 && signBefore > 0) || (signAfter > 0 && signBefore < 0))
                    newVelocity = 0;
                if(newVelocity > internalVelocity+90)
                    newVelocity = internalVelocity+90;
                if(newVelocity < internalVelocity-90)
                    newVelocity = internalVelocity-90;

                //System.out.println(newVelocity + ":" + myTile.getBlockPos() + ":" + data.combinedTransformedForce + ":" + data.combinedTransformedMass + ":" + data.combinedTransformedResistanceForce);


                boolean resetStress = me.getBlockEntity().getLevel().random.nextInt(20*120) == 0;

                propagateVelocityUpdate(newVelocity, null, workedPositions, false, resetStress);

                double t1 = System.nanoTime();

                Set <AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                aggregateConnectedParts(connectedBlocks);

                if(resetStress) {
                    for (AbstractMechanicalBlock i : connectedBlocks) {
                        if (i.lastAddedForce != 0) {
                            forceDistributionNode n = new forceDistributionNode(i);
                            nodeInfo info = new nodeInfo();
                            info.nextInputFace = null;
                            info.nextTarget = i;
                            info.node = n;
                            i.forceDistributionDeq.addLast(info);
                        }
                    }
                }

boolean tickedANode = false;
                    for (AbstractMechanicalBlock i : connectedBlocks) {
                        if (!i.forceDistributionDeq.isEmpty()) {
                            System.out.println("tick a node");
                            tickedANode = true;
                            nodeInfo info = i.forceDistributionDeq.removeFirst();
                            info.nextTarget.walkDistributeForce(info.nextInputFace, info.node);
                        }
                    }

                for(AbstractMechanicalBlock i : connectedBlocks){
                    //System.out.println(i.stress+":"+i.me.getBlockEntity().getBlockState().getBlock());
                }
                //System.out.println(me.getBlockEntity().getBlockPos());
                if(tickedANode) {
                    System.out.println((System.nanoTime() - t1) / 1000 / 1000);
                    System.out.println("");
                }
            }
        }
        hasReceivedUpdate = false;
        applyRotations();

        if (!myTile.getLevel().isClientSide()) {
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
                me.getBlockEntity().setChanged();
                CompoundTag updateTag = new CompoundTag();
                updateTag.putDouble("velocity", internalVelocity);
                updateTag.putInt("id", id);
                for (UUID i : clientsTrackingThisAsMaster.keySet()) {
                    ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(i);
                    PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(myTile, updateTag));
                }
            }
            if(Math.abs(internalVelocity) > 100000 ||Double.isNaN(internalVelocity)){
                System.out.println("set block to air because velocity is way too high!  "+me.getBlockEntity().getBlockPos());
                me.getBlockEntity().getLevel().destroyBlock(me.getBlockEntity().getBlockPos(),true);
            }



            CompoundTag updateTag = new CompoundTag();
            updateTag.putDouble("stress", stress);
            updateTag.putInt("id", id);
            PacketDistributor.sendToAllPlayers(PacketBlockEntity.getBlockEntityPacket(me.getBlockEntity(),updateTag));
        }
    }

    public void mechanicalReadServer(CompoundTag tag) {
        if (tag.contains("ping_is_master") && tag.contains("id")) {
            UUID from = tag.getUUID("ping_is_master");
            int id = tag.getInt("id");
            if (id == this.id) {
                clientsTrackingThisAsMaster.put(from, 0);
                CompoundTag updateTag = new CompoundTag();
                updateTag.putDouble("velocity", internalVelocity);
                updateTag.putInt("id", id);
                ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
                PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(me.getBlockEntity(), updateTag));
            }
        }
    }

    public void mechanicalReadClient(CompoundTag tag) {
        if (tag.contains("velocity") && tag.contains("id"))
            if (tag.getInt("id") == this.id)
                internalVelocity = tag.getDouble("velocity");

        if (tag.contains("stress") && tag.contains("id"))
            if (tag.getInt("id") == this.id)
                stress = tag.getDouble("stress");
    }


    public void mechanicalLoadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag myTag = tag.getCompound("MechanicalBlock_"+this.id);
        internalVelocity = myTag.getDouble("internalVelocity");
    }


    public void mechanicalSaveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag myTag = new CompoundTag();
        myTag.putDouble("internalVelocity", internalVelocity);

        tag.put("MechanicalBlock_"+this.id, myTag);
    }
}


