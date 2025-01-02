package Farms.CropFarm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class RenderCropFarmBounds implements BlockEntityRenderer<EntityCropFarm> {

    public RenderCropFarmBounds(BlockEntityRendererProvider.Context c) {
        super();
    }

    @Override
    public AABB getRenderBoundingBox(EntityCropFarm tile) {
        return new AABB(tile.getBlockPos()).inflate(tile.maxSize);
    }

    @Override
    public void render(EntityCropFarm entityCropFarm, float v, PoseStack stack, MultiBufferSource multiBufferSource, int i, int i1) {
        // Create a VertexConsumer for LINES render type
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LIGHTNING);

        // Define the RGBA color (e.g., red with 50% transparency)
        float r = 1.0f;
        float g = 0.0f;
        float b = 0.0f;
        float a = 0.5f;

        BlockPos lowerEnd = entityCropFarm.pmin.subtract(entityCropFarm.getBlockPos());
        BlockPos upperEnd = entityCropFarm.pmax.subtract(entityCropFarm.getBlockPos());
        
        // lower edges

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);


        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);


        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);


        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY() + 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), 0.01f+lowerEnd.getY(), 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        
        
        
        // upper edges

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ() + 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);


        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1 - 0.1f)).setColor(0.8f, 0.8f, 0.8f, 0.5f);


        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1 - 0.1f, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(-0.01f+upperEnd.getX() + 1, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);


        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX() + 0.1f, -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);

        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1 - 0.1f, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY() + 1, -0.01f+upperEnd.getZ() + 1)).setColor(0.8f, 0.8f, 0.8f, 0.5f);
        vertexConsumer.addVertex(stack.last(), new Vector3f(0.01f+lowerEnd.getX(), -0.01f+upperEnd.getY()+1, 0.01f+lowerEnd.getZ())).setColor(0.8f, 0.8f, 0.8f, 0.5f);


    }
}
