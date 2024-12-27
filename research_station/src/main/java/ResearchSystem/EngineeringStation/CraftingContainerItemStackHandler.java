package ResearchSystem.EngineeringStation;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;
import java.util.stream.Collectors;

public class CraftingContainerItemStackHandler extends ItemStackHandler implements CraftingContainer {
    int width;
    int height;
    
    public CraftingContainerItemStackHandler(int width, int height) {
        super(width*height);
        this.width = width;
        this.height = height;
        
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }


    public ItemStack getItem(int index) {
        return index >= 0 && index < this.stacks.size() ? (ItemStack)this.stacks.get(index) : ItemStack.EMPTY;
    }

    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(this.stacks, index, count);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack removeItemNoUpdate(int index) {
        ItemStack itemstack = (ItemStack)this.stacks.get(index);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.stacks.set(index, ItemStack.EMPTY);
            return itemstack;
        }
    }

    public void setItem(int index, ItemStack stack) {
        this.stacks.set(index, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    public void setChanged() {
onContentsChanged(-1);
    }

    public int getContainerSize() {
        return this.stacks.size();
    }

    public boolean isEmpty() {
        for(ItemStack itemstack : this.stacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        this.stacks.clear();
        this.setChanged();
    }

    public void fillStackedContents(StackedContents helper) {
        for(ItemStack itemstack : this.stacks) {
            helper.accountStack(itemstack);
        }
    }

    public String toString() {
        return ((List)this.stacks.stream().filter((p_19194_) -> !p_19194_.isEmpty()).collect(Collectors.toList())).toString();
    }

    public NonNullList<ItemStack> getItems() {
        return this.stacks;
    }
}
