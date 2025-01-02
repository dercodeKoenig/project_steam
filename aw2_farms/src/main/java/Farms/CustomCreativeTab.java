package Farms;


import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

import static ProjectSteam.Registry.ITEM_WOODEN_HAMMER;

public class CustomCreativeTab extends CreativeModeTab {

    public CustomCreativeTab() {
        super(CreativeModeTab.builder()
                .title(Component.literal("Farming"))
                        .icon(()->new ItemStack(Items.WHEAT))
                );
    }
}
