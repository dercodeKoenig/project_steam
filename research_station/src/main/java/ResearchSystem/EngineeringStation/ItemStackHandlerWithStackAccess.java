package ResearchSystem.EngineeringStation;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ItemStackHandlerWithStackAccess extends ItemStackHandler {
    public ItemStackHandlerWithStackAccess(int size) {
        super(size);
    }
    public NonNullList<ItemStack> getStacks(){
        return this.stacks;
    }
}
