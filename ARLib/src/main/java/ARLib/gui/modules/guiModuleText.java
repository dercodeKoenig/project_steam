package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Objects;

public class guiModuleText extends GuiModuleBase {

    public String text;
    public int color;
    public boolean makeShadow;

    public guiModuleText(int id, String text, IGuiHandler guiHandler, int x, int y, int color, boolean makeShadow) {
        super(id, guiHandler, x, y);
        this.text = text;
        this.color = color;
        this.makeShadow = makeShadow;
    }

    public void setText(String text) {
        boolean needsUpdate = !Objects.equals(this.text, text);
        this.text = text;
        if (needsUpdate) {
            CompoundTag tag = new CompoundTag();
            server_writeDataToSyncToClient(tag);
            this.guiHandler.sendToTrackingClients(tag);
        }
    }

    public void setColor(int color) {
        boolean needsUpdate = this.color != color;
        this.color = color;
        if (needsUpdate) {
            CompoundTag tag = new CompoundTag();
            server_writeDataToSyncToClient(tag);
            this.guiHandler.sendToTrackingClients(tag);
        }
    }

    @Override
    public void server_writeDataToSyncToClient(CompoundTag tag) {
        CompoundTag myTag = new CompoundTag();
        myTag.putString("text", this.text);
        myTag.putInt("color", this.color);
        tag.put(getMyTagKey(), myTag);

        super.server_writeDataToSyncToClient(tag);
    }

    @Override
    public void client_handleDataSyncedToClient(CompoundTag tag) {
        if (tag.contains(getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(getMyTagKey());
            if (myTag.contains("text")) {
                this.text = myTag.getString("text");
            }
            if (myTag.contains("color")) {
                this.color = myTag.getInt("color");
            }
        }
        super.client_handleDataSyncedToClient(tag);
    }


    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (isEnabled) {
            guiGraphics.drawString(Minecraft.getInstance().font, text, onGuiX, onGuiY, color, makeShadow);
        }
    }
}
