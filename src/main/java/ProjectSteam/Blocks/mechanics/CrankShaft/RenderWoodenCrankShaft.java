package ProjectSteam.Blocks.mechanics.CrankShaft;

import ProjectSteam.Blocks.mechanics.Axle.RenderAxleBase;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenCrankShaft extends RenderCrankShaftBase {
    public RenderWoodenCrankShaft(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}