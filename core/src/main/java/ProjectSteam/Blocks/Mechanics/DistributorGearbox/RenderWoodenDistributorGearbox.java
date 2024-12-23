package ProjectSteam.Blocks.Mechanics.DistributorGearbox;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenDistributorGearbox extends RenderDistributorGearboxBase{
    public RenderWoodenDistributorGearbox(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}
