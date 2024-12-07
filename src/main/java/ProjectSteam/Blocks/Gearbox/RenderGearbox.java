package ProjectSteam.Blocks.Gearbox;

import ARLib.obj.Face;
import ARLib.obj.ModelFormatException;
import ARLib.obj.WavefrontObject;
import ProjectSteam.Blocks.Axle.BlockAxle;
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
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/rod_gearbox_connection.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }
    }


    public RenderGearbox(BlockEntityRendererProvider.Context c) {
        super();
    }


    void renderModelWithLight(EntityGearbox tile, int light) {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/rod_gearbox_connection.obj"));
        } catch (ModelFormatException e) {
            throw new RuntimeException(e);
        }

        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
        BufferBuilder b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("Cube").faces) {
            i.addFaceForRender(new PoseStack(), b, light, 0, 0xffffffff);
        }
        tile.mesh = b.build();
        tile.vertexBuffer.upload(tile.mesh);
        byteBuffer.close();
    }

    @Override
    public void render(EntityGearbox tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState axleState = tile.getLevel().getBlockState(tile.getBlockPos());
        if (axleState.getBlock() instanceof BlockGearbox) {
            Direction.Axis normalAxis = axleState.getValue(BlockGearbox.ROTATION_AXIS);

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
            if (normalAxis == Direction.Axis.Y) {
                // no rotation
            } else if (normalAxis == Direction.Axis.X) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0, 0, 1f, 90));
            } else if (normalAxis == Direction.Axis.Z) {
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0, 0, 1f, 90));
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(1f, 0, 0, 90));
            }


            for (int i = 0; i < 4; i++) {
                Matrix4f m2 = new Matrix4f(m1);
                m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 1, 0f, (float) 90 * i));

                if (i == 0)
                    m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f, (float) (tile.getMechanicalData().currentRotation + tile.getMechanicalData().internalVelocity * partialTick)));
                if (i == 1)
                    m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f, 14.7f - (float) (tile.getMechanicalData().currentRotation + tile.getMechanicalData().internalVelocity * partialTick)));
                if (i == 2)
                    m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f, (float) (tile.getMechanicalData().currentRotation + tile.getMechanicalData().internalVelocity * partialTick)));
                if (i == 3)
                    m2 = m2.rotate(new Quaternionf().fromAxisAngleDeg((float) 0, (float) 0, 1.0f, 14.7f - (float) (tile.getMechanicalData().currentRotation + tile.getMechanicalData().internalVelocity * partialTick)));

                shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                shader.getUniform("NormalMatrix").set(new Matrix3f(m2).invert().transpose());

                shader.apply();
                tile.vertexBuffer.draw();
                shader.clear();
            }
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}