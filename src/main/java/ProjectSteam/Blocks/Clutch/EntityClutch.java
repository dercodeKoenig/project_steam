package ProjectSteam.Blocks.Clutch;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ProjectSteam.api.AbstractMechanicalBlock;
import ProjectSteam.api.IMechanicalBlockProvider;
import ProjectSteam.api.MechanicalFlowData;
import ProjectSteam.api.MechanicalPartBlockEntityBaseExample;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static ProjectSteam.Registry.ENTITY_CLUTCH;

public class EntityClutch extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {


    boolean shouldConnect;
    int timeSinceConnectStart;
    boolean isFullyConnected;
    public boolean last_wasPowered = false;

    double massPerSide = 2;
    double baseFrictionPerSide = 0.5;

    public AbstractMechanicalBlock myMechanicalBlockA = new AbstractMechanicalBlock(0,this) {
        @Override
        public double getMass(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return massPerSide;
        }

        @Override
        public double getTorqueResistance(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {

            double resistance =baseFrictionPerSide;
            if(shouldConnect && !isFullyConnected) {
                resistance+=current_resistance.get(face);
            }
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            if (isFullyConnected) return 0;
            if(shouldConnect) {
                return current_force.get(face);
            }
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace, @org.jetbrains.annotations.Nullable BlockState myState) {
            return 1;
        }

        @Override
        public void onPropagatedTickEnd() {

        }
    };


    public AbstractMechanicalBlock myMechanicalBlockB = new AbstractMechanicalBlock(1,this) {
        @Override
        public double getMass(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return massPerSide;
        }

        @Override
        public double getTorqueResistance(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {

            double resistance =baseFrictionPerSide;
            if(shouldConnect && !isFullyConnected) {
                resistance+=current_resistance.get(face);
            }
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            if (isFullyConnected) return 0;
            if(shouldConnect) {
                return current_force.get(face);
            }
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace, @org.jetbrains.annotations.Nullable BlockState myState) {
            return 1;
        }

        @Override
        public void onPropagatedTickEnd() {

        }
    };



    public EntityClutch(BlockPos pos, BlockState blockState) {
        super(ENTITY_CLUTCH.get(), pos, blockState);

        for (Direction i : Direction.values()){
            current_force.put(i,0.0);
            current_resistance.put(i,0.0);
        }
    }


    Map<Direction, Double> current_resistance = new HashMap<>();
    Map<Direction, Double> current_force = new HashMap<>();

    void updateResistance(BlockState state){
        if (state == null) state = level.getBlockState(getBlockPos());
        if (state.getBlock() instanceof BlockClutch) {
            Direction myFacing = state.getValue(BlockClutch.FACING);
            double rotationDiff = velocityA - velocityB;

            if(rotationDiff > 0) {
                double forceConstant = 1;
                double workingForce = forceConstant * timeSinceConnectStart;
                current_resistance.put(myFacing,0.0);
            }
            if(rotationDiff < 0) {
                double forceConstant = 1;
                double workingForce = forceConstant * timeSinceConnectStart;
                current_resistance.put(myFacing.getOpposite(),0.0);
            }
        }
    }
    void updateForce(BlockState state){
        if (state == null) state = level.getBlockState(getBlockPos());
        if (state.getBlock() instanceof BlockClutch) {
            Direction myFacing = state.getValue(BlockClutch.FACING);
            double rotationDiff = 0;

                rotationDiff = velocityB - velocityA;


                double forceConstant = 1;

                current_force.put(myFacing, Math.signum(rotationDiff)* forceConstant * timeSinceConnectStart);

                current_force.put(myFacing.getOpposite(), - Math.signum(rotationDiff) * forceConstant * timeSinceConnectStart);

        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void setRemoved() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer.close();
            });

        }
        super.setRemoved();
    }


    @Override
    public boolean connectsAtFace(Direction face, @Nullable BlockState myState) {
        if (myState == null)
            myState = level.getBlockState(getBlockPos());
        if (myState.getBlock() instanceof BlockClutch) {
            return face == myState.getValue(BlockClutch.FACING) || face == myState.getValue(BlockClutch.FACING).getOpposite();
        }
        return false;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityClutch) t).tick();
    }




    public void tick() {

        MechanicalBlockData myData = getMechanicalData();
        BlockState myState = level.getBlockState(getBlockPos());


        if (!myData.hasReceivedUpdate) {
            propagateTickBeforeUpdate();
        }

        if (level.isClientSide()) {


            if (!updatedFromSideA && !velUpdatedFromSideA) {
                HashSet<BlockPos> workedPositions = new HashSet<>();
                propagateVelocityUpdate(velocityA, myState.getValue(BlockClutch.FACING), workedPositions, false);
            }
            if (!updatedFromSideB && !velUpdatedFromSideB) {
                HashSet<BlockPos> workedPositions = new HashSet<>();
                propagateVelocityUpdate(velocityB, myState.getValue(BlockClutch.FACING).getOpposite(), workedPositions, false);
            }
            myData.lastPing++;
            if (myData.lastPing > myData.cttam_timeout / 2) {
                myData.lastPing = 0;
                CompoundTag tag = new CompoundTag();
                tag.putUUID("ping_is_master", Minecraft.getInstance().player.getUUID());
                PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, tag));
            }
        }

        if (!level.isClientSide()) {
            double t = 0.05;
            if (!updatedFromSideA && !velUpdatedFromSideA) {
                //System.out.println("A");
                HashSet<BlockPos> workedPositions = new HashSet<>();
                MechanicalFlowData dataA = new MechanicalFlowData();
                getPropagatedData(dataA, myState.getValue(BlockClutch.FACING), workedPositions);
                workedPositions.clear();
                dataA.combinedTransformedMass = Math.max(dataA.combinedTransformedMass, 0.01);
                double newVelocityA = velocityA;
                newVelocityA += dataA.combinedTransformedForce / dataA.combinedTransformedMass * t;
                float signBeforeA = (float) Math.signum(newVelocityA);
                newVelocityA -= dataA.combinedTransformedResistanceForce * Math.signum(newVelocityA) / dataA.combinedTransformedMass * t;
                float signAfterA = (float) Math.signum(newVelocityA);
                if ((signAfterA < 0 && signBeforeA > 0) || (signAfterA > 0 && signBeforeA < 0))
                    newVelocityA = 0;
                if (Math.abs(newVelocityA) < 0.0001) newVelocityA = 0;
                if (newVelocityA > velocityA + 90)
                    newVelocityA = velocityA + 90;
                if (newVelocityA < velocityA - 90)
                    newVelocityA = velocityA - 90;
                propagateVelocityUpdate(newVelocityA, myState.getValue(BlockClutch.FACING), workedPositions, false);
            }
            if (!updatedFromSideB && !velUpdatedFromSideB) {
                //System.out.println("B");
                HashSet<BlockPos> workedPositions = new HashSet<>();
                MechanicalFlowData dataB = new MechanicalFlowData();
                getPropagatedData(dataB, myState.getValue(BlockClutch.FACING).getOpposite(), workedPositions);
                workedPositions.clear();
                dataB.combinedTransformedMass = Math.max(dataB.combinedTransformedMass, 0.01);
                double newVelocityB = velocityB;
                newVelocityB += dataB.combinedTransformedForce / dataB.combinedTransformedMass * t;
                float signBeforeB = (float) Math.signum(newVelocityB);
                newVelocityB -= dataB.combinedTransformedResistanceForce * Math.signum(newVelocityB) / dataB.combinedTransformedMass * t;
                float signAfterB = (float) Math.signum(newVelocityB);
                if ((signAfterB < 0 && signBeforeB > 0) || (signAfterB > 0 && signBeforeB < 0))
                    newVelocityB = 0;
                if (Math.abs(newVelocityB) < 0.0001) newVelocityB = 0;
                if (newVelocityB > velocityB + 90)
                    newVelocityB = velocityB + 90;
                if (newVelocityB < velocityB - 90)
                    newVelocityB = velocityB - 90;
                propagateVelocityUpdate(newVelocityB, myState.getValue(BlockClutch.FACING).getOpposite(), workedPositions, false);
            }
        }


        applyRotations();

        if (!level.isClientSide()) {
            for (UUID i : myData.clientsTrackingThisAsMaster.keySet()) {
                // increment timeout counter
                myData.clientsTrackingThisAsMaster.put(i, myData.clientsTrackingThisAsMaster.get(i) + 1);
                if (myData.clientsTrackingThisAsMaster.get(i) > myData.cttam_timeout) {
                    myData.clientsTrackingThisAsMaster.remove(i);
                    break; // break to prevent concurrent modification bs
                }
            }
            if (last_velocityA != velocityA || last_velocityB != velocityB) {
                last_velocityA = velocityA;
                last_velocityB = velocityB;
                myData.me.setChanged();
                CompoundTag updateTag = new CompoundTag();
                updateTag.putDouble("velocityA", velocityA);
                updateTag.putDouble("velocityB", velocityB);
                for (UUID i : myData.clientsTrackingThisAsMaster.keySet()) {
                    ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(i);
                    PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(this, updateTag));
                }
            }

            if (Math.abs(velocityA) > 100000 || Math.abs(velocityB) > 100000 || Double.isNaN(velocityA) || Double.isNaN(velocityB)) {
                System.out.println("set block to air because velocity is way too high!  " + getMechanicalData().me.getBlockPos());
                getMechanicalData().me.getLevel().setBlock(getMechanicalData().me.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        getMechanicalData().hasReceivedUpdate = false;
        updatedFromSideA = false;
        updatedFromSideB = false;
        velUpdatedFromSideA = false;
        velUpdatedFromSideB = false;
if(!level.isClientSide()) {
    if (level.hasNeighborSignal(getBlockPos())) {
        if (!last_wasPowered) {
            last_wasPowered = true;
            timeSinceConnectStart = 0;
            lastRotationDiff = Math.abs(velocityB - velocityA);
        }
        shouldConnect = true;
        if (timeSinceConnectStart < 1000) {
            timeSinceConnectStart += 1;
        }
        if (Math.abs(velocityB - velocityA) < 0.5)
            isFullyConnected = true;
        else {
            updateResistance(myState);
            updateForce(myState);
            //System.out.println(timeSinceConnectStart);
        }
    } else {
        last_wasPowered = false;
        isFullyConnected = false;
        shouldConnect = false;
    }
}
//System.out.println(level.isClientSide+":"+shouldConnect+":"+isFullyConnected+":"+velocityA+":"+velocityB);

    }


    public void getPropagatedData(MechanicalFlowData data, @org.jetbrains.annotations.Nullable Direction requestedFrom, HashSet<BlockPos> workedPositions) {
        MechanicalBlockData myData = getMechanicalData();
        BlockState myState = level.getBlockState(getBlockPos());

        if (!isFullyConnected) {
            if (requestedFrom == myState.getValue(BlockClutch.FACING) && !updatedFromSideA) {
                updatedFromSideA = true;
                // the following is in case this is the master.
                // if it is not the master the other tile should already have been updated and will simply do nothing with the data
                MechanicalBlock b = myData.connectedParts.get(requestedFrom);
                if (b != null) {
                    b.getPropagatedData(data, requestedFrom.getOpposite(), workedPositions);
                }

                data.combinedTransformedForce += getTorqueProduced(requestedFrom, myState);
                data.combinedTransformedResistanceForce += getTorqueResistance(requestedFrom, myState);
                data.combinedTransformedMass += getMass(requestedFrom, myState);
                data.combinedTransformedMomentum += velocityA * getMass(requestedFrom, myState);

            }
            if (requestedFrom == myState.getValue(BlockClutch.FACING).getOpposite() && !updatedFromSideB) {
                updatedFromSideB = true;
                // the following is in case this is the master.
                // if it is not the master the other tile should already have been updated and will simply do nothing with the data
                MechanicalBlock b = myData.connectedParts.get(requestedFrom);
                if (b != null) {
                    b.getPropagatedData(data, requestedFrom.getOpposite(), workedPositions);
                }

                data.combinedTransformedForce += getTorqueProduced(requestedFrom, myState);
                data.combinedTransformedResistanceForce += getTorqueResistance(requestedFrom, myState);
                data.combinedTransformedMass += getMass(requestedFrom, myState);
                data.combinedTransformedMomentum += velocityB * getMass(requestedFrom, myState);
            }
        }
        if (isFullyConnected) {
            updatedFromSideA = true;
            updatedFromSideB = true;
            if (!workedPositions.contains(getBlockPos())) {
                workedPositions.add(getBlockPos());

                MechanicalFlowData myInputFlowData = new MechanicalFlowData();

                MechanicalBlock b = myData.connectedParts.get(requestedFrom.getOpposite());
                if (b != null) {

                    MechanicalFlowData d = new MechanicalFlowData();

                    b.getPropagatedData(d, requestedFrom, workedPositions);


                    myInputFlowData.combinedTransformedForce += d.combinedTransformedForce;
                    myInputFlowData.combinedTransformedMass += d.combinedTransformedMass;
                    myInputFlowData.combinedTransformedMomentum += d.combinedTransformedMomentum;
                    myInputFlowData.combinedTransformedResistanceForce += d.combinedTransformedResistanceForce;
                }


                myInputFlowData.combinedTransformedForce += getTorqueProduced(requestedFrom, myState);
                myInputFlowData.combinedTransformedMass += getMass(requestedFrom, myState);
                myInputFlowData.combinedTransformedMomentum += myData.internalVelocity * getMass(requestedFrom, myState);
                myInputFlowData.combinedTransformedResistanceForce += getTorqueResistance(requestedFrom, myState);


                data.combinedTransformedForce += myInputFlowData.combinedTransformedForce;
                data.combinedTransformedResistanceForce += myInputFlowData.combinedTransformedResistanceForce;
                data.combinedTransformedMass += myInputFlowData.combinedTransformedMass;
                data.combinedTransformedMomentum += myInputFlowData.combinedTransformedMomentum;

            }
        }
    }

    public void applyRotations() {
        double eqs = 2 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9;

        rotationA += velocityA;
        if (rotationA > 360 * eqs) rotationA -= 360 * eqs;
        if (rotationA < -360 * eqs) rotationA += 360 * eqs;

        rotationB += velocityB;
        if (rotationB > 360 * eqs) rotationB -= 360 * eqs;
        if (rotationB < -360 * eqs) rotationB += 360 * eqs;

    }

    public void propagateVelocityUpdate(double velocity, @org.jetbrains.annotations.Nullable Direction receivingFace, HashSet<BlockPos> workedPositions, boolean ignorePreviousUpdate) {
        MechanicalBlockData myData = getMechanicalData();
        BlockState myState = level.getBlockState(getBlockPos());
        if(!isFullyConnected){
            if(receivingFace == myState.getValue(BlockClutch.FACING) && !velUpdatedFromSideA){
                velocityA = velocity;
                velUpdatedFromSideA = true;

                //System.out.println(level.isClientSide+":"+velocity+"-"+ receivingFace);

                // the following is in case this is the master.
                MechanicalBlock b = myData.connectedParts.get(receivingFace);
                if(b!=null){
                    b.propagateVelocityUpdate(velocity,receivingFace.getOpposite(), workedPositions, ignorePreviousUpdate);
                }

            }
            if(receivingFace == myState.getValue(BlockClutch.FACING).getOpposite() &&!velUpdatedFromSideB){
                velocityB = velocity;
                velUpdatedFromSideB = true;

                //System.out.println(level.isClientSide+":"+velocity+"-"+ receivingFace);

                // the following is in case this is the master.
                MechanicalBlock b = myData.connectedParts.get(receivingFace);
                if(b!=null){
                    b.propagateVelocityUpdate(velocity,receivingFace.getOpposite(),workedPositions, ignorePreviousUpdate);
                }
            }
        }

        if (isFullyConnected) {
            velUpdatedFromSideA = true;
            velUpdatedFromSideB = true;

            if (!ignorePreviousUpdate && !level.isClientSide && workedPositions.contains(getBlockPos()) && Math.abs(velocity - velocityA) > 0.00001) {
                // break this block because something is wrong with the network
                System.out.println("breaking the network because something is wrong: this tile received a different velocity update in the same tick:" + getBlockPos());
                System.out.println("current reveiced rotation from face " + receivingFace + ":" + velocity * getRotationMultiplierToInside(receivingFace, myState) + ". Last received velocity: " + myData.internalVelocity);

                level.destroyBlock(getBlockPos(),true);
            }

            if (!workedPositions.contains(getBlockPos())) {
                workedPositions.add(getBlockPos());

                velocityA = velocity;
                velocityB = velocity;

                // forward the transformed rotation to the other blocks
                for (Direction i : myData.connectedParts.keySet()) {
                    MechanicalBlock b = myData.connectedParts.get(i);
                    b.propagateVelocityUpdate(velocity, i.getOpposite(), workedPositions, ignorePreviousUpdate);
                }
            }
        }
    }


    public void readServer(CompoundTag tag) {
        if (tag.contains("ping_is_master")) {
            UUID from = tag.getUUID("ping_is_master");
            getMechanicalData().clientsTrackingThisAsMaster.put(from, 0);

            CompoundTag updateTag = new CompoundTag();
            updateTag.putDouble("velocityA", velocityA);
            updateTag.putDouble("velocityB", velocityB);
            ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            PacketDistributor.sendToPlayer(player, PacketBlockEntity.getBlockEntityPacket(getMechanicalData().me, updateTag));
        }
    }

    public void readClient(CompoundTag tag) {
        if (tag.contains("velocityA"))
            velocityA = tag.getDouble("velocityA");
        if (tag.contains("velocityB"))
            velocityB = tag.getDouble("velocityB");
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        velocityA = tag.getDouble("velocityA");
        velocityB = tag.getDouble("velocityB");
        super.loadAdditional(tag, registries);
    }


    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putDouble("velocityA",velocityA);
        tag.putDouble("velocityB",velocityB);
        super.saveAdditional(tag, registries);
    }
}