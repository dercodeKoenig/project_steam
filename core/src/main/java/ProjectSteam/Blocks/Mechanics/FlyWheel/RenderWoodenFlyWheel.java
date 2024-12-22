package ProjectSteam.Blocks.Mechanics.FlyWheel;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderWoodenFlyWheel extends RenderFlyWheelBase {
    public RenderWoodenFlyWheel(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
}