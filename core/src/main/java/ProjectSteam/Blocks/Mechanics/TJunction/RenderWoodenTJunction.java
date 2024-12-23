package ProjectSteam.Blocks.Mechanics.TJunction;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenTJunction extends RenderTJunctionBase{
    public RenderWoodenTJunction(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}
