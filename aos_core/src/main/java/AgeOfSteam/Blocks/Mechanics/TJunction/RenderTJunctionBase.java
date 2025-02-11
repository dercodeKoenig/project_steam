package AgeOfSteam.Blocks.Mechanics.TJunction;

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

public abstract class RenderTJunctionBase implements BlockEntityRenderer<EntityTJunctionBase> {

    static WavefrontObject model;
    static ResourceLocation tex;
    static VertexBuffer vertexBuffer= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh;
    static     VertexBuffer vertexBuffer2= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh2;
    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath(Main.MODID, "objmodels/t_junction.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }

        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(2048);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("gear2").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh2 = b.build();
        vertexBuffer2.bind();
        vertexBuffer2.upload(mesh2);
        byteBuffer.close();

        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("gear1").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh = b.build();
        vertexBuffer.bind();
        vertexBuffer.upload(mesh);
        byteBuffer.close();
    }


    public RenderTJunctionBase(BlockEntityRendererProvider.Context c, ResourceLocation texture) {
        super();
        this.tex = texture;
    }



    @Override
    public void render(EntityTJunctionBase tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState myState = tile.getBlockState();
        if (myState.getBlock() instanceof BlockTJunctionBase) {
            Direction.Axis axis = myState.getValue(BlockTJunctionBase.AXIS);
            Direction facing = myState.getValue(BlockTJunctionBase.FACING);

            RenderSystem.setShader(Static::getEntitySolidDynamicNormalDynamicLightShader);
            LIGHTMAP.setupRenderState();
            LEQUAL_DEPTH_TEST.setupRenderState();
            NO_TRANSPARENCY.setupRenderState();
            RenderSystem.setShaderTexture(0, tex);

            ShaderInstance shader = RenderSystem.getShader();

            boolean isInverted = myState.getValue(BlockTJunctionBase.INVERTED);
            float inversionMultiplier = isInverted ? -1f:1f;

            Matrix4f m1 = new Matrix4f(RenderSystem.getModelViewMatrix());
            m1 = m1.mul(stack.last().pose());
            m1 = m1.translate(0.5f, 0.5f, 0.5f);

            Matrix4f m2 = new Matrix4f(m1);
            double rotationMultiplier = tile.myMechanicalBlock.getRotationMultiplierToOutside(facing);

            if (facing == Direction.EAST) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, (float) 270));
                rotationMultiplier *= -1;
            }
            if (facing == Direction.WEST) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, (float) 90));
            }
            if (facing == Direction.NORTH) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, (float) 0));
            }
            if (facing == Direction.SOUTH) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, (float) 180));
                rotationMultiplier *= -1;
            }
            if (facing == Direction.UP) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(1f, 0f, 0f, (float) 90));
                rotationMultiplier *= -1;
            }
            if (facing == Direction.DOWN) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(1f, 0f, 0f, (float) 270));
            }

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 0f, 1f, (float) (inversionMultiplier*14.7f+ (tile.myMechanicalBlock.currentRotation * rotationMultiplier + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS*partialTick * rotationMultiplier))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();
            
            vertexBuffer.bind();
            vertexBuffer.draw();


            m2 = new Matrix4f(m1);
            if (axis == Direction.Axis.Z) {
                //m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0f, (float) 1f, 0f, (float) 0));
            }
            if (axis == Direction.Axis.X) {
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, 90f));
            }
            if(axis != Direction.Axis.Y){
                if(isInverted)
                    m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, 180f));
            }

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 0f, 1f, inversionMultiplier*(float) (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS*partialTick)));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();

            vertexBuffer2.bind();
            vertexBuffer2.draw();



            VertexBuffer.unbind();
            shader.clear();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}