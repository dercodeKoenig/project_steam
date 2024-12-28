package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class guiModuleScrollContainer extends GuiModuleBase {

    protected int w;
    protected int h;
    int left, top;
    public double top_extra_offset = 0;
    int backgroundColor;
    public List<GuiModuleBase> modules;

    public guiModuleScrollContainer(List<GuiModuleBase> modules, int backgroundColor, IGuiHandler guiHandler, int x, int y, int w, int h) {
        super(-1, guiHandler, x, y);
        this.w = w;
        this.h = h;
        this.modules = modules;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void client_onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
if(client_isMouseOver(mouseX,mouseY,onGuiX,onGuiY,w,h)) {
    this.top_extra_offset += scrollY * 10;
}
        this.top_extra_offset = Math.min(0,this.top_extra_offset);
        int maxY = 0;
        for (GuiModuleBase i : modules) {
            maxY = Math.max(maxY,i.y);
        }
        this.top_extra_offset = Math.max(-maxY,this.top_extra_offset);
        client_setGuiOffset(left, top);
    }

    public void client_setGuiOffset(int left, int top) {
        this.left = left;
        this.top = top;
        onGuiX = x + left;
        onGuiY = y + top;
        for (GuiModuleBase i : modules) {
            i.client_setGuiOffset(left+x, (int) (top +y+ top_extra_offset));
        }
    }


    public void client_onMouseCLick(double x, double y, int button) {
        for (GuiModuleBase i : modules) {
            i.client_onMouseCLick(x, y, button);
        }
    }

    public void server_readNetworkData(CompoundTag tag) {
        for (GuiModuleBase i : modules) {
            i.server_readNetworkData(tag);
        }
    }

    public void client_handleDataSyncedToClient(CompoundTag tag) {
        for (GuiModuleBase i : modules) {
            i.client_handleDataSyncedToClient(tag);
        }
        super.client_handleDataSyncedToClient(tag);
    }

    public void serverTick() {
        for (GuiModuleBase i : modules) {
            i.serverTick();
        }
    }

    public void server_writeDataToSyncToClient(CompoundTag tag) {
        for (GuiModuleBase i : modules) {
            i.server_writeDataToSyncToClient(tag);
        }
        super.server_writeDataToSyncToClient(tag);
    }

    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (isEnabled) {
            guiGraphics.fill(onGuiX, onGuiY, onGuiX + w, onGuiY + h, backgroundColor);
            guiGraphics.enableScissor(onGuiX, onGuiY, onGuiX + w, onGuiY + h);
            for (GuiModuleBase i : modules) {
                i.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            guiGraphics.disableScissor();
        }
    }

    protected String getMyTagKey() {
        return "moduleTag" + this.id;
    }
}
