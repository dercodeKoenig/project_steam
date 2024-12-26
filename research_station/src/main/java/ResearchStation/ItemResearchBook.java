package ResearchStation;

import ARLib.gui.GuiHandlerMainHandItem;
import ARLib.gui.IGuiHandler;
import ARLib.gui.ModularScreen;
import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleDefaultButton;
import ARLib.gui.modules.guiModuleImage;
import ARLib.gui.modules.guiModuleScrollContainer;
import ARLib.network.INetworkItemStackTagReceiver;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketPlayerMainHand;
import ResearchStation.Config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ItemResearchBook extends Item {

    GuiHandlerMainHandItem guiHandler = new GuiHandlerMainHandItem();

    public ItemResearchBook() {
        super(new Properties().stacksTo(1));
        makeGui();
    }

    void makeGui() {
        List<GuiModuleBase> modules = new ArrayList<>();
        for (int n = 0; n < Config.INSTANCE.researchList.size(); n++) {
            Config.Research i = Config.INSTANCE.researchList.get(n);
            guiModuleDefaultButton b = new guiModuleDefaultButton(n, i.name, guiHandler, 10, (int) (n * 20), 130, 16) {
                @Override
                public void onButtonClicked() {
                    researchButtonCLicked(i.name);
                }
            };
            modules.add(b);
        }

        guiHandler.getModules().clear();
        guiModuleImage i1 = new guiModuleImage(guiHandler, 0, 0, 150, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/book.png"), 148, 180);
        guiHandler.getModules().add(i1);
        guiModuleImage i2 = new guiModuleImage(guiHandler, 150, 0, 150, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/book.png"), 148, 180);
        guiHandler.getModules().add(i2);
        guiModuleScrollContainer c = new guiModuleScrollContainer(modules, 0x00000000, guiHandler, 0, 7, 150, 180);
        guiHandler.getModules().add(c);
    }

    CompoundTag getStackTagOrEmpty(ItemStack stack) {
        try {
            return stack.get(DataComponents.CUSTOM_DATA).copyTag();
        } catch (Exception e) {
            return new CompoundTag();
        }
    }
    void setStackTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }


    public void openGui() {
        //makeGui();
        guiHandler.openGui(300, 180, false);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            openGui();
        }
        return InteractionResultHolder.success(itemstack);
    }

    void researchButtonCLicked(String name) {
        System.out.println(name + " button clicked");
    }
}
