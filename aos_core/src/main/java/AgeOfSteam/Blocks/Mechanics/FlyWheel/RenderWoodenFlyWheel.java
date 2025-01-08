package AgeOfSteam.Blocks.Mechanics.FlyWheel;

import AgeOfSteam.Main;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenFlyWheel extends RenderFlyWheelBase {
    public RenderWoodenFlyWheel(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/block/planks.png"));
    }
}