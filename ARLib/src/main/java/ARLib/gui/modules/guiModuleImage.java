package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class guiModuleImage extends GuiModuleBase {
    public int w, h;
    public int textureW, textureH;
    public ResourceLocation image;


    public guiModuleImage(IGuiHandler guiHandler, int x, int y, int w, int h, ResourceLocation image, int textureW, int textureH) {
        super(-1, guiHandler, x, y);
        this.w = w;
        this.h = h;
        this.image = image;
        this.textureW = textureW;
        this.textureH = textureH;
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if(isEnabled) {
            guiGraphics.blit(image, onGuiX, onGuiY, w, h, 0f, 0f, textureW, textureH, textureW, textureH);
        }
    }
}
