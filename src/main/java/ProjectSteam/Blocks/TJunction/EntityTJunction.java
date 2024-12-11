package ProjectSteam.Blocks.TJunction;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.api.AbstractMechanicalBlock;
import ProjectSteam.api.IMechanicalBlockProvider;
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

import static ProjectSteam.Registry.ENTITY_TJUNCTION;

public class EntityTJunction extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    VertexBuffer vertexBuffer2;
    MeshData mesh2;
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
        public double getMass(Direction face, @Nullable BlockState myBlockState) {
            return myMass;
        }

        @Override
        public double getTorqueResistance(Direction face, @Nullable BlockState myBlockState) {
            return myFriction;
        }

        @Override
        public double getTorqueProduced(Direction face, @Nullable BlockState myBlockState) {
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@Nullable Direction receivingFace, @Nullable BlockState myState) {
            if (receivingFace == null) return 1;
            if (myState == null) myState = getBlockState();

            if (myState.getBlock() instanceof BlockTJunction) {
                Direction.Axis myAxis = myState.getValue(BlockTJunction.AXIS);
                if(myAxis == receivingFace.getAxis()){
                    return 1;
                }
            }
            if(receivingFace == myState.getValue(BlockTJunction.FACING)){
                if(receivingFace.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
                    return -1;
                else
                    return 1;
            }
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


    public EntityTJunction(BlockPos pos, BlockState blockState) {
        super(ENTITY_TJUNCTION.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                vertexBuffer2 = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
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


    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (side.getAxis() == myState.getValue(BlockTJunction.AXIS) || side == myState.getValue(BlockTJunction.FACING))
            return myMechanicalBlock;
        return null;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityTJunction) t).tick();
    }
}