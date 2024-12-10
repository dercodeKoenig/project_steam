package ProjectSteam.Blocks.Gearbox;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.Blocks.DistributorGearbox.BlockDistributorGearbox;
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

import static ProjectSteam.Registry.ENTITY_DISTRIBUTOR_GEARBOX;
import static ProjectSteam.Registry.ENTITY_GEARBOX;

public class EntityGearbox extends BlockEntity implements IMechanicalBlockProvider, INetworkTagReceiver {

    VertexBuffer vertexBuffer_in;
    VertexBuffer vertexBuffer_out;
    VertexBuffer vertexBuffer_mid;
    MeshData mesh_in;
    MeshData mesh_out;
    MeshData mesh_mid;
    int lastLight = 0;


    double myMass = 0.5;
    double myFriction = 5;
double maxStress = 500;

    public AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0,this) {
        @Override
        public double getMaxStress() {
            return maxStress;
        }
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
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@org.jetbrains.annotations.Nullable Direction receivingFace, @org.jetbrains.annotations.Nullable BlockState myState) {
                if (receivingFace == null) return 1;
                if (myState == null) myState = level.getBlockState(getBlockPos());

                if (myState.getBlock() instanceof BlockGearbox) {
                    Direction facing = myState.getValue(BlockGearbox.FACING);

                    if (receivingFace == facing.getOpposite())
                        return (double) -3 / 2;
                    if (receivingFace == facing)
                        return (double) -2 / 3;
                }
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

    public EntityGearbox(BlockPos pos, BlockState blockState) {
        super(ENTITY_GEARBOX.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer_in = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                vertexBuffer_out = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                vertexBuffer_mid = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }
    }

    @Override
    public void setRemoved() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer_in.close();
                vertexBuffer_out.close();
                vertexBuffer_mid.close();
            });

        }
        super.setRemoved();
    }



    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if(side.getAxis() == myState.getValue(BlockGearbox.FACING).getAxis())
            return myMechanicalBlock;
        return null;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityGearbox) t).tick();
    }
}