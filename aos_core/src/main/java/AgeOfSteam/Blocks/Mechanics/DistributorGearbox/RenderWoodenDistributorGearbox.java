package AgeOfSteam.Blocks.Mechanics.DistributorGearbox;

import AgeOfSteam.Main;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenDistributorGearbox extends RenderDistributorGearboxBase{
    public RenderWoodenDistributorGearbox(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/block/planks.png"));
    }
}
