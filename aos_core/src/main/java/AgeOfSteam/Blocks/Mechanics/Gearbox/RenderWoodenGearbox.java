package AgeOfSteam.Blocks.Mechanics.Gearbox;

import AgeOfSteam.Main;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenGearbox extends RenderGearboxBase{
    public RenderWoodenGearbox(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/block/planks.png"));
    }
}
