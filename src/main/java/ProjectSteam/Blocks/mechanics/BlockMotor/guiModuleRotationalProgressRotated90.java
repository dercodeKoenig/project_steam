package ProjectSteam.Blocks.mechanics.BlockMotor;

import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

public class guiModuleRotationalProgressRotated90 extends guiModuleRotationalProgress {

    public guiModuleRotationalProgressRotated90(int id, IGuiHandler guiHandler, int x, int y) {
        super(id, guiHandler, x, y);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(isEnabled){

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate( onGuiX,onGuiY,0);
            guiGraphics.pose().mulPose (new Quaternionf().fromAxisAngleDeg(0,0,1,-90));
            guiGraphics.pose().translate( -onGuiX,-onGuiY,0);


            guiGraphics.blit(this.bg, this.onGuiX, this.onGuiY, bg_w, bg_h, 0.0F, 0.0F, bg_tw, bg_th, bg_tw, bg_th);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate( onGuiX+bg_w/2,onGuiY+bg_h*0.85,0);

            guiGraphics.pose().mulPose (new Quaternionf().fromAxisAngleDeg(0,0,1,-90));
            guiGraphics.pose().mulPose (new Quaternionf().fromAxisAngleDeg(0f,0f,1f, (float) (180f*progress)));

            guiGraphics.blit(this.arrow, -a_w/2, (int)(-a_h*1.15), a_w, a_h, 0.0F, 0.0F, a_tw, a_th, a_tw, a_th);

            guiGraphics.pose().popPose();

            guiGraphics.pose().popPose();

        }
    }
}
