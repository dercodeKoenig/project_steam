package ResearchSystem;


import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static ResearchSystem.Registry.RESEARCH_STATION;

public class CustomCreativeTab extends CreativeModeTab {

    public CustomCreativeTab() {
        super(CreativeModeTab.builder()
                .title(Component.literal("Research Station"))
                        .icon(()->new ItemStack(RESEARCH_STATION.get()))
                );
    }
}
