package ARLib.gui;

import ARLib.gui.modules.GuiModuleBase;
import ARLib.network.PacketBlockEntity;
import ARLib.network.PacketPlayerMainHand;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;


// this guihandler is very simplified version of the guiHandlerBlockEntity.
// a guiHandler for items does not support networking because you can only have one guiHandler for the Item class
// you will rely on nbt of the itemstack and the client tick to manually update gui information
// you do not call serverTick or readServer or readClient on this guiHandler
public class GuiHandlerMainHandItem implements IGuiHandler {

    List<GuiModuleBase> modules;
    IguiOnClientTick clientTicker;

    public GuiHandlerMainHandItem(IguiOnClientTick clientTicker) {
        modules = new ArrayList<>();
        this.clientTicker = clientTicker;
    }

    public void registerModule(GuiModuleBase guiModule) {
        modules.add(guiModule);
    }

    @Override
    public List<GuiModuleBase> getModules() {
        return modules;
    }

    @Override
    // this will never be called for this guiHandler
    public Map<UUID, Integer> getPlayersTrackingGui(){
        return Map.of();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public CustomPacketPayload getNetworkPacketForTag_client(CompoundTag tag) {
        return new PacketPlayerMainHand(Minecraft.getInstance().player.getUUID(),tag);
    }
    @Override
    public CustomPacketPayload getNetworkPacketForTag_server(CompoundTag tag) {
        return null;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void openGui(int w, int h) {
        // fix for not syncing in creative mode
        Minecraft.getInstance().player.inventoryMenu.setCarried(ItemStack.EMPTY);
        Minecraft.getInstance().setScreen(new ModularScreen(this, w, h));
    }
    @Override
    public void onGuiClientTick() {
        clientTicker.onGuiClientTick();
        // use this to update the gui with nbt data from the current itemstack in your hand
    }

}
