package AOSWorkshopExpansion.MillStone;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import static AOSWorkshopExpansion.Registry.MENU_MILLSTONE;

public class MenuMillStone extends AbstractContainerMenu {

    public EntityMillStone station;

    public MenuMillStone(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (EntityMillStone) null);
    }

    public MenuMillStone(int containerId, Inventory playerInv, EntityMillStone station) {
        super(MENU_MILLSTONE.get(), containerId);
        this.station = station;


        // 0 - 18, station inventory
        for (int i = 0; i < 18; i++) {
            addSlot(new SlotItemHandler(
                    station != null ? station.inventory : new ItemStackHandler(18),
                    i, 10 + i % 9 * 18, 20 + i / 9 * 18){
                        @Override
                        public void setChanged() {
                            if (station != null) {
                                station.setChanged();
                                station.sendUpdateTag(null);
                            }
                        }
            });
        }

        // 18 - 18+4*9 playerInventory
        int yoffset2 = 50;
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInv, i, 10 + i % 9 * 18, 75 + yoffset2));
        }
        for (int i = 9; i < 9 * 4; i++) {
            addSlot(new Slot(playerInv, i, 10 + i % 9 * 18, yoffset2 + i / 9 * 18));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            stack = slot.getItem();
            ItemStack stack1 = stack.copy();
            if (index < 18) {
                if (!moveItemStackTo(stack, 18, 18+4*9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else{
                if (!moveItemStackTo(stack, 0, 18, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack1.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            slot.onTake(player, stack1);
        }

        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
