package ProjectSteamAW2Generators.WindMill;

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
import org.joml.Vector3f;

import static ProjectSteam.Static.*;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderWindMillGenerator implements BlockEntityRenderer<EntityWindMillGenerator> {

    static WavefrontObject model;
    static ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("projectsteam_aw2_generators", "textures/block/windmill_generator.png");

    static VertexBuffer vertexBuffer_wheel = new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh_wheel;
    static VertexBuffer vertexBuffer_axle = new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh_axle;


    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam_aw2_generators", "objmodels/windmill_generator.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }

        ByteBufferBuilder byteBuffer;
        BufferBuilder b;

        byteBuffer = new ByteBufferBuilder(4096);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("wheel").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh_wheel = b.build();
        vertexBuffer_wheel.bind();
        vertexBuffer_wheel.upload(mesh_wheel);
        byteBuffer.close();

        byteBuffer = new ByteBufferBuilder(4096);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("axle").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh_axle = b.build();
        vertexBuffer_axle.bind();
        vertexBuffer_axle.upload(mesh_axle);
        byteBuffer.close();

    }

    public static void updateWindmillMesh(EntityWindMillGenerator tile, int size) {
        if(size < 1)return;

        ByteBufferBuilder byteBuffer;
        BufferBuilder b;

        byteBuffer = new ByteBufferBuilder(4096);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);

        for (int p = 0; p < size; p++) {
            for (int n = 0; n < 4; n++) {
                for (Face i : model.groupObjects.get("blade").faces) {
                    model.resetTransformations("blade");
                    model.rotateWorldSpace("blade", new Vector3f(0, 0, 1), 90f * n);
                    model.translateWorldSpace("blade",new Vector3f(0, 1*p, 0));
                    model.applyTransformations("blade");
                    i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
                }
            }
        }

        tile.mesh = b.build();
        tile.vertexBuffer.bind();
        tile.vertexBuffer.upload(tile.mesh);
        byteBuffer.close();
    }


    public RenderWindMillGenerator(BlockEntityRendererProvider.Context c) {
        super();
    }

    public AABB getRenderBoundingBox(EntityWindMillGenerator tile) {
        return new AABB(tile.getBlockPos()).inflate(tile.size+2);
    }


    @Override
    public void render(EntityWindMillGenerator tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (tile.isRemoved()) return;
        BlockState state = tile.getBlockState();
        if (state.getBlock() instanceof BlockWindMillGenerator) {
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
            m2 = m2.translate(0,0,-0.11f);
            m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg(0f, 0f, rotationMultiplier, (float) (tile.myMechanicalBlock.currentRotation + rad_to_degree(tile.myMechanicalBlock.internalVelocity) / TPS * partialTick)));
            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();
            vertexBuffer_wheel.bind();
            vertexBuffer_wheel.draw();


            if(tile.getBlockState().getValue(BlockWindMillGenerator.STATE_MULTIBLOCK_FORMED)) {
                vertexBuffer_axle.bind();
                vertexBuffer_axle.draw();

                if(tile.size != tile.last_size_for_meshUpdate) {
                    updateWindmillMesh(tile, tile.size);
                    tile.last_size_for_meshUpdate = tile.size;
                }

                if(tile.size > 0) {
                    tile.vertexBuffer.bind();
                    tile.vertexBuffer.draw();
                }
            }

            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}