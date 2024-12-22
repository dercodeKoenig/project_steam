package ProjectSteamAW2Generators.WaterWheel;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static ProjectSteam.Static.*;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderWaterWheelGenerator implements BlockEntityRenderer<EntityWaterWheelGenerator> {

    static WavefrontObject model;
    static ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png");

    static VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh;

    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam_aw2_generators", "objmodels/waterwheel_generator.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }


        ByteBufferBuilder byteBuffer;
        BufferBuilder b;


        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("wheel").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh = b.build();
        vertexBuffer.bind();
        vertexBuffer.upload(mesh);
        byteBuffer.close();
    }

    public RenderWaterWheelGenerator(BlockEntityRendererProvider.Context c) {
        super();
    }

    public AABB getRenderBoundingBox(EntityWaterWheelGenerator tile) {

        return new AABB(tile.getBlockPos()).inflate(1);
    }


    @Override
    public void render(EntityWaterWheelGenerator tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (tile.isRemoved()) return;
        BlockState state = tile.getBlockState();
        if (state.getBlock() instanceof BlockWaterWheelGenerator) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            Matrix4f m1 = new Matrix4f(RenderSystem.getModelViewMatrix());
            m1 = m1.mul(stack.last().pose());
            m1 = m1.translate(0.5f, 0.5f, 0.5f);
            float rotationMultiplier = 0;

            if (facing == Direction.WEST) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1.0f, 0, 90f));
                rotationMultiplier = 1;
            }
            if (facing == Direction.EAST) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1.0f, 0, 270f));
                rotationMultiplier = -1;
            }
            if (facing == Direction.SOUTH) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1.0f, 0, 180f));
                rotationMultiplier = -1;
            }
            if (facing == Direction.NORTH) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f, 1.0f, 0, 0f));
                rotationMultiplier = 1;
            }


            LIGHTMAP.setupRenderState();
            LEQUAL_DEPTH_TEST.setupRenderState();
            NO_TRANSPARENCY.setupRenderState();

            RenderSystem.setShader(Static::getEntitySolidDynamicNormalDynamicLightShader);
            ShaderInstance shader = RenderSystem.getShader();
            RenderSystem.setShaderTexture(0, tex);

            Matrix4f m2 = new Matrix4f(m1);
            m2 = m2.translate(0,0,-0.2f);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 0f, rotationMultiplier, (float) (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS * partialTick)));
            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
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