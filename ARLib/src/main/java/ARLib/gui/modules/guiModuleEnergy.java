package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class guiModuleEnergy extends guiModuleVerticalProgressBar {

    final IEnergyStorage energyStorage;

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

            setHoverInfoAndSync(energy + "/" + maxEnergy + "RF");
            setProgressAndSync((float)energy/maxEnergy);
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
}
