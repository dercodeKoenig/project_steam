package ARLib.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class ItemUtils {
    public static boolean matches(String identifier, ItemStack stack) {
        //check for tag first
        ResourceLocation tagLocation = ResourceLocation.tryParse(identifier);
        if (tagLocation != null) {
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLocation);
            if (stack.is(tagKey))
                return true;
        }

        // It's a direct item ID
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(identifier));
        return stack.is(item);
    }


    public static boolean matches(String identifier, FluidStack stack) {
        //check for tag first
        ResourceLocation tagLocation = ResourceLocation.tryParse(identifier);
        if (tagLocation != null) {
            TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagLocation);
            if (stack.is(tagKey))
                return true;
        }

        // It's a direct fluid ID
        return stack.is(BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(identifier)));
    }

    // id is the item ID string, e.g., "minecraft:diamond"
    public static ItemStack getItemStackFromId(String id, int count) {
        ResourceLocation itemId = ResourceLocation.tryParse(id);
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if(item == Items.AIR)return null;
        return new ItemStack(item, count);
    }

    // id is the fluid ID string, e.g., "minecraft:water"
    public static FluidStack getFluidStackFromId(String id, int amount) {
        ResourceLocation fluidId = ResourceLocation.tryParse(id);
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        if (fluid == Fluids.EMPTY)return null;
        return new FluidStack(fluid, amount);
    }

}
