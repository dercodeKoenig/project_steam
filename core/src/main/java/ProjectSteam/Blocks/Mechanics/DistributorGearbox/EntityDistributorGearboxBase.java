package ProjectSteam.Blocks.Mechanics.DistributorGearbox;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Map;

import static ProjectSteam.Registry.ENTITY_DISTRIBUTOR_GEARBOX;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityDistributorGearboxBase extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    double myInertia;
    double myFriction;
    double maxStress;

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
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
            if (receivingFace == null) return 1;
            BlockState myState = getBlockState();

            if (myState.getBlock() instanceof BlockDistributorGearboxbase) {
                Direction.Axis myNormalAxis = myState.getValue(BlockDistributorGearboxbase.ROTATION_AXIS);

                if (myNormalAxis == Direction.Axis.Y) {
                    if (receivingFace == Direction.NORTH) return 1;
                    if (receivingFace == Direction.EAST) return 1;
                    if (receivingFace == Direction.SOUTH) return -1;
                    if (receivingFace == Direction.WEST) return -1;
                }
                if (myNormalAxis == Direction.Axis.X) {
                    if (receivingFace == Direction.NORTH) return 1;
                    if (receivingFace == Direction.UP) return 1;
                    if (receivingFace == Direction.SOUTH) return -1;
                    if (receivingFace == Direction.DOWN) return -1;
                }
                if (myNormalAxis == Direction.Axis.Z) {
                    if (receivingFace == Direction.WEST) return 1;
                    if (receivingFace == Direction.UP) return 1;
                    if (receivingFace == Direction.EAST) return -1;
                    if (receivingFace == Direction.DOWN) return -1;
                }
            }
            return 1;
        }
        double getRotationOffsetForFace(Direction face){

            if(face == null)return 0;

            BlockState myState = getBlockState();
            if(myState.getValue(BlockDistributorGearboxbase.ROTATION_AXIS) == Direction.Axis.Y) {
                if(face.getAxis() == Direction.Axis.X)
                    return 14.7f;
            }
            if(myState.getValue(BlockDistributorGearboxbase.ROTATION_AXIS) == Direction.Axis.X) {
                if(face.getAxis() == Direction.Axis.Y)
                    return 14.7f;
            }
            if(myState.getValue(BlockDistributorGearboxbase.ROTATION_AXIS) == Direction.Axis.Z) {
                if(face.getAxis() == Direction.Axis.Y)
                    return 14.7f;
            }

            return 0;
        }

@Override
        public void propagateResetRotation(double rotation,Direction receivingFace, HashSet<AbstractMechanicalBlock> workedPositions) {
            if (!workedPositions.contains(this)) {
                workedPositions.add(this);
                Map<Direction, AbstractMechanicalBlock> connections = me.getConnectedParts(me, this);

                if (receivingFace != null) {
                    rotation+=getRotationOffsetForFace(receivingFace)*getRotationMultiplierToInside(receivingFace);
                    rotation *= getRotationMultiplierToInside(receivingFace);
                }

                currentRotation = rotation;

                for (Direction i : connections.keySet()) {
                    double rotationToOutside = (-getRotationOffsetForFace(i)+currentRotation) * getRotationMultiplierToOutside(i);
                    connections.get(i).propagateResetRotation(rotationToOutside, i.getOpposite(), workedPositions);
                }
            }
        }
    };

    public EntityDistributorGearboxBase(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
    }


    public void tick() {
        myMechanicalBlock.mechanicalTick();
        if(level.random.nextFloat() < 0.005*Math.abs(myMechanicalBlock.internalVelocity)) {
            int randomIndex = level.random.nextInt(WOODEN_SOUNDS.length);
            SoundEvent randomEvent = WOODEN_SOUNDS[randomIndex];
            level.playSound(null, getBlockPos(), randomEvent,
                    SoundSource.BLOCKS, 0.005f*(float)Math.abs(myMechanicalBlock.internalVelocity), 1.0f);  //
        }
    }


    @Override
    public void readClient(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadServer(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (side.getAxis() != myState.getValue(BlockDistributorGearboxbase.ROTATION_AXIS))
            return myMechanicalBlock;
        return null;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityDistributorGearboxBase) t).tick();
    }
}