package ResearchStation;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.network.INetworkTagReceiver;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import static ResearchStation.Registry.ENTITY_RESEARCH_STATION;


public class EntityResearchStation extends BlockEntity implements INetworkTagReceiver {


    public GuiHandlerBlockEntity guiHandler;
ItemStackHandler inventory;


    public EntityResearchStation(BlockPos pos, BlockState blockState) {
        super(ENTITY_RESEARCH_STATION.get(), pos, blockState);

        guiHandler = new GuiHandlerBlockEntity(this);
        inventory = new ItemStackHandler(1){
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };

        guiModuleItemHandlerSlot s1 = new guiModuleItemHandlerSlot(0,inventory,0,1,0,guiHandler,10,10);
        guiHandler.getModules().add(s1);
        for( GuiModuleBase i: guiModulePlayerInventorySlot.makePlayerHotbarModules(10,120,200,0,1,guiHandler)){
            guiHandler.getModules().add(i);
        }
        for( GuiModuleBase i: guiModulePlayerInventorySlot.makePlayerInventoryModules(10,50,100,0,1,guiHandler)){
            guiHandler.getModules().add(i);
        }
    }

    public void openGui(){
        if(level.isClientSide) {
            guiHandler.openGui(180, 150);
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void tick() {
        if(!level.isClientSide){
            guiHandler.serverTick();
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityResearchStation) t).tick();
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        guiHandler.readServer(compoundTag);
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        guiHandler.readClient(compoundTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries,tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

}