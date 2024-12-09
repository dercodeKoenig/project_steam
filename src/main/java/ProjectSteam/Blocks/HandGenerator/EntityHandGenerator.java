package ProjectSteam.Blocks.HandGenerator;

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

import static ProjectSteam.Registry.ENTITY_HAND_GENERATOR;
import static ProjectSteam.Registry.ENTITY_MOTOR;

public class EntityHandGenerator extends MechanicalPartBlockEntityBaseExample {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    VertexBuffer vertexBuffer2;
    MeshData mesh2;
    VertexBuffer vertexBuffer3;
    MeshData mesh3;
    public double myForce = 0;

    public static double MOTOR_FORCE = 25;
    public static double MAX_SPEED = 20;

    int lastLight = 0;

    int ticksRemainingForForce = 0;

    public EntityHandGenerator(BlockPos pos, BlockState blockState) {
        super(ENTITY_HAND_GENERATOR.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                vertexBuffer2 = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                vertexBuffer3 = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }

        myFriction = 0.5;
        myMass = 2;
    }
    @Override
    public double getTorqueProduced(Direction face, @javax.annotation.Nullable BlockState myBlockState) {
        double actualForce = myForce * Math.max(0, (1 - Math.abs(myMechanicalData.internalVelocity) / MAX_SPEED));
        return actualForce;
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
                vertexBuffer2.close();
                vertexBuffer3.close();
            });

        }
        super.setRemoved();
    }

    public boolean onPlayerClicked(){
        if(ticksRemainingForForce < 20) {
            ticksRemainingForForce += 20;
            return true;
        }
        return false;
    }

@Override
public void tick() {
    super.tick();
    if(ticksRemainingForForce > 0){
        ticksRemainingForForce--;
        myForce = MOTOR_FORCE;
    } else {
        myForce = 0;
    }
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
        if (myState.getBlock() instanceof BlockHandGenerator) {
            if (face == myState.getValue(BlockHandGenerator.FACING)) {
                return true;
            }
        }
        return false;
    }


    public double getRotationMultiplierToInside(@javax.annotation.Nullable Direction receivingFace, @javax.annotation.Nullable BlockState myBlockState){
        return 1;
    }


    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityHandGenerator)t).tick();
    }
}