package ProjectSteam.Blocks.BlockMotor;

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

public class EntityMotor extends MechanicalPartBlockEntityBaseExample {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    int lastLight = 0;

    public EntityMotor(BlockPos pos, BlockState blockState) {
        super(ENTITY_MOTOR.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }

        myForce = 20;
        myWorkPerTick = 600;
        myFriction = 0.5;
        myMass = 2;
    }

    @Override
    public void onLoad() {
        super.onLoad();
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
        if (myState.getBlock() instanceof BlockMotor) {
            if (face == myState.getValue(BlockMotor.FACING)) {
                return true;
            }
        }
        return false;
    }


    public double getRotationMultiplierToInside(@javax.annotation.Nullable Direction receivingFace, @javax.annotation.Nullable BlockState myBlockState){
        return 1;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityMotor)t).tick();
    }
}