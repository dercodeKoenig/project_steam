package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class guiModuleEnergy extends GuiModuleBase {

    public ResourceLocation energy_bar_background = ResourceLocation.fromNamespaceAndPath("arlib","textures/gui/gui_vertical_progress_bar_background.png");
    public ResourceLocation energy_bar = ResourceLocation.fromNamespaceAndPath("arlib","textures/gui/gui_vertical_progress_bar.png");


    final IEnergyStorage energyStorage;

    // textures width and height
    final int energy_bar_background_tw = 14;
    final int energy_bar_background_th = 54;
    final int borderpx = 1;
    final int energy_bar_tw = 6;
    final int energy_bar_th = 44;

    // full size of the bar when rendering (background)
    final int w = 14;
    final int h = 54;

    // size of the bar
    final int bar_size_w = w - borderpx * 2;
    final int bar_size_h = h - borderpx * 2;

    final int energy_bar_offset_x = borderpx;
    final int energy_bar_offset_y = borderpx;


    public int maxEnergy;
    public int energy;
    int last_energy;
    int last_maxEnergy;

    @Override
    public void server_writeDataToSyncToClient(CompoundTag tag){
        CompoundTag myTag = new CompoundTag();
        myTag.putInt("energy",energyStorage.getEnergyStored());
        myTag.putLong("time",System.currentTimeMillis());
        myTag.putInt("maxEnergy", energyStorage.getMaxEnergyStored());
        tag.put(getMyTagKey(),myTag);

        super.server_writeDataToSyncToClient(tag);
    }

    int last_update = 0;
    @Override
    public void serverTick(){
        last_update+=1;
        energy = energyStorage.getEnergyStored();
        // update every x ticks
        if ((energy != last_energy || last_maxEnergy != energyStorage.getMaxEnergyStored())&& last_update > 2){
            last_update = 0;
            last_energy = energy;
            last_maxEnergy = energyStorage.getMaxEnergyStored();
            CompoundTag tag = new CompoundTag();
            server_writeDataToSyncToClient(tag);
            this.guiHandler. sendToTrackingClients(tag);
        }
    }

    long last_packet_time = 0; // sometimes older packets can come in after newer ones. so this will make sure only the most recent data will be used
    @Override
    public void client_handleDataSyncedToClient(CompoundTag tag){
        if(tag.contains(getMyTagKey())){
            CompoundTag myTag = tag.getCompound(getMyTagKey());
            if(myTag.contains("time") && myTag.contains("energy") && myTag.contains("maxEnergy")) {
                long update_time = myTag.getLong("time");
                if (update_time > last_packet_time) {
                    last_packet_time = update_time;
                    this.energy = myTag.getInt("energy");
                    this.maxEnergy = myTag.getInt("maxEnergy");
                }
            }
        }
        super.client_handleDataSyncedToClient(tag);
    }

    public guiModuleEnergy(int id, IEnergyStorage energyStorage, IGuiHandler guiHandler, int x, int y){
        super(id,guiHandler,x,y);
        this.energyStorage = energyStorage;
    }

    @Override
    public  void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if(isEnabled) {

            double relative_energy_level = (double) energy / maxEnergy;
            int v_offset = (int) ((1 - relative_energy_level) * bar_size_h);
            int v_offset_tex = (int) ((1 - relative_energy_level) * energy_bar_th);

            guiGraphics.blit(energy_bar_background, onGuiX, onGuiY, 0, 0, w, h, energy_bar_background_tw, energy_bar_background_th);

            //guiGraphics.blit(energy_bar,x+left+energy_bar_offset_x,y+top+energy_bar_offset_y,0,v_offset,energy_bar_tw, energy_bar_th-v_offset);

            guiGraphics.blit(
                    energy_bar,
                    onGuiX + energy_bar_offset_x, onGuiY + v_offset + energy_bar_offset_y,
                    bar_size_w, bar_size_h - v_offset,
                    (float) 0, (float) 0 + v_offset_tex,
                    energy_bar_tw, energy_bar_th - v_offset_tex,
                    energy_bar_tw, energy_bar_th
            );
            if (client_isMouseOver(mouseX, mouseY, onGuiX, onGuiY, w, h)) {
                String info = energy + "/" + maxEnergy + "RF";
                guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal(info), mouseX, mouseY);
            }
        }
    }
}
