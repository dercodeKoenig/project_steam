package ProjectSteam.Blocks.Mechanics.Axle;

import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.obj.Face;
import ARLib.obj.ModelFormatException;
import ARLib.obj.WavefrontObject;
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

public class RenderAxleBase implements BlockEntityRenderer<EntityAxleBase> {

    static WavefrontObject model;
    static ResourceLocation tex;
     static VertexBuffer vertexBuffer =new VertexBuffer(VertexBuffer.Usage.STATIC);;
     static MeshData mesh;

    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/rod_new.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }

        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("Cube").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh = b.build();
        vertexBuffer.bind();
        vertexBuffer.upload(mesh);
        byteBuffer.close();
    }


    public RenderAxleBase(BlockEntityRendererProvider.Context c, ResourceLocation texture) {
        super();
        this.tex = texture;
    }


    @Override
    public void render(EntityAxleBase tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState axleState = tile.getBlockState();
        if (axleState.getBlock() instanceof BlockAxleBase) {
            // hide the block when it is part of multi-block-structure
            if(axleState.getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED))return;

            Direction.Axis facingAxis = axleState.getValue(BlockAxleBase.ROTATION_AXIS);

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



            RenderSystem.setShader(Static::getEntitySolidDynamicNormalDynamicLightShader);
            LIGHTMAP.setupRenderState();
            LEQUAL_DEPTH_TEST.setupRenderState();
            NO_TRANSPARENCY.setupRenderState();
            RenderSystem.setShaderTexture(0, tex);

            ShaderInstance shader = RenderSystem.getShader();
            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m1, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m1).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();

            vertexBuffer.bind();
            vertexBuffer.draw();

            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();

        }
    }
}