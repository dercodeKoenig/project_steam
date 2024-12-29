package BetterPipes;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class fluidRenderData {

    TextureAtlasSprite spriteFLowing;
    TextureAtlasSprite spriteStill;
    int color;

    public fluidRenderData() {
        if(FMLEnvironment.dist == Dist.CLIENT) {
            updateSprites(Fluids.WATER);
        }
    }

    public void updateSprites(Fluid f) {
        if (f == Fluids.EMPTY) f = Fluids.WATER;
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(f);
        color = extensions.getTintColor();
        ResourceLocation fluidtextureStill = extensions.getStillTexture();
        spriteStill = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidtextureStill);
        ResourceLocation fluidtextureFlowing = extensions.getFlowingTexture();
        spriteFLowing = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidtextureFlowing);
    }
}


