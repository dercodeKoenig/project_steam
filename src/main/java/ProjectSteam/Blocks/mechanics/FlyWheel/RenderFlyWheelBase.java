package ProjectSteam.Blocks.mechanics.FlyWheel;

import ARLib.obj.Face;
import ARLib.obj.ModelFormatException;
import ARLib.obj.WavefrontObject;
import ProjectSteam.Blocks.mechanics.Axle.EntityAxleBase;
import ProjectSteam.Static;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static ProjectSteam.Static.*;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderFlyWheelBase implements BlockEntityRenderer<EntityFlyWheelBase> {

    public WavefrontObject model;
    public ResourceLocation tex;
    public WavefrontObject flywheel;



    public RenderFlyWheelBase(BlockEntityRendererProvider.Context c, ResourceLocation texture) {
        super();
        this.tex = texture;
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/small_crankshaft.obj"));
            flywheel = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/flywheel.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }
    }



    void renderModelWithLight(EntityFlyWheelBase tile, int light) {
        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("Cube").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        for (Face i : flywheel.groupObjects.get("fly_wheel").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh = b.build();
        tile.vertexBuffer.upload(tile.mesh);
        byteBuffer.close();
    }

    @Override
    public void render(EntityFlyWheelBase tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState axleState = tile.getBlockState();
        if (axleState.getBlock() instanceof BlockFlyWheelBase) {
            Direction.Axis facingAxis = axleState.getValue(BlockFlyWheelBase.ROTATION_AXIS);

            tile.vertexBuffer.bind();


            RenderSystem.setShader(Static::getEntitySolidDynamicNormalShader);
            LIGHTMAP.setupRenderState();
            LEQUAL_DEPTH_TEST.setupRenderState();
            NO_TRANSPARENCY.setupRenderState();
            RenderSystem.setShaderTexture(0, tex);

            if (packedLight != tile.lastLight) {
                tile.lastLight = packedLight;
                renderModelWithLight(tile, packedLight);
            }

            ShaderInstance shader = RenderSystem.getShader();
            Matrix4f m1 = new Matrix4f(RenderSystem.getModelViewMatrix());
            m1 = m1.mul(stack.last().pose());

            m1 = m1.translate(0.5f, 0.5f, 0.5f);

            if (facingAxis == Direction.Axis.Z) {
                // no rotation
            } else if (facingAxis == Direction.Axis.X) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0, 1f, 0, 90));
            } else if (facingAxis == Direction.Axis.Y) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(1f, 0, 0, -90));
            }

            m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f, (float) ( tile.myMechanicalBlock.currentRotation+rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS*partialTick)));
            //System.out.println(tile.currentRotation);

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m1, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m1).invert().transpose());

            shader.apply();
            tile.vertexBuffer.draw();
            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();

        }
    }
}