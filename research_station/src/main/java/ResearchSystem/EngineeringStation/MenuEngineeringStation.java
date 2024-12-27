package ResearchSystem.EngineeringStation;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import static ResearchSystem.Registry.MENU_ENGINEERING_STATION;

public class MenuEngineeringStation extends AbstractContainerMenu {

    public MenuEngineeringStation(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null);
    }

    public MenuEngineeringStation( int containerId, Inventory playerInv, EntityEngineeringStation station) {
        super(MENU_ENGINEERING_STATION.get(), containerId);

        int craftingx = 65;
        int craftingy = 17;
        for (int i = 0; i < 9; i++) {
                addSlot(new SlotItemHandler(
                        station != null? station.craftingInventory:new ItemStackHandler(9),
                        i, craftingx+i%3*18, craftingy+i/3*18));
            }

        addSlot(new SlotItemHandler(station!=null?station.bookInventory:new ItemStackHandler(1),0,10,35));
        addSlot(new ResultSlot(
                playerInv.player,station!=null?station.craftingInventory:new CraftingContainerItemStackHandler(3,3),
                station!=null?station.resultContainer:new ResultContainer(),
                0,150,35
                ));

        int yoffset = 65;
        for (int i = 9; i < 9*4; i++) {
            addSlot(new Slot(playerInv,i,10+i%9*18,yoffset+i/9*18));
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInv,i,10+i%9*18,75+yoffset));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
