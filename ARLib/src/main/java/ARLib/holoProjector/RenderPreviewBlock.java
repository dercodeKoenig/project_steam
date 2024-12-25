package ARLib.holoProjector;


import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.obj.ModelFormatException;
import ARLib.obj.WavefrontObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static ARLib.obj.GroupObject.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderPreviewBlock implements BlockEntityRenderer<EntityStructurePreviewBlock> {

    BlockRenderDispatcher br;
    public RenderPreviewBlock(BlockEntityRendererProvider.Context context) {
         br= context.getBlockRenderDispatcher();
    }

    // This method is called every frame in order to render the block entity. Parameters are:
    // - blockEntity:   The block entity instance being rendered. Uses the generic type passed to the super interface.
    // - partialTick:   The amount of time, in fractions of a tick (0.0 to 1.0), that has passed since the last tick.
    // - poseStack:     The pose stack to render to.
    // - bufferSource:  The buffer source to get vertex buffers from.
    // - packedLight:   The light value of the block entity.
    // - packedOverlay: The current overlay value of the block entity, usually OverlayTexture.NO_OVERLAY.
    @Override
    public void render(EntityStructurePreviewBlock tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
Block toRender = tile.getBlockToRender();
//System.out.println(toRender);
        stack.scale(0.5f,0.5f,0.5f);
        stack.translate(0.5f,0.5f,0.5f);

            stack.rotateAround(new Quaternionf().fromAxisAngleRad((Vector3fc) new Vector3f(0f, 1f, 0f), (float) Math.toRadians(System.currentTimeMillis() / 100 % 360)), 0.5f, 0.5f, 0.5f);
        
br.renderSingleBlock(toRender.defaultBlockState(),stack,bufferSource,packedLight,packedOverlay);


    }
}

