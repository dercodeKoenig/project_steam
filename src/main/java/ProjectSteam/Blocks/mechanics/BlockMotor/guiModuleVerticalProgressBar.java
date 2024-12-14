package ProjectSteam.Blocks.mechanics.BlockMotor;

import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class guiModuleVerticalProgressBar extends GuiModuleBase {
    public ResourceLocation background = ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/gui_vertical_progress_bar_background.png");
    public ResourceLocation bar = ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/gui_vertical_progress_bar.png");

    final int background_tw = 14;
    final int background_th = 54;
    final int borderpx = 1;
    final int bar_tw = 6;
    final int bar_th = 44;
    final int w = 14;
    final int h = 54;
    final int bar_size_w = 12;
    final int bar_size_h = 52;
    final int bar_offset_x = 1;
    final int bar_offset_y = 1;

    public double progress;
    public String info = "";

    public guiModuleVerticalProgressBar(int id, IGuiHandler guiHandler, int x, int y) {
        super(id, guiHandler, x, y);
    }

public void setHoverInfo(String s){
        this.info = s;

        CompoundTag tag = new CompoundTag();
        this.server_writeDataToSyncToClient(tag);
        this.guiHandler.sendToTrackingClients(tag);

}

    public void setProgress(double progress) {
        progress = Math.max(0,Math.min(1,progress));
        boolean needsUpdate =progress != this.progress;
        this.progress = progress;
if(needsUpdate) {
    CompoundTag tag = new CompoundTag();
    this.server_writeDataToSyncToClient(tag);
    this.guiHandler.sendToTrackingClients(tag);
}
    }

    public void server_writeDataToSyncToClient(CompoundTag tag) {
        CompoundTag myTag = new CompoundTag();
        myTag.putDouble("progress", this.progress);
        if(this.info.isEmpty()){
            myTag.putBoolean("noInfo", true);
        }else {
            myTag.putString("info", this.info);
        }
        tag.put(this.getMyTagKey(), myTag);
        super.server_writeDataToSyncToClient(tag);
    }

    public void client_handleDataSyncedToClient(CompoundTag tag) {
        if (tag.contains(this.getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(this.getMyTagKey());
            if (myTag.contains("progress")) {
                this.progress = myTag.getDouble("progress");
            }
            if (myTag.contains("info")) {
                this.info = myTag.getString("info");
            }
            if (myTag.contains("noInfo")) {
                this.info = "";
            }
        }
        super.client_handleDataSyncedToClient(tag);
    }


    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isEnabled) {
            int v_offset = (int)(((double)1.0F - progress) * (double)52.0F);
            int v_offset_tex = (int)(((double)1.0F - progress) * (double)44.0F);
            guiGraphics.blit(this.background, this.onGuiX, this.onGuiY, 0.0F, 0.0F, 14, 54, 14, 54);
            guiGraphics.blit(this.bar, this.onGuiX + 1, this.onGuiY + v_offset + 1, 12, 52 - v_offset, 0.0F, 0.0F + (float)v_offset_tex, 6, 44 - v_offset_tex, 6, 44);
            if (this.client_isMouseOver((double)mouseX, (double)mouseY, this.onGuiX, this.onGuiY, w, h)) {
                guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal(info), mouseX, mouseY);
            }
        }

    }
}
