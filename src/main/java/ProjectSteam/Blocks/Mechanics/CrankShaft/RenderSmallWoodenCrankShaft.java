package ProjectSteam.Blocks.Mechanics.CrankShaft;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderSmallWoodenCrankShaft extends RenderSmallCrankShaftBase {
    public RenderSmallWoodenCrankShaft(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}