package ProjectSteam.Blocks.mechanics.Clutch;

import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ProjectSteam.core.AbstractMechanicalBlock;
import ProjectSteam.core.IMechanicalBlockProvider;
import ProjectSteam.core.MechanicalFlowData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.joml.Vector3f;

import java.util.*;

import static ProjectSteam.Registry.ENTITY_CLUTCH;
import static ProjectSteam.Static.WOODEN_SOUNDS;

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
        public double getMass(Direction face) {
            return massPerSide;
        }

        @Override
        public double getTorqueResistance(Direction face) {
            double resistance = baseFrictionPerSide;
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face) {
            if (isFullyConnected) return 0;
            if (shouldConnect) {
                return current_force.get(face);
            }
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
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
                    propagateVelocityUpdate(internalVelocity, getBlockState().getValue(BlockClutch.FACING), workedPositions, false, false);

                    double rotationDiff = serverRotation - currentRotation;
                    if (Math.abs(rotationDiff) < 3600) {
                        // to avoid precision errors, the rotation will not always increase.
                        // at some point it will reset and this will create a large gap between the rotations for up to a few ticks
                        // in this case, ignore and wait until the client had reset itself and continue with sync
                        propagateResetRotation(currentRotation + rotationDiff * 0.01, getBlockState().getValue(BlockClutch.FACING), new HashSet<AbstractMechanicalBlock>());
                        timeWithImpossibleSmoothSync = 0;
                    } else {
                        timeWithImpossibleSmoothSync++;
                        if (timeWithImpossibleSmoothSync > 200) {
                            propagateResetRotation(serverRotation, getBlockState().getValue(BlockClutch.FACING), new HashSet<AbstractMechanicalBlock>());
                        }
                    }

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

                    if (resetStress) {
                        lastTickHadForceToDistribute = true;
                        Set<AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                        aggregateConnectedParts(null, connectedBlocks);
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
                    if (lastTickHadForceToDistribute) {
                        lastTickHadForceToDistribute = false;
                        Set<AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                        aggregateConnectedParts(null, connectedBlocks);
                        for (AbstractMechanicalBlock i : connectedBlocks) {
                            if (!i.forceDistributionDeq.isEmpty()) {
                                nodeInfo info = i.forceDistributionDeq.removeFirst();
                                info.nextTarget.walkDistributeForce(info.nextInputFace, info.node);
                                lastTickHadForceToDistribute = true;
                            }
                        }
                    }
                }
            }
            hasReceivedUpdate = false;
            applyRotations();
if(level.isClientSide)
            serverRotation += internalVelocity;

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

        public void propagateResetRotation(double rotation, Direction receivingFace, HashSet<AbstractMechanicalBlock> workedPositions) {
            // keep the current rotationdiff
            double d = myMechanicalBlockA.currentRotation - myMechanicalBlockB.currentRotation;

            if (!workedPositions.contains(this)) {
                workedPositions.add(this);
                Map<Direction, AbstractMechanicalBlock> connections = me.getConnectedParts(me, this);

                currentRotation = rotation;

                for (Direction i : connections.keySet()) {
                    double rotationToOutside = currentRotation;
                    if(i == getBlockState().getValue(BlockClutch.FACING).getOpposite())
                        rotationToOutside -= d;
                    connections.get(i).propagateResetRotation(rotationToOutside, i.getOpposite(), workedPositions);
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
        public double getMass(Direction face) {
            return massPerSide;
        }

        @Override
        public double getTorqueResistance(Direction face) {
            double resistance = baseFrictionPerSide;
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face) {
            if (isFullyConnected) return 0;
            if (shouldConnect) {
                return current_force.get(face);
            }
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
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
                    propagateVelocityUpdate(internalVelocity, getBlockState().getValue(BlockClutch.FACING).getOpposite(), workedPositions, false, false);

                    double rotationDiff = serverRotation - currentRotation;
                    if (Math.abs(rotationDiff) < 3600) {
                        // to avoid precision errors, the rotation will not always increase.
                        // at some point it will reset and this will create a large gap between the rotations for up to a few ticks
                        // in this case, ignore and wait until the client had reset itself and continue with sync
                        propagateResetRotation(currentRotation + rotationDiff * 0.01, getBlockState().getValue(BlockClutch.FACING).getOpposite(), new HashSet<AbstractMechanicalBlock>());
                        timeWithImpossibleSmoothSync = 0;
                    } else {
                        timeWithImpossibleSmoothSync++;
                        if (timeWithImpossibleSmoothSync > 200) {
                            propagateResetRotation(serverRotation, getBlockState().getValue(BlockClutch.FACING).getOpposite(), new HashSet<AbstractMechanicalBlock>());
                        }
                    }

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


                    if (resetStress) {
                        lastTickHadForceToDistribute = true;
                        Set<AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                        aggregateConnectedParts(null, connectedBlocks);
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
                    if (lastTickHadForceToDistribute) {
                        lastTickHadForceToDistribute = false;
                        Set<AbstractMechanicalBlock> connectedBlocks = new HashSet<>();
                        aggregateConnectedParts(null, connectedBlocks);
                        for (AbstractMechanicalBlock i : connectedBlocks) {
                            if (!i.forceDistributionDeq.isEmpty()) {
                                nodeInfo info = i.forceDistributionDeq.removeFirst();
                                info.nextTarget.walkDistributeForce(info.nextInputFace, info.node);
                                lastTickHadForceToDistribute = true;
                            }
                        }
                    }
                }
            }
            hasReceivedUpdate = false;
            applyRotations();
            if(level.isClientSide)
                serverRotation += internalVelocity;

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
        public void propagateResetRotation(double rotation, Direction receivingFace, HashSet<AbstractMechanicalBlock> workedPositions) {
            // keep the current rotationdiff
            double d = myMechanicalBlockB.currentRotation - myMechanicalBlockA.currentRotation;

            if (!workedPositions.contains(this)) {
                workedPositions.add(this);
                Map<Direction, AbstractMechanicalBlock> connections = me.getConnectedParts(me, this);

                currentRotation = rotation;

                for (Direction i : connections.keySet()) {
                    double rotationToOutside = currentRotation;
                    if(i == getBlockState().getValue(BlockClutch.FACING))
                        rotationToOutside -= d;
                    connections.get(i).propagateResetRotation(rotationToOutside, i.getOpposite(), workedPositions);
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
        if(!level.isClientSide) {
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
        if(level.isClientSide){
            if(level.hasNeighborSignal(getBlockPos()) && Math.abs(myMechanicalBlockB.internalVelocity - myMechanicalBlockA.internalVelocity) > 0.5) {
                int i = Math.abs(level.random.nextInt() % 1000);
                boolean doParticle = i < timeSinceConnectStart*10;
                timeSinceConnectStart++;

                if(doParticle) {
                    double x = level.random.nextDouble() - 0.5;
                    double y = level.random.nextDouble() - 0.5;
                    double z = level.random.nextDouble() - 0.5;
                    level.addParticle(new DustParticleOptions(new Vector3f(0.5f, 0.5f, 0.5f), 1f), getBlockPos().getCenter().x + x, getBlockPos().getCenter().y + 0.5 + y, getBlockPos().getCenter().z + z, x, y, z);
                }

            }else timeSinceConnectStart = 0;
        }


        if(level.random.nextFloat() < 0.005*(Math.abs(myMechanicalBlockA.internalVelocity)+Math.abs(myMechanicalBlockB.internalVelocity))) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.002f*(float)((Math.abs(myMechanicalBlockA.internalVelocity)+Math.abs(myMechanicalBlockB.internalVelocity))), 1.0f);  //
        }

        if(level.hasNeighborSignal(getBlockPos()) && Math.abs(myMechanicalBlockB.internalVelocity - myMechanicalBlockA.internalVelocity) > 0.5) {
            for (int i = 0; i < 2; i++) {
                SoundEvent[] clutch_sounds = {
                        SoundEvents.GRAVEL_BREAK,
                        SoundEvents.STONE_HIT
                };
                int randomIndex = level.random.nextInt(clutch_sounds.length);
                SoundEvent randomEvent = clutch_sounds[randomIndex];
                level.playSound(null, getBlockPos(), randomEvent,
                        SoundSource.BLOCKS, 0.005f * (float) ((Math.abs(myMechanicalBlockA.internalVelocity - myMechanicalBlockB.internalVelocity))), 0.1f * (float) (Math.abs(myMechanicalBlockA.internalVelocity - myMechanicalBlockB.internalVelocity)));  //
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