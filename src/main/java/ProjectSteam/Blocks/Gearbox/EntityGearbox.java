package ProjectSteam.Blocks.Gearbox;

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

public class EntityGearbox extends MechanicalPartBlockEntityBaseExample {

    VertexBuffer vertexBuffer_in;
    VertexBuffer vertexBuffer_out;
    VertexBuffer vertexBuffer_mid;
    MeshData mesh_in;
    MeshData mesh_out;
    MeshData mesh_mid;
    int lastLight = 0;

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
    public void onLoad() {
        super.onLoad();
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
    public void readServer(CompoundTag compoundTag) {
        super.readServer(compoundTag);
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
    super.readClient(compoundTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

    }

    @Override
    public boolean connectsAtFace(Direction face, @Nullable BlockState myState) {
        if (myState == null)
            myState = level.getBlockState(getBlockPos());
       return face.getAxis() == myState.getValue(BlockGearbox.FACING).getAxis();
    }


    public double getRotationMultiplierToInside(@javax.annotation.Nullable Direction receivingFace) {
        if (receivingFace == null) return 1;

        BlockState myState = level.getBlockState(getBlockPos());
        if(myState.getBlock() instanceof BlockGearbox) {
            Direction facing = myState.getValue(BlockGearbox.FACING);

            if (receivingFace == facing.getOpposite())
                return (double) -3 / 2;
            if (receivingFace == facing)
                return (double) -2 / 3;
        }
        return 1;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityGearbox)t).tick();
    }
}