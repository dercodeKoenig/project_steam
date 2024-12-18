package ProjectSteam.Blocks.Mechanics.CrankShaft;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderBigWoodenCrankShaft extends RenderBigCrankShaftBase {
    public RenderBigWoodenCrankShaft(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}