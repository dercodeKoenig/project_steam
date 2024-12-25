package ARLib.multiblockCore;


import ARLib.obj.WavefrontObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static ARLib.obj.GroupObject.POSITION_COLOR_OVERLAY_LIGHT_NORMAL;
import static ARLib.obj.GroupObject.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderMultiblockPlaceholder implements BlockEntityRenderer<EntityMultiblockPlaceholder> {
    public RenderMultiblockPlaceholder(BlockEntityRendererProvider.Context context) {

    }
    @Override
    public void render(EntityMultiblockPlaceholder tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(tile.renderBlock){
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    tile.replacedState,
                    stack,
                    bufferSource,
                    packedLight,
                    packedOverlay,
                    net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
                    null
            );
        }
    }
}

