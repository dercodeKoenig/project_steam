package ProjectSteamCrafting.Sieve;

import ARLib.obj.Face;
import ARLib.obj.ModelFormatException;
import ARLib.obj.WavefrontObject;
import ProjectSteam.Blocks.Mechanics.CrankShaft.EntityCrankShaftBase;
import ProjectSteam.Static;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.nio.ByteBuffer;

import static ProjectSteam.Static.*;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderSieve implements BlockEntityRenderer<EntitySieve> {

    static WavefrontObject model;
    static ResourceLocation tex = ResourceLocation.fromNamespaceAndPath("projectsteam", "textures/block/planks.png");

    static     VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh;
    static VertexBuffer vertexBuffer2= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh2;
    static VertexBuffer vertexBuffer3= new VertexBuffer(VertexBuffer.Usage.STATIC);
    static MeshData mesh3;

    static {
        try {
            model = new WavefrontObject(ResourceLocation.fromNamespaceAndPath("projectsteam_crafting", "objmodels/mechanical_sieve.obj"));
        } catch (ModelFormatException ex) {
            throw new RuntimeException(ex);
        }


        ByteBufferBuilder byteBuffer;
        BufferBuilder b;


        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("sieve.001").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh2 = b.build();
        vertexBuffer2.bind();
        vertexBuffer2.upload(mesh2);
        byteBuffer.close();


        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("sieve").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh = b.build();
        vertexBuffer.bind();
        vertexBuffer.upload(mesh);
        byteBuffer.close();


        byteBuffer = new ByteBufferBuilder(1024);
        b = new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
        for (Face i : model.groupObjects.get("arm").faces) {
            i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
        }
        mesh3 = b.build();
        vertexBuffer3.bind();
        vertexBuffer3.upload(mesh3);
        byteBuffer.close();
    }

    void updateRenderData(EntitySieve tile){
        if(tile.myInputs.getItem() instanceof BlockItem bi) {
            Block block = bi.getBlock();
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            TextureAtlasSprite blockTexture = blockRenderer.getBlockModel(block.defaultBlockState()).getParticleIcon(ModelData.EMPTY);
            tile.inputStackTexture = blockTexture.atlasLocation();
            model.scaleUV("sieve.001", blockTexture.getU0(), blockTexture.getV0(), blockTexture.getU1(), blockTexture.getV1());
            ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
            BufferBuilder b= new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
            for (Face i : model.groupObjects.get("sieve.001").faces) {
                i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
            }
            model.scaleUV("sieve.001",0,0,1,1);
            MeshData mesh = b.build();
            tile.myInputRendererBuffer.bind();
            tile.myInputRendererBuffer.upload(mesh);
            byteBuffer.close();
        }

        if(tile.myHopperInputs.getItem() instanceof BlockItem bi) {
            Block block = bi.getBlock();
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
            TextureAtlasSprite blockTexture = blockRenderer.getBlockModel(block.defaultBlockState()).getParticleIcon(ModelData.EMPTY);
            tile.hopperStackTexture = blockTexture.atlasLocation();
            model.scaleUV("hopper_plane", blockTexture.getU0(), blockTexture.getV0(), blockTexture.getU1(), blockTexture.getV1());
            ByteBufferBuilder byteBuffer = new ByteBufferBuilder(1024);
            BufferBuilder b= new BufferBuilder(byteBuffer, VertexFormat.Mode.TRIANGLES, POSITION_COLOR_TEXTURE_NORMAL_LIGHT);
            for (Face i : model.groupObjects.get("hopper_plane").faces) {
                i.addFaceForRender(new PoseStack(), b, 0, 0, 0xffffffff);
            }
            model.scaleUV("hopper_plane",0,0,1,1);
            MeshData mesh = b.build();
            tile.myHopperInputRendererBuffer.bind();
            tile.myHopperInputRendererBuffer.upload(mesh);
            byteBuffer.close();
        }
    }

    public RenderSieve(BlockEntityRendererProvider.Context c) {
        super();
    }

    @Override
    public void render(EntitySieve tile, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(tile.isRemoved())return;
        BlockState state = tile.getBlockState();
        if (state.getBlock() instanceof BlockSieve) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            Matrix4f m1 = new Matrix4f(RenderSystem.getModelViewMatrix());
            m1 = m1.mul(stack.last().pose());
            m1 = m1.translate(0.5f, 0.5f, 0.5f);

            if(facing == Direction.WEST){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 0f));
            }
            if(facing == Direction.EAST){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 180f));

            }
            if(facing == Direction.SOUTH){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 90f));
            }
            if(facing == Direction.NORTH){
                m1 = m1.rotate(new Quaternionf().fromAxisAngleDeg(0f,1.0f, 0, 270f));
            }



            LIGHTMAP.setupRenderState();
            LEQUAL_DEPTH_TEST.setupRenderState();
            NO_TRANSPARENCY.setupRenderState();

            RenderSystem.setShader(Static::getEntitySolidDynamicNormalDynamicLightShader);
            ShaderInstance shader = RenderSystem.getShader();
            RenderSystem.setShaderTexture(0, tex);

            Matrix4f m2 = new Matrix4f(m1);
            float crankshaftR = 0.07f;
            double targetHeight = 0.03;
            double armLength = 0.62;
            float XRotationMultiplier =
                    (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1)
                    *(facing.getAxis() == Direction.Axis.X ? 1 : -1);

            double a = tile.myMechanicalBlock.currentRotation / 180 * Math.PI + tile.myMechanicalBlock.internalVelocity/TPS*partialTick;
            float translationX =  -1f+(float) Math.sin(a) * crankshaftR * XRotationMultiplier;
            float translationY =  (float) Math.cos(a) * crankshaftR;
            double b = Math.asin((translationY-targetHeight) / armLength);
            m2.translate(translationX,translationY,-0.04f);
            m2.rotate(new Quaternionf().fromAxisAngleDeg(0f,0f,1f,-(float)b*180f/(float)Math.PI));
            m2.rotate(new Quaternionf().fromAxisAngleDeg(0f,0f,1f,180f)           );

            BlockEntity t = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(facing));
            if (t instanceof EntityCrankShaftBase cs) {
                shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
                shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
                shader.apply();
                vertexBuffer3.bind();
                vertexBuffer3.draw();
            }

            m2 = new Matrix4f(m1);
            float sieveTargetX = 0.4f+(float) (translationX+Math.cos(b)*armLength);
            m2.translate(sieveTargetX,0,0);

            shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
            shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
            shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
            shader.apply();
            vertexBuffer.bind();
            vertexBuffer.draw();


            if(tile.myMesh.getItem() instanceof IMesh mesh) {
                RenderSystem.setShaderTexture(0, mesh.getTexture());

                shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
                shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
                shader.apply();

                vertexBuffer2.bind();
                vertexBuffer2.draw();

                Matrix4f m3 = new Matrix4f(m2);
                m3.translate(0, -0.01f, 0);
                m3.rotate(new Quaternionf().fromAxisAngleDeg(1f, 0f, 0f, 180f));
                shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m3, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                shader.getUniform("NormalMatrix").set((new Matrix3f(m3)).invert().transpose());
                shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
                shader.apply();
                vertexBuffer2.bind();
                vertexBuffer2.draw();

                if (tile.myInputs.getItem() instanceof BlockItem bi) {
                    if (!tile.lastInputStackForRender.getItem().equals(tile.myInputs.getItem())) {
                        updateRenderData(tile);
                        tile.lastInputStackForRender = tile.myInputs.copy();
                    }
                    RenderSystem.setShaderTexture(0, tile.inputStackTexture);
                    float maxTranslateUp = 0.065f;
                    float translateUp = (float) (((float)tile.myInputs.getCount()-tile.currentProgress/tile.client_syncedCurrentRecipeTime) / tile.maxStackSizeForSieve * maxTranslateUp+0.001f);
                    m2.translate(0, translateUp, 0);
                    shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                    shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
                    shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
                    shader.apply();
                    tile.myInputRendererBuffer.bind();
                    tile.myInputRendererBuffer.draw();

                    m3.translate(0, -0.01f, 0);
                    shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m3, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                    shader.getUniform("NormalMatrix").set((new Matrix3f(m3)).invert().transpose());
                    shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
                    shader.apply();
                    tile.myInputRendererBuffer.bind();
                    tile.myInputRendererBuffer.draw();
                }

                if (tile.myHopperInputs.getItem() instanceof BlockItem bi) {

                    if (!tile.lastHopperInputStackForRender.getItem().equals(tile.myHopperInputs.getItem())) {
                        updateRenderData(tile);
                        tile.lastHopperInputStackForRender = tile.myHopperInputs.copy();
                    }
                    RenderSystem.setShaderTexture(0, tile.hopperStackTexture);
                    float baseOffset = 0.37f;
                    float maxTranslateUp = 0.49f-baseOffset;
                    float translateUp = (float) (((float)tile.myHopperInputs.getCount()) / tile.maxStackSizeForSieveHopper * maxTranslateUp+baseOffset);
                    m2 = new Matrix4f(m1);
                    m2.translate(0, translateUp, 0);
                    shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLES, m2, RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
                    shader.getUniform("NormalMatrix").set((new Matrix3f(m2)).invert().transpose());
                    shader.getUniform("UV2").set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
                    shader.apply();
                    tile.myHopperInputRendererBuffer.bind();
                    tile.myHopperInputRendererBuffer.draw();
                }
            }



            shader.clear();
            VertexBuffer.unbind();

            LIGHTMAP.clearRenderState();
            LEQUAL_DEPTH_TEST.clearRenderState();
            NO_TRANSPARENCY.clearRenderState();
        }
    }
}