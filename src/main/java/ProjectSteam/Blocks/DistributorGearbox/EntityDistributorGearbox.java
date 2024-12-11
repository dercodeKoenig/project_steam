package ProjectSteam.Blocks.DistributorGearbox;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Blocks.Gearbox.BlockGearbox;
import ProjectSteam.api.AbstractMechanicalBlock;
import ProjectSteam.api.IMechanicalBlockProvider;
import ProjectSteam.api.MechanicalPartBlockEntityBaseExample;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;

import static ProjectSteam.Registry.ENTITY_DISTRIBUTOR_GEARBOX;

public class EntityDistributorGearbox extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    int lastLight = 0;


    double myMass = 0.5;
    double myFriction = 10;
    double maxStress = 600;

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return maxStress;
        }

        @Override
        public double getMass(Direction face) {
            return myMass;
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

            if (myState.getBlock() instanceof BlockDistributorGearbox) {
                Direction.Axis myNormalAxis = myState.getValue(BlockDistributorGearbox.ROTATION_AXIS);

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
            if(myState.getValue(BlockDistributorGearbox.ROTATION_AXIS) == Direction.Axis.Y) {
                if(face.getAxis() == Direction.Axis.X)
                    return 14.7f;
            }
            if(myState.getValue(BlockDistributorGearbox.ROTATION_AXIS) == Direction.Axis.X) {
                if(face.getAxis() == Direction.Axis.Y)
                    return 14.7f;
            }
            if(myState.getValue(BlockDistributorGearbox.ROTATION_AXIS) == Direction.Axis.Z) {
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

        @Override
        public void onPropagatedTickEnd() {

        }
    };

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


    public EntityDistributorGearbox(BlockPos pos, BlockState blockState) {
        super(ENTITY_DISTRIBUTOR_GEARBOX.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
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
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (side.getAxis() != myState.getValue(BlockDistributorGearbox.ROTATION_AXIS))
            return myMechanicalBlock;
        return null;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityDistributorGearbox) t).tick();
    }
}