package ProjectSteam.Blocks.mechanics.BlockMotor;

import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.joml.Quaternionf;

public class guiModuleRotationalProgress extends GuiModuleBase {

    ResourceLocation bg = ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/simple_scale_round.png");
    ResourceLocation arrow = ResourceLocation.fromNamespaceAndPath("arlib", "textures/gui/red_arrow.png");

    int bg_w = 50;
    int bg_h =  30;

    int bg_tw =417;
    int bg_th =248;

    int a_w = 6;
    int a_h = 20;

    int a_tw =39;
    int a_th =157;

    public double progress = 0;

    public guiModuleRotationalProgress(int id, IGuiHandler guiHandler, int x, int y) {
        super(id, guiHandler, x, y);
    }



    public void setProgress(double progress) {
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
        tag.put(this.getMyTagKey(), myTag);
        super.server_writeDataToSyncToClient(tag);
    }

    public void client_handleDataSyncedToClient(CompoundTag tag) {
        if (tag.contains(this.getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(this.getMyTagKey());
            if (myTag.contains("progress")) {
                this.progress = myTag.getDouble("progress");
            }
        }
        super.client_handleDataSyncedToClient(tag);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(isEnabled){


            guiGraphics.blit(this.bg, this.onGuiX, this.onGuiY, bg_w, bg_h, 0.0F, 0.0F, bg_tw, bg_th, bg_tw, bg_th);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate( onGuiX+bg_w/2,onGuiY+bg_h*0.85,0);

            guiGraphics.pose().mulPose (new Quaternionf().fromAxisAngleDeg(0,0,1,-90));
            guiGraphics.pose().mulPose (new Quaternionf().fromAxisAngleDeg(0f,0f,1f, (float) (180f*progress)));

            guiGraphics.blit(this.arrow, -a_w/2, (int)(-a_h*1.15), a_w, a_h, 0.0F, 0.0F, a_tw, a_th, a_tw, a_th);

            guiGraphics.pose().popPose();

        }
    }
}
