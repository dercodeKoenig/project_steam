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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;


// a guiHandler for items does not support networking because you can only have one guiHandler for the Item class
// you will rely on nbt of the itemstack and the client tick to manually update gui information
// you do not readServer or readClient on this guiHandler
public class GuiHandlerMainHandItem implements IGuiHandler {

    List<GuiModuleBase> modules;

    public GuiHandlerMainHandItem() {
        modules = new ArrayList<>();
    }

    @Override
    public List<GuiModuleBase> getModules() {
        return modules;
    }

    @Override
    public void broadcastUpdate(CompoundTag tag) {
        // not used, use client tick and read itemstack nbt
    }
    @Override
    public void onGuiClientTick() {
        // use this to update the gui with nbt data from the current itemstack in your hand
    }

    @Override
    public void sendToServer(CompoundTag tag) {
        PacketDistributor.sendToServer(PacketPlayerMainHand.packetToServer(Minecraft.getInstance().player.getUUID(),tag));
    }


    @OnlyIn(Dist.CLIENT)
    public void openGui(int w, int h, boolean renderBackground) {
        // fix for not syncing in creative mode
        if(Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.inventoryMenu.setCarried(ItemStack.EMPTY);

        Minecraft.getInstance().setScreen(new ModularScreen(this, w, h, renderBackground));
    }

}
