package AgeOfSteam.Blocks.Mechanics.TJunction;

import AgeOfSteam.Main;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenTJunction extends RenderTJunctionBase{
    public RenderWoodenTJunction(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/block/planks.png"));
    }
}
