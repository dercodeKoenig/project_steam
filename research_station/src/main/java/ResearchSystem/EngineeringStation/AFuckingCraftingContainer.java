package ResearchSystem.EngineeringStation;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AFuckingCraftingContainer implements CraftingContainer {
        private final NonNullList<ItemStack> items;
        private final int width;
        private final int height;

        public AFuckingCraftingContainer(int width, int height) {
            this(width, height, NonNullList.withSize(width * height, ItemStack.EMPTY));
        }

        public AFuckingCraftingContainer(int width, int height, NonNullList<ItemStack> items) {
            this.items = items;
            this.width = width;
            this.height = height;
        }

        public int getContainerSize() {
            return this.items.size();
        }

        public boolean isEmpty() {
            for(ItemStack itemstack : this.items) {
                if (!itemstack.isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        public ItemStack getItem(int slot) {
            return slot >= this.getContainerSize() ? ItemStack.EMPTY : this.items.get(slot);
        }

        public ItemStack removeItemNoUpdate(int slot) {
            return ContainerHelper.takeItem(this.items, slot);
        }

        public ItemStack removeItem(int slot, int amount) {
            return ContainerHelper.removeItem(this.items, slot, amount);
        }

        public void setItem(int slot, ItemStack stack) {
            this.items.set(slot, stack);
        }

        public void setChanged() {
        }

        public boolean stillValid(Player player) {
            return true;
        }

        public void clearContent() {
            this.items.clear();
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }

        public List<ItemStack> getItems() {
            return List.copyOf(this.items);
        }

        public void fillStackedContents(StackedContents contents) {
            for(ItemStack itemstack : this.items) {
                contents.accountSimpleStack(itemstack);
            }
        }

}
