package ProjectSteam.Blocks.mechanics.Axle;

import ARLib.network.PacketBlockEntity;
import ProjectSteam.core.AbstractMechanicalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.checkerframework.checker.units.qual.C;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static ProjectSteam.Registry.ENTITY_AXLE_CRANKSHAFT;
import static ProjectSteam.Registry.ENTITY_AXLE_FLYWHEEL;

public class EntityWoodenAxleCrankShaft extends EntityWoodenAxle {

    public int rotationoffset = 0;

    public EntityWoodenAxleCrankShaft(BlockPos pos, BlockState blockState) {
        super(ENTITY_AXLE_CRANKSHAFT.get(), pos, blockState);
        maxStress = 100;
        myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
            @Override
            public double getMaxStress() {
                return maxStress;
            }

            @Override
            public double getInertia(Direction face) {
                return myInertia;
            }

            @Override
            public double getTorqueResistance(Direction face) {
                return myFriction;
            }

            @Override
            public double getTorqueProduced(Direction face) {
                return 0;
            }

            @Override
            public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
                return 1;
            }

            @Override
            public void propagateResetRotation(double rotation, Direction receivingFace, HashSet<AbstractMechanicalBlock> workedPositions) {
                if (!workedPositions.contains(this)) {
                    workedPositions.add(this);
                    Map<Direction, AbstractMechanicalBlock> connections = me.getConnectedParts(me, this);


                    if (receivingFace != null) {
                        rotation += rotationoffset * 90;
                        System.out.println(rotationoffset);
                    }

                    currentRotation = rotation;

                    for (Direction i : connections.keySet()) {
                        double rotationToOutside = (currentRotation - rotationoffset * 90) * getRotationMultiplierToOutside(i);
                        connections.get(i).propagateResetRotation(rotationToOutside, i.getOpposite(), workedPositions);
                    }
                }
            }
        };
    }

    public void incRotationOffset() {
        if (!level.isClientSide) {
            rotationoffset++;
            if (rotationoffset > 3) rotationoffset = 0;
            CompoundTag i = new CompoundTag();
            i.putInt("rotationOffset", rotationoffset);
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, new ChunkPos(getBlockPos()), PacketBlockEntity.getBlockEntityPacket(this, i));
            myMechanicalBlock.propagateResetRotation(myMechanicalBlock.currentRotation + 90, null, new HashSet<>());
            setChanged();
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
        if (level.isClientSide) {
            CompoundTag i = new CompoundTag();
            i.putUUID("request_rotationOffset", Minecraft.getInstance().player.getUUID());
            PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(this, i));
        }
    }


    @Override
    public void readClient(CompoundTag tag) {
        if (tag.contains("rotationOffset")) {
            rotationoffset = tag.getInt("rotationOffset");
            myMechanicalBlock.propagateResetRotation(myMechanicalBlock.currentRotation + 90, null, new HashSet<>());
        }
        super.readClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        if (tag.contains("request_rotationOffset")) {
            UUID from = tag.getUUID("request_rotationOffset");
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            CompoundTag res = new CompoundTag();
            res.putInt("rotationOffset", rotationoffset);
            PacketDistributor.sendToPlayer(p, PacketBlockEntity.getBlockEntityPacket(this, res));
        }
        super.readServer(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        rotationoffset = tag.getInt("rotationOffset");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("rotationOffset", rotationoffset);
    }
}