package ProjectSteam.Blocks.HandGenerator;

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

import static ProjectSteam.Static.POSITION_COLOR_TEXTURE_NORMAL_LIGHT;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderHandGenerator implements BlockEntityRenderer<EntityHandGenerator> {

    static WavefrontObject model;
    static ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png");

    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/handcranked_generator.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }
    }


    public RenderHandGenerator(BlockEntityRendererProvider.Context c) {
        super();
    }


    void renderModelWithLight(EntityHandGenerator tile, int light) {

        tile.vertexBuffer.bind();
        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("base").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh = b.build();
        tile.vertexBuffer.upload(tile.mesh);
        byteBuffer.close();

        tile.vertexBuffer2.bind();
        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("fly_wheel").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh2 = b.build();
        tile.vertexBuffer2.upload(tile.mesh2);
        byteBuffer.close();

        tile.vertexBuffer3.bind();
        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("hand_wheel").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh3 = b.build();
        tile.vertexBuffer3.upload(tile.mesh3);
        byteBuffer.close();
    }

    @Override
    public void render(EntityHandGenerator tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState axleState = tile.getLevel().getBlockState(tile.getBlockPos());
        if (axleState.getBlock() instanceof BlockHandGenerator) {
            Direction facing = axleState.getValue(BlockHandGenerator.FACING);

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

            double rotorRotationMultiplier = 1;
            if(facing == Direction.WEST){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 90f));
            }
            if(facing == Direction.EAST){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 270f));
                rotorRotationMultiplier = -1;
            }
            if(facing == Direction.SOUTH){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 180f));
                rotorRotationMultiplier = -1;
            }
            if(facing == Direction.NORTH){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 0f));
                //rotorRotationMultiplier = -1;
            }

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m1, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m1).invert().transpose());

            shader.apply();
            tile.vertexBuffer.bind();
            tile.vertexBuffer.draw();

            Matrix4f m2 = new Matrix4f(m1);
            m2 = m2.translate(0.0f, 0.0f, -0.2f);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 0f, 1.0f, (float) (rotorRotationMultiplier*( tile.myMechanicalBlock.currentRotation+tile.myMechanicalBlock.internalVelocity*partialTick))));
            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

            shader.apply();
            tile.vertexBuffer2.bind();
            tile.vertexBuffer2.draw();

            m2 = new Matrix4f(m1);
            m2 = m2.translate(0.0f, 0.1f, 0.2f);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1f, 0f, (float) (rotorRotationMultiplier*( tile.myMechanicalBlock.currentRotation+tile.myMechanicalBlock.internalVelocity*partialTick))));
            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

            shader.apply();
            tile.vertexBuffer3.bind();
            tile.vertexBuffer3.draw();

            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}