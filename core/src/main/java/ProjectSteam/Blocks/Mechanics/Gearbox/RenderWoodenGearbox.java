package ProjectSteam.Blocks.Mechanics.Gearbox;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenGearbox extends RenderGearboxBase{
    public RenderWoodenGearbox(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}
