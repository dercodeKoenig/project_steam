package ProjectSteam.Blocks.Mechanics.FlyWheel;

import ProjectSteamAW2Generators.WindMill.EntityWindMillGenerator;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class RenderWoodenFlyWheelLarge extends RenderLargeFlyWheelBase {
    public RenderWoodenFlyWheelLarge(BlockEntityRendererProvider.Context c) {
        super(c, ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png"));
    }
    @Override
    public AABB getRenderBoundingBox(EntityFlyWheelBase tile) {
        return new AABB(tile.getBlockPos()).inflate(1);
    }

}