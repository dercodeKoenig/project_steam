package ProjectSteam.Blocks.mechanics.Gearbox;

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

public class RenderGearbox implements BlockEntityRenderer<EntityGearbox> {

    static WavefrontObject model;
    static ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png");

    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/gearbox.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }
    }


    public RenderGearbox(BlockEntityRendererProvider.Context c) {
        super();
    }


    void renderModelWithLight(EntityGearbox tile, int light) {

        tile.vertexBuffer_in.bind();
        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("small_output").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh_in = b.build();
        tile.vertexBuffer_in.upload(tile.mesh_in);
        byteBuffer.close();


        tile.vertexBuffer_out.bind();
        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("big_output").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh_out = b.build();
        tile.vertexBuffer_out.upload(tile.mesh_out);
        byteBuffer.close();


        tile.vertexBuffer_mid.bind();
        byteBuffer = new ByteBufferBuilder(2048);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("connection").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh_mid = b.build();
        tile.vertexBuffer_mid.upload(tile.mesh_mid);
        byteBuffer.close();
    }

    @Override
    public void render(EntityGearbox tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState myState = tile.getBlockState();
        if (myState.getBlock() instanceof BlockGearbox) {
            Direction facing = myState.getValue(BlockGearbox.FACING);

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

            double facingBasedRotationMultiplier = 1;
            if(facing == Direction.NORTH){
                // all good
            }
            if(facing == Direction.SOUTH){
                facingBasedRotationMultiplier = -1;
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 180));
            }
            if(facing == Direction.EAST){
                facingBasedRotationMultiplier = -1;
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 270));
            }
            if(facing == Direction.WEST){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 90));
            }



            Matrix4f m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * tile.myMechanicalBlock.getRotationMultiplierToOutside(facing) * (tile.myMechanicalBlock.currentRotation + tile.myMechanicalBlock.internalVelocity * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

            shader.apply();
            tile.vertexBuffer_in.bind();
            tile.vertexBuffer_in.draw();
            //shader.clear();



            m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * tile.myMechanicalBlock.getRotationMultiplierToOutside(facing.getOpposite()) * (tile.myMechanicalBlock.currentRotation + tile.myMechanicalBlock.internalVelocity * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

            shader.apply();
            tile.vertexBuffer_out.bind();
            tile.vertexBuffer_out.draw();
            //shader.clear();



            m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));
            m2 = m2.translate(0.3f,0,0);

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) ( facingBasedRotationMultiplier * (tile.myMechanicalBlock.currentRotation + tile.myMechanicalBlock.internalVelocity * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

            shader.apply();
            tile.vertexBuffer_mid.bind();
            tile.vertexBuffer_mid.draw();
            //shader.clear();

            m2 = new Matrix4f(m1);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 0));
            m2 = m2.translate(-0.3f,0,0);

            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f,
                    (float) (facingBasedRotationMultiplier * (tile.myMechanicalBlock.currentRotation + tile.myMechanicalBlock.internalVelocity * partialTick))));

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

            shader.apply();
            tile.vertexBuffer_mid.bind();
            tile.vertexBuffer_mid.draw();


            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}