package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;

public class GuiModuleBase {

    public int x;
    public int y;
    public int id;

    public boolean isEnabled = true;

    protected int onGuiX;
    protected int onGuiY;

    protected IGuiHandler guiHandler;
    public GuiModuleBase(int id, IGuiHandler guiHandler, int x, int y) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.guiHandler = guiHandler;
        this.onGuiX = 0;
        this.onGuiY = 0;
    }

    public void broadcastModuleUpdate(){
        CompoundTag tag = new CompoundTag();
        server_writeDataToSyncToClient(tag);
        this.guiHandler.broadcastUpdate(tag);
    }

    public void setIsEnabledAndBroadcastUpdate(boolean isEnabled) {
        boolean needsUpdate = isEnabled != this.isEnabled;
        this.isEnabled = isEnabled;
        if (needsUpdate) {
            broadcastModuleUpdate();
        }
    }

    public void client_setGuiOffset(int left, int top){
        onGuiX = x+left;
        onGuiY = y+top;
    }

    public boolean client_isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
        if(!isEnabled)return false;
        return mouseX >= x &&
                mouseX <= x + w &&
                mouseY >= y &&
                mouseY <= y + h;
    }

    public void client_onMouseScrolled(double mouseX,double  mouseY, double scrollX,double scrollY){

    }

    public void client_onMouseCLick(double x, double y, int button) {

    }

    public void server_readNetworkData(CompoundTag tag) {

    }

    public void client_handleDataSyncedToClient(CompoundTag tag) {
        if(tag.contains(getMySuperTagKey())) {
            CompoundTag myTag = tag.getCompound(getMySuperTagKey());
            if(myTag.contains("updateIsEnabled")){
                isEnabled = (myTag.getBoolean("updateIsEnabled"));
            }
        }
    }

    public void serverTick() {

    }

    public void server_writeDataToSyncToClient(CompoundTag tag){
        CompoundTag myTag = new CompoundTag();
        myTag.putBoolean("updateIsEnabled", isEnabled);
        tag.put(getMySuperTagKey(),myTag);
    }

    public  void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

    }

    protected String getMyTagKey(){
        return "moduleTag"+this.id;
    }
    private String getMySuperTagKey(){
        return "moduleTag_s_"+this.id;
    }
}
