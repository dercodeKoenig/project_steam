package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class guiModuleItemHandlerSlot extends guiModuleInventorySlotBase {

    IItemHandler itemHandler;
    int targetSlot;

    public ItemStack stack;
    ItemStack lastStack;

    @Override
    public ItemStack client_getItemStackToRender() {
        return stack;
    }


    @Override
    public void server_writeDataToSyncToClient(CompoundTag tag){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        // it can be null, this can run on client because they can use the same set method to update values
        // this is usually not bad because the list of players tracking the gui on client side is empty so no packet will be sent.
        // if server = null, this was probably called because the Base Module had something updated
        if(server != null) {
            CompoundTag myTag = new CompoundTag();
            RegistryAccess registryAccess = server.registryAccess();
            myTag.putBoolean("isEmpty", itemHandler.getStackInSlot(targetSlot).isEmpty());
            if (!itemHandler.getStackInSlot(targetSlot).isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                myTag.put("ItemStack", itemHandler.getStackInSlot(targetSlot).save(registryAccess, itemTag));
            }
            tag.put(getMyTagKey(), myTag);
        }

        super.server_writeDataToSyncToClient(tag);

    }
    @Override
    public void serverTick(){
        if (!ItemStack.isSameItemSameComponents(itemHandler.getStackInSlot(targetSlot),lastStack) || itemHandler.getStackInSlot(targetSlot).getCount() != lastStack.getCount()){
            CompoundTag tag = new CompoundTag();
            server_writeDataToSyncToClient(tag);
            this.guiHandler. sendToTrackingClients(tag);
            lastStack = itemHandler.getStackInSlot(targetSlot).copy();
        }
    }
    @Override
    public void client_handleDataSyncedToClient(CompoundTag tag) {
        if (tag.contains(getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(getMyTagKey());
            RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
            if (myTag.contains("ItemStack")) {
                this.stack = ItemStack.parse(registryAccess, myTag.getCompound("ItemStack")).orElse(ItemStack.EMPTY);
            } else {
                if (myTag.contains("isEmpty") && myTag.getBoolean("isEmpty"))
                    this.stack = ItemStack.EMPTY;
            }
        }
        super.client_handleDataSyncedToClient(tag);
    }

    public guiModuleItemHandlerSlot(int id, IItemHandler itemHandler, int targetSlot, int inventoryGroupId, int instantTransferTargetGroup, IGuiHandler guiHandler, int x, int y) {
        super(id,guiHandler,inventoryGroupId,instantTransferTargetGroup,x, y);
        this.targetSlot = targetSlot;
        this.itemHandler = itemHandler;
        stack =ItemStack.EMPTY;
        lastStack =ItemStack.EMPTY;
    }


    @Override
    public ItemStack getStackInSlot(Player player) {
        return itemHandler.getStackInSlot(targetSlot);
    }

    @Override
    public ItemStack insertItemIntoSlot(Player player, ItemStack stack, int amount) {

            ItemStack toInsert = stack.copyWithCount(amount);
            ItemStack notInserted = itemHandler.insertItem(targetSlot, toInsert, false);
            int inserted = toInsert.getCount() - notInserted.getCount();
            return stack.copyWithCount(stack.getCount() - inserted);

    }

    @Override
    public ItemStack extractItemFromSlot(Player player, int amount) {
        return itemHandler.extractItem(targetSlot,amount,false);
    }

    @Override
    public int getSlotLimit(Player player, ItemStack stack) {
        return itemHandler.getSlotLimit(targetSlot);
    }
}
