package ProjectSteam.Blocks.Axle;

import ProjectSteam.core.AbstractMechanicalBlock;
import ProjectSteam.core.MechanicalPartBlockEntityBaseExample;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import static ProjectSteam.Blocks.Axle.BlockAxle.ROTATION_AXIS;
import static ProjectSteam.Registry.ENTITY_AXLE;

public class EntityAxle extends MechanicalPartBlockEntityBaseExample {

    VertexBuffer vertexBuffer;
    MeshData mesh;
    int lastLight = 0;

    public EntityAxle(BlockPos pos, BlockState blockState) {
        super(ENTITY_AXLE.get(), pos, blockState);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RenderSystem.recordRenderCall(() -> {
                vertexBuffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
            });
        }

        myMass = 0.02;
        myFriction = 0.01;
        maxStress = 600;
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityAxle) t).tick();
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction side) {
        BlockState myState = getBlockState();
        if (myState.getBlock() instanceof BlockAxle) {
            Direction.Axis blockAxis = myState.getValue(ROTATION_AXIS);
            if (side.getAxis() == blockAxis) {
                return myMechanicalBlock;
            }
        }
        return null;
    }
}