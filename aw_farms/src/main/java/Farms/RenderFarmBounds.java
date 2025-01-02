package Farms;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.Set;

public class RenderFarmBounds implements BlockEntityRenderer<EntityFarmBase> {

    public RenderFarmBounds(BlockEntityRendererProvider.Context c) {
        super();
    }

    @Override
    public AABB getRenderBoundingBox(EntityFarmBase tile) {
        return new AABB(tile.getBlockPos()).inflate(tile.maxSize);
    }

    @Override
    public void render(EntityFarmBase tile, float v, PoseStack stack, MultiBufferSource multiBufferSource, int i, int i1) {
        // Create a VertexConsumer for LINES render type
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.LIGHTNING);

        // Define the RGBA color (e.g., red with 50% transparency)
        float r = 1.0f;
        float g = 0.0f;
        float b = 0.0f;
        float a = 0.5f;

        BlockPos lowerEnd = tile.pmin.subtract(tile.getBlockPos());
        BlockPos upperEnd = tile.pmax.subtract(tile.getBlockPos());

        Set<BlockPos> blocked = tile.blackListAsBlockPos;
        for (BlockPos pos : blocked) {
            BlockPos target = pos.subtract(tile.getBlockPos());
            vertexConsumer.addVertex(stack.last(), new Vector3f(target.getX(), 0.01f + target.getY(), target.getZ() + 1)).setColor(0.8f, 0.0f, 0.0f, 0.5f);
            vertexConsumer.addVertex(stack.last(), new Vector3f(target.getX() + 1, 0.01f + target.getY(), target.getZ() + 1)).setColor(0.8f, 0.0f, 0.0f, 0.5f);
            vertexConsumer.addVertex(stack.last(), new Vector3f(target.getX() + 1, 0.01f + target.getY(), target.getZ())).setColor(0.8f, 0.0f, 0.0f, 0.5f);
            vertexConsumer.addVertex(stack.last(), new Vector3f(target.getX(), 0.01f + target.getY(), target.getZ())).setColor(0.8f, 0.0f, 0.0f, 0.5f);
        }
        
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
