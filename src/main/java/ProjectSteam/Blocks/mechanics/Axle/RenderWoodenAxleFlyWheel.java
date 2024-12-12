package ProjectSteam.Blocks.mechanics.Axle;

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

public class RenderWoodenAxleFlyWheel extends RenderWoodenAxle{

    public WavefrontObject flywheel;

    public RenderWoodenAxleFlyWheel(BlockEntityRendererProvider.Context c) {
        super(c);
        try {
            flywheel = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam", "objmodels/flywheel.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }
    }

    void renderModelWithLight(EntityWoodenAxle tile, int light) {
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
}