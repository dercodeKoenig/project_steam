package ProjectSteam.Blocks.mechanics.BlockMotor;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.core.AbstractMechanicalBlock;
import ProjectSteam.core.IMechanicalBlockProvider;
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

import static ProjectSteam.Registry.ENTITY_MOTOR;

public class EntityMotor extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    VertexBuffer vertexBuffer;
    MeshData mesh;


    public double myMass = 10;
    public boolean isRedstonePowered = false;

    public static double MOTOR_BASE_FRICTION = 5;

    public static double MOTOR_FORCE = 500;
    public static double MAX_SPEED = 20;

    int lastLight = 0;

    public EntityMotor(BlockPos pos, BlockState blockState) {
        super(ENTITY_MOTOR.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
    }

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return MOTOR_FORCE * 10;
        }

        @Override
        public double getMass(Direction face) {
            return myMass;
        }

        @Override
        public double getTorqueResistance(Direction face) {
            // TODO: only break when it can generate rf (when it has space for rf in inventory)
            double resistance = MOTOR_BASE_FRICTION;
            if (!isRedstonePowered) {
                double additionalFriction = MOTOR_FORCE * Math.abs(internalVelocity) / MAX_SPEED;
                resistance += additionalFriction;
            }
            return resistance;
        }

        @Override
        public double getTorqueProduced(Direction face) {
            if (isRedstonePowered) {
                double actualForce = MOTOR_FORCE * Math.max(0, (1 - Math.abs(internalVelocity) / MAX_SPEED));
                double facingMultiplier = getBlockState().getValue(BlockMotor.FACING).getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
                return actualForce * facingMultiplier;
            } else {
                return 0;
            }
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace) {
            return 1;
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

    @Override
    public void setRemoved() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer.close();
            });

        }
        super.setRemoved();
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();

        if (level.hasNeighborSignal(getBlockPos())) {
            isRedstonePowered = true;
        } else {
            isRedstonePowered = false;
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
            if (side == myState.getValue(BlockMotor.FACING))
                return myMechanicalBlock;
        }
        return null;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityMotor) t).tick();
    }
}