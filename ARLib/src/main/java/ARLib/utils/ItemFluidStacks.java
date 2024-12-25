package ARLib.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

public class ItemFluidStacks{
    public List<ItemStack> itemStacks = new ArrayList<>();
    public List<FluidStack> fluidStacks = new ArrayList<>();

    public void toNBT(CompoundTag tag, HolderLookup.Provider registries) {
        // Serialize ItemStacks
        ListTag itemList = new ListTag();
        for (ItemStack stack : itemStacks) {
            if(!stack.isEmpty()) {
                Tag itemTag = stack.save(registries); // Serialize the ItemStack to a CompoundTag
                itemList.add(itemTag);
            }
        }
        tag.put("ItemStacks", itemList);

        // Serialize FluidStacks
        ListTag fluidList = new ListTag();
        for (FluidStack fluid : fluidStacks) {
            if(!fluid.isEmpty()) {
                Tag fluidTag = fluid.save(registries); // Serialize the FluidStack to a CompoundTag
                fluidList.add(fluidTag);
            }
        }
        tag.put("FluidStacks", fluidList);
    }

    public void fromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        // Deserialize ItemStacks
        itemStacks.clear();
        if (tag.contains("ItemStacks", ListTag.TAG_LIST)) {
            ListTag itemList = tag.getList("ItemStacks", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < itemList.size(); i++) {
                CompoundTag itemTag = itemList.getCompound(i);
                ItemStack stack = ItemStack.parse(registries, itemTag).get(); // Deserialize the ItemStack
                itemStacks.add(stack);
            }
        }

        // Deserialize FluidStacks
        fluidStacks.clear();
        if (tag.contains("FluidStacks", ListTag.TAG_LIST)) {
            ListTag fluidList = tag.getList("FluidStacks", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < fluidList.size(); i++) {
                CompoundTag fluidTag = fluidList.getCompound(i);
                FluidStack fluid = FluidStack.parse(registries,fluidTag).get(); // Deserialize the FluidStack
                fluidStacks.add(fluid);
            }
        }
    }
}
