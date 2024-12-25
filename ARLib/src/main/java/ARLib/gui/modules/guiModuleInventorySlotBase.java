package ARLib.gui.modules;

import ARLib.gui.IGuiHandler;
import ARLib.gui.ModularScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public abstract class guiModuleInventorySlotBase extends GuiModuleBase {

    ResourceLocation slot_background = ResourceLocation.fromNamespaceAndPath("arlib","textures/gui/gui_item_slot_background.png");

    int slot_bg_w = 18;
    int slot_bg_h = 18;
    public void setSlotBackground(ResourceLocation bg, int textureWidth, int textureHeight){
        slot_background = bg;
        slot_bg_h = textureHeight;
        slot_bg_w = textureWidth;
    }

    int w = 18;
    int h = 18;

    int invGroup;
    int instantTransferTarget;

    // returns the ItemStack for the slot to render
    public abstract ItemStack client_getItemStackToRender();

    // called when the slot is clicked from server
    public void server_handleInventoryClick(Player player, int button, boolean isShift) {
        InventoryMenu inventoryMenu = player.inventoryMenu;
        ItemStack carriedStack = inventoryMenu.getCarried();
        ItemStack stack = getStackInSlot(player);

        if (button == 0 && !isShift) {

            if (carriedStack.isEmpty() && !stack.isEmpty()) {
                // Pick up the stack
                int max_pickup = Math.min(stack.getCount(),stack.getMaxStackSize());
                inventoryMenu.setCarried(extractItemFromSlot(player,max_pickup));

            } else if (stack.isEmpty() && !carriedStack.isEmpty()) {
                // Place down the carried item
                inventoryMenu.setCarried(insertItemIntoSlot(player,carriedStack,carriedStack.getCount()));

            } else if (!stack.isEmpty() && !carriedStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, carriedStack)) {
                // Add to stack
                int transferAmount = Math.min(getSlotLimit(player,stack) - stack.getCount(), carriedStack.getCount());
                inventoryMenu.setCarried(insertItemIntoSlot(player,carriedStack,transferAmount));
            } else if (!stack.isEmpty() && !carriedStack.isEmpty() && !ItemStack.isSameItemSameComponents(stack, carriedStack)) {
                // swap items
                if (stack.getCount()<=stack.getMaxStackSize() && carriedStack.getCount()<=carriedStack.getMaxStackSize()){
                    ItemStack stackCopy = stack.copy();
                    extractItemFromSlot(player,stack.getCount());
                    insertItemIntoSlot(player,carriedStack,carriedStack.getCount());
                    inventoryMenu.setCarried(stackCopy);
                }
            }
        }
        if (button == 1 && !isShift) {
            if (carriedStack.isEmpty() && !stack.isEmpty()) {
                // Pick up half of the stack
                int halfCount = stack.getCount() / 2;
                inventoryMenu.setCarried(extractItemFromSlot(player, halfCount));

            } else if (stack.getCount() < getSlotLimit(player,stack) && !carriedStack.isEmpty()) {
                // Place one item from carried stack
                ItemStack ret = insertItemIntoSlot(player,carriedStack,1);
                inventoryMenu.setCarried(ret);
            }
        }
        if (button == 0 && isShift) {
            // move all items in the current slot to slots of the instant transfer target group
            // loop over all modules and try to find a module where the group id matches the transfer target

            for (GuiModuleBase i : this.guiHandler.getModules()) {
                if (i instanceof guiModuleInventorySlotBase j) {
                    if (j.invGroup == instantTransferTarget) {
                        ItemStack toTransfer = getStackInSlot(player);
                        ItemStack notInserted = j.insertItemIntoSlot(player,toTransfer,toTransfer.getCount());
                        int inserted = toTransfer.getCount() - notInserted.getCount();
                        extractItemFromSlot(player,inserted);
                    }
                }
            }
        }
    }


    @Override
    public void client_onMouseCLick(double mx, double my, int button) {
        if (isEnabled) {
            boolean isShiftDown =
                    InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT) ||
                            InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RSHIFT);

            if (client_isMouseOver(mx, my, onGuiX, onGuiY, w, h)) {
                CompoundTag tag = new CompoundTag();
                CompoundTag myTag = new CompoundTag();

                // add client id to the tag
                UUID myId = Minecraft.getInstance().player.getUUID();
                myTag.putUUID("uuid_from", myId);
                myTag.putInt("mouseButtonClicked", button);
                myTag.putBoolean("isShift", isShiftDown);

                tag.put(getMyTagKey(), myTag);
                guiHandler.sendToServer(tag);

                //server_handleInventoryClick(Minecraft.getInstance().player,button,isShiftDown);
            }
        }
    }

    @Override
    public void server_readNetworkData(CompoundTag tag) {
        if (tag.contains(getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(getMyTagKey());

            if (myTag.contains("uuid_from") && myTag.contains("mouseButtonClicked") && myTag.contains("isShift")) {
                UUID from_uuid = myTag.getUUID("uuid_from");
                int button = myTag.getInt("mouseButtonClicked");
                boolean isShift = myTag.getBoolean("isShift");
                Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from_uuid);

                server_handleInventoryClick(player,button,isShift);
            }
        }
    }


    public guiModuleInventorySlotBase(int id, IGuiHandler guiHandler, int inventoryGroup, int instantTransferTargetGroup, int x, int y) {
        super(id,guiHandler,x, y);
        this. invGroup = inventoryGroup;
        this. instantTransferTarget = instantTransferTargetGroup;
    }

    @Override
    public  void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (isEnabled) {

            guiGraphics.blit(slot_background, onGuiX, onGuiY, 0f, 0f, w, h, slot_bg_w, slot_bg_h);
            ModularScreen.renderItemStack(guiGraphics, onGuiX, onGuiY, client_getItemStackToRender());

            if (!client_getItemStackToRender().isEmpty() && client_isMouseOver(mouseX, mouseY, onGuiX, onGuiY, w, h)) {
                guiGraphics.fill(onGuiX, onGuiY, w + onGuiX, h + onGuiY, 0x30FFFFFF); // Semi-transparent white
                guiGraphics.renderTooltip(Minecraft.getInstance().font, client_getItemStackToRender(), mouseX, mouseY);
            }

        }
    }

    public abstract ItemStack getStackInSlot(Player p) ;


    public abstract ItemStack insertItemIntoSlot(Player p, ItemStack stack, int amount);


    public abstract ItemStack extractItemFromSlot(Player p, int amount);


    public abstract int getSlotLimit(Player p, ItemStack stack);


}
