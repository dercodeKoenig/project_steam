package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class GuiModuleBase {

    protected int x;
    protected int y;
    protected int id;

    protected boolean isEnabled = true;

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

    public void setIsEnabled(boolean isEnabled) {
        boolean needsUpdate = isEnabled != this.isEnabled;
        this.isEnabled = isEnabled;
        if (needsUpdate) {
            CompoundTag tag = new CompoundTag();
            server_writeDataToSyncToClient(tag);
            this.guiHandler. sendToTrackingClients(tag);
        }
    }

    public void client_setLocation(int x, int y){
        this.x = x;
        this.y = y;
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
                setIsEnabled(myTag.getBoolean("updateIsEnabled"));
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
