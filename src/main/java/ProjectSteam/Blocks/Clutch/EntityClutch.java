package ProjectSteam.Blocks.Clutch;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ProjectSteam.api.AbstractMechanicalBlock;
import ProjectSteam.api.IMechanicalBlockProvider;
import ProjectSteam.api.MechanicalFlowData;
import ProjectSteam.api.MechanicalPartBlockEntityBaseExample;
import com.ibm.icu.impl.Pair;
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

import java.util.*;

import static ProjectSteam.Registry.ENTITY_CLUTCH;

public class EntityClutch extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {


    boolean shouldConnect;
    int timeSinceConnectStart;
    boolean isFullyConnected;
    public boolean last_wasPowered = false;
    Map<Direction, Double> current_force = new HashMap<>();

    double massPerSide = 1;
    double baseFrictionPerSide = 5;

    public AbstractMechanicalBlock myMechanicalBlockA = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return 100000;
        }
        @Override
        public double getMass(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return massPerSide;
        }

        @Override
        public double getTorqueResistance(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            double resistance = baseFrictionPerSide;
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            if (isFullyConnected) return 0;
            if (shouldConnect) {
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

        public void mechanicalTick() {

            BlockEntity myTile = me.getBlockEntity();
            if (myTile.getLevel().isClientSide()) {
                if (!hasReceivedUpdate) {
                    propagateTickBeforeUpdate();
                    HashSet<AbstractMechanicalBlock> workedPositions = new HashSet<>();
                    propagateVelocityUpdate(internalVelocity, null, workedPositions, false, false);

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
                    getPropagatedData(data, getBlockState().getValue(BlockClutch.FACING), workedPositions);
                    workedPositions.clear();

                    double t = (double) 1 / tps;

                    data.combinedTransformedMass = Math.max(data.combinedTransformedMass, 0.01);
                    double newVelocity = internalVelocity;
                    newVelocity += data.combinedTransformedForce / data.combinedTransformedMass * t;
                    float signBefore = (float) Math.signum(newVelocity);
                    newVelocity -= data.combinedTransformedResistanceForce * Math.signum(newVelocity) / data.combinedTransformedMass * t;
                    float signAfter = (float) Math.signum(newVelocity);
                    if (Math.abs(newVelocity) < 0.0001) newVelocity = 0;

                    if ((signAfter < 0 && signBefore > 0) || (signAfter > 0 && signBefore < 0))
                        newVelocity = 0;
                    if (newVelocity > internalVelocity + 90)
                        newVelocity = internalVelocity + 90;
                    if (newVelocity < internalVelocity - 90)
                        newVelocity = internalVelocity - 90;

                    //System.out.println(newVelocity + ":" + myTile.getBlockPos() + ":" + data.combinedTransformedForce + ":" + data.combinedTransformedMass + ":" + data.combinedTransformedResistanceForce);


                    boolean resetStress = me.getBlockEntity().getLevel().random.nextInt(20*120) == 0;

                    propagateVelocityUpdate(newVelocity, getBlockState().getValue(BlockClutch.FACING), workedPositions, false, resetStress);

                    Set<AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                    aggregateConnectedParts(connectedBlocks);

                    if (resetStress) {
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


                    for (AbstractMechanicalBlock i : connectedBlocks) {
                        if (!i.forceDistributionDeq.isEmpty()) {
                            nodeInfo info = i.forceDistributionDeq.removeFirst();
                            info.nextTarget.walkDistributeForce(info.nextInputFace, info.node);
                        }
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
                if (Math.abs(internalVelocity) > 100000 || Double.isNaN(internalVelocity)) {
                    System.out.println("set block to air because velocity is way too high!  " + me.getBlockEntity().getBlockPos());
                    me.getBlockEntity().getLevel().destroyBlock(me.getBlockEntity().getBlockPos(), true);
                }
            }
        }
    };


    public AbstractMechanicalBlock myMechanicalBlockB = new AbstractMechanicalBlock(1, this) {
        @Override
        public double getMaxStress() {
            return 100000;
        }
        @Override
        public double getMass(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return massPerSide;
        }

        @Override
        public double getTorqueResistance(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            double resistance = baseFrictionPerSide;
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            if (isFullyConnected) return 0;
            if (shouldConnect) {
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

        public void mechanicalTick() {

            BlockEntity myTile = me.getBlockEntity();
            if (myTile.getLevel().isClientSide()) {
                if (!hasReceivedUpdate) {
                    propagateTickBeforeUpdate();
                    HashSet<AbstractMechanicalBlock> workedPositions = new HashSet<>();
                    propagateVelocityUpdate(internalVelocity, null, workedPositions, false, false);

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
                    getPropagatedData(data, getBlockState().getValue(BlockClutch.FACING).getOpposite(), workedPositions);
                    workedPositions.clear();

                    double t = (double) 1 / tps;

                    data.combinedTransformedMass = Math.max(data.combinedTransformedMass, 0.01);
                    double newVelocity = internalVelocity;
                    newVelocity += data.combinedTransformedForce / data.combinedTransformedMass * t;
                    float signBefore = (float) Math.signum(newVelocity);
                    newVelocity -= data.combinedTransformedResistanceForce * Math.signum(newVelocity) / data.combinedTransformedMass * t;
                    float signAfter = (float) Math.signum(newVelocity);
                    if (Math.abs(newVelocity) < 0.0001) newVelocity = 0;

                    if ((signAfter < 0 && signBefore > 0) || (signAfter > 0 && signBefore < 0))
                        newVelocity = 0;
                    if (newVelocity > internalVelocity + 90)
                        newVelocity = internalVelocity + 90;
                    if (newVelocity < internalVelocity - 90)
                        newVelocity = internalVelocity - 90;

                    //System.out.println(newVelocity + ":" + myTile.getBlockPos() + ":" + data.combinedTransformedForce + ":" + data.combinedTransformedMass + ":" + data.combinedTransformedResistanceForce);


                    boolean resetStress = me.getBlockEntity().getLevel().random.nextInt(20*120) == 0;

                    propagateVelocityUpdate(newVelocity, getBlockState().getValue(BlockClutch.FACING).getOpposite(), workedPositions, false, resetStress);

                    Set<AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                    aggregateConnectedParts(connectedBlocks);

                    if (resetStress) {
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


                    for (AbstractMechanicalBlock i : connectedBlocks) {
                        if (!i.forceDistributionDeq.isEmpty()) {
                            nodeInfo info = i.forceDistributionDeq.removeFirst();
                            info.nextTarget.walkDistributeForce(info.nextInputFace, info.node);
                        }
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
                if (Math.abs(internalVelocity) > 100000 || Double.isNaN(internalVelocity)) {
                    System.out.println("set block to air because velocity is way too high!  " + me.getBlockEntity().getBlockPos());
                    me.getBlockEntity().getLevel().destroyBlock(me.getBlockEntity().getBlockPos(), true);
                }
            }
        }
    };


    public EntityClutch(BlockPos pos, BlockState blockState) {
        super(ENTITY_CLUTCH.get(), pos, blockState);

        for (Direction i : Direction.values()) {
            current_force.put(i, 0.0);
        }
    }


    void updateForce(BlockState state) {
        if (state == null) state = getBlockState();
        if (state.getBlock() instanceof BlockClutch) {
            Direction myFacing = state.getValue(BlockClutch.FACING);

            double rotationDiff = myMechanicalBlockB.internalVelocity - myMechanicalBlockA.internalVelocity;
            double forceConstant = 5;
            current_force.put(myFacing, Math.signum(rotationDiff) * forceConstant * timeSinceConnectStart);
            current_force.put(myFacing.getOpposite(), -Math.signum(rotationDiff) * forceConstant * timeSinceConnectStart);
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlockA.mechanicalOnload();
        myMechanicalBlockB.mechanicalOnload();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }


    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (myState.getBlock() instanceof BlockClutch) {
            if (side == myState.getValue(BlockClutch.FACING))
                return myMechanicalBlockA;
            if (side == myState.getValue(BlockClutch.FACING).getOpposite())
                return myMechanicalBlockB;
        }
        return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityClutch) t).tick();
    }


    public void tick() {

        myMechanicalBlockA.mechanicalTick();
        myMechanicalBlockB.mechanicalTick();

        if (!level.isClientSide()) {
            if (level.hasNeighborSignal(getBlockPos())) {
                if (!last_wasPowered) {
                    last_wasPowered = true;
                    timeSinceConnectStart = 0;
                }
                shouldConnect = true;
                if (timeSinceConnectStart < 1000) {
                    timeSinceConnectStart += 1;
                }
                if (Math.abs(myMechanicalBlockB.internalVelocity - myMechanicalBlockA.internalVelocity) < 0.5)
                    isFullyConnected = true;
                else {
                    updateForce(getBlockState());
                }
            } else {
                last_wasPowered = false;
                isFullyConnected = false;
                shouldConnect = false;
            }
        }
    }

    @Override
    public Map<Direction, AbstractMechanicalBlock> getConnectedParts(IMechanicalBlockProvider mechanicalBlockProvider, AbstractMechanicalBlock MechanicalBlock) {
        Map<Direction, AbstractMechanicalBlock> connectedBlocks = new HashMap<>();

        if(isFullyConnected) {
            if(MechanicalBlock == myMechanicalBlockB) {
                connectedBlocks.put(getBlockState().getValue(BlockClutch.FACING), myMechanicalBlockA);
            }
            if(MechanicalBlock == myMechanicalBlockA) {
                connectedBlocks.put(getBlockState().getValue(BlockClutch.FACING).getOpposite(), myMechanicalBlockB);
            }
        }

        if(MechanicalBlock == myMechanicalBlockB) {
            BlockEntity otherBE = level.getBlockEntity(getBlockPos().relative(getBlockState().getValue(BlockClutch.FACING).getOpposite()));
            if (otherBE instanceof IMechanicalBlockProvider p) {
                AbstractMechanicalBlock other = p.getMechanicalBlock(getBlockState().getValue(BlockClutch.FACING));
                if (other instanceof AbstractMechanicalBlock otherMechBlock) {
                    connectedBlocks.put(getBlockState().getValue(BlockClutch.FACING).getOpposite(), otherMechBlock);
                }
            }
        }
        if(MechanicalBlock == myMechanicalBlockA) {
            BlockEntity otherBE = level.getBlockEntity(getBlockPos().relative(getBlockState().getValue(BlockClutch.FACING)));
            if (otherBE instanceof IMechanicalBlockProvider p) {
                AbstractMechanicalBlock other = p.getMechanicalBlock(getBlockState().getValue(BlockClutch.FACING).getOpposite());
                if (other instanceof AbstractMechanicalBlock otherMechBlock) {
                    connectedBlocks.put(getBlockState().getValue(BlockClutch.FACING), otherMechBlock);
                }
            }
        }

        return connectedBlocks;
    }

    public void readServer(CompoundTag tag) {
        myMechanicalBlockA.mechanicalReadServer(tag);
        myMechanicalBlockB.mechanicalReadServer(tag);
    }

    public void readClient(CompoundTag tag) {
        myMechanicalBlockA.mechanicalReadClient(tag);
        myMechanicalBlockB.mechanicalReadClient(tag);
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        myMechanicalBlockA.mechanicalLoadAdditional(tag, registries);
        myMechanicalBlockB.mechanicalLoadAdditional(tag, registries);
        super.loadAdditional(tag, registries);
    }


    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        myMechanicalBlockA.mechanicalSaveAdditional(tag, registries);
        myMechanicalBlockB.mechanicalSaveAdditional(tag, registries);
        super.saveAdditional(tag, registries);
    }
}