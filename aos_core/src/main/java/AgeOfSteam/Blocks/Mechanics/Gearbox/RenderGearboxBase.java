package AgeOfSteam.Blocks.Mechanics.Gearbox;

import ARLib.obj.Face;
import ARLib.obj.ModelFormatException;
import ARLib.obj.WavefrontObject;
import AgeOfSteam.Main;
import AgeOfSteam.Static;
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

import static AgeOfSteam.Static.*;
import static net.minecraft.client.renderer.RenderStateShard.*;

public abstract class RenderGearboxBase implements BlockEntityRenderer<EntityGearboxBase> {

    static WavefrontObject model;
    static ResourceLocation tex;

    static VertexBuffer vertexBuffer_in= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static VertexBuffer vertexBuffer_out= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static VertexBuffer vertexBuffer_mid= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh_in;
    static MeshData mesh_out;
    static MeshData mesh_mid;

    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath(Main.MODID, "objmodels/gearbox.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }


        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("small_output").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh_in = b.build();
        vertexBuffer_in.bind();
        vertexBuffer_in.upload(mesh_in);
        byteBuffer.close();


        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("big_output").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        vertexBuffer_out.bind();
        mesh_out = b.build();
        vertexBuffer_out.upload(mesh_out);
        byteBuffer.close();


        byteBuffer = new ByteBufferBuilder(2048);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("connection").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh_mid = b.build();
        vertexBuffer_mid.bind();
        vertexBuffer_mid.upload(mesh_mid);
        byteBuffer.close();
    }


    public RenderGearboxBase(BlockEntityRendererProvider.Context c, ResourceLocation texture)  {
        super();
        this.tex = texture;
    }


    @Override
    public void render(EntityGearboxBase tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState myState = tile.getBlockState();
        if (myState.getBlock() instanceof BlockGearboxBase) {
            Direction facing = myState.getValue(BlockGearboxBase.FACING);

            Matrix4f m1 = new Matrix4f(RenderSystem.getModelViewMatrix());
            m1 = m1.mul(stack.last().pose());
            m1 = m1.translate(0.5f, 0.5f, 0.5f);

            double facingBasedRotationMultiplier = 1;
            if (facing == Direction.NORTH) {
                // all good
            }
            if (facing == Direction.SOUTH) {
                facingBasedRotationMultiplier = -1;
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 180));
            }
            if (facing == Direction.EAST) {
                facingBasedRotationMultiplier = -1;
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 270));
            }
            if (facing == Direction.WEST) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 90));
            }

            RenderSystem.setShader(Static::getEntitySolidDynamicNormalDynamicLightShader);
            LIGHTMAP.setupRenderState();
            LEQUAL_DEPTH_TEST.setupRenderState();
            NO_TRANSPARENCY.setupRenderState();
            RenderSystem.setShaderTexture(0, tex);

            ShaderInstance shader = RenderSystem.getShader();

            Matrix4f m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * tile.myMechanicalBlock.getRotationMultiplierToOutside(facing) * (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS * partialTick))));


            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');

            shader.apply();
            vertexBuffer_in.bind();
            vertexBuffer_in.draw();


            m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * tile.myMechanicalBlock.getRotationMultiplierToOutside(facing.getOpposite()) * (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();

            vertexBuffer_out.bind();
            vertexBuffer_out.draw();
            //shader.clear();


            m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));
            m2 = m2.translate(0.3f, 0, 0);

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();

            vertexBuffer_mid.bind();
            vertexBuffer_mid.draw();
            //shader.clear();

            m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));
            m2 = m2.translate(-0.3f, 0, 0);

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();

            vertexBuffer_mid.bind();
            vertexBuffer_mid.draw();


            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}