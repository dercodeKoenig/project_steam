package ProjectSteam.Blocks.Mechanics.Axle;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import static ProjectSteam.Blocks.Mechanics.Axle.BlockAxleBase.ROTATION_AXIS;

public class EntityAxleBase extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    int lastLight = 0;

    public double myInertia;
    public double myFriction;
    public double maxStress;

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0,this) {
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
    };

    public EntityAxleBase(BlockEntityType t, BlockPos pos, BlockState blockState) {
        super(t, pos, blockState);

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


    public void tick(){
        myMechanicalBlock.mechanicalTick();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityAxleBase) t).tick();
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (myState.getBlock() instanceof BlockAxleBase) {
            Direction.Axis blockAxis = myState.getValue(ROTATION_AXIS);
            if (side.getAxis() == blockAxis) {
                return myMechanicalBlock;
            }
        }
        return null;
    }

    @Override
    public BlockEntity getBlockEntity(){return this;}

    @Override
    public void onLoad() {
        super.onLoad();
        myMechanicalBlock.mechanicalOnload();
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
}