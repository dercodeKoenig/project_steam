package AgeOfSteam;


import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static AgeOfSteam.Registry.ITEM_WOODEN_HAMMER;

public class CustomCreativeTab extends CreativeModeTab {

    public CustomCreativeTab() {
        super(CreativeModeTab.builder()
                .title(Component.literal("Age Of Steam"))
                        .icon(()->new ItemStack(ITEM_WOODEN_HAMMER.get()))
                );
    }
}
