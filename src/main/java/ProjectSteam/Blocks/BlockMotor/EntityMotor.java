package ProjectSteam.Blocks.BlockMotor;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Blocks.Axle.BlockAxle;
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

import static ProjectSteam.Blocks.Axle.BlockAxle.ROTATION_AXIS;
import static ProjectSteam.Registry.ENTITY_AXLE;
import static ProjectSteam.Registry.ENTITY_MOTOR;

public class EntityMotor extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    VertexBuffer vertexBuffer2;
    MeshData mesh2;
    public double myForce = 0;
    public double myFriction = 10;
    public double myMass = 1;

    public static double MOTOR_FORCE = 250;
    public static double MAX_SPEED = 20;

    int lastLight = 0;

    public EntityMotor(BlockPos pos, BlockState blockState) {
        super(ENTITY_MOTOR.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                vertexBuffer2 = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
    }

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0,this) {
        @Override
        public double getMass(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return myMass;
        }

        @Override
        public double getTorqueResistance(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            return myFriction;
        }

        @Override
        public double getTorqueProduced(Direction face, @org.jetbrains.annotations.Nullable BlockState myBlockState) {
            double actualForce = myForce * Math.max(0, (1 - Math.abs(internalVelocity) / MAX_SPEED));
            return actualForce;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace, @org.jetbrains.annotations.Nullable BlockState myState) {
            return 1;
        }

        @Override
        public void onPropagatedTickEnd() {

        }
    };
    @Override
    public BlockEntity getBlockEntity(){return this;}

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
    }
    @Override
    public void setRemoved() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer.close();
                vertexBuffer2.close();
            });

        }
        super.setRemoved();
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();

        if (level.hasNeighborSignal(getBlockPos())) {
            myForce = MOTOR_FORCE;
        } else {
            myForce = 0;
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
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (myState.getBlock() instanceof BlockMotor) {
            if(side == myState.getValue(BlockMotor.FACING))
                return myMechanicalBlock;
        }
        return null;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityMotor)t).tick();
    }
}