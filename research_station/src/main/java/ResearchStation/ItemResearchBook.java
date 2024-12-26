package ResearchStation;

import ARLib.gui.GuiHandlerMainHandItem;
import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleDefaultButton;
import ARLib.gui.modules.guiModuleScrollContainer;
import ARLib.network.INetworkItemStackTagReceiver;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketPlayerMainHand;
import ResearchStation.Config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class ItemResearchBook extends Item implements INetworkItemStackTagReceiver {

    GuiHandlerMainHandItem guiHandler = new GuiHandlerMainHandItem();

    public ItemResearchBook() {
        super(new Properties().stacksTo(1));
        makeGui();
    }

    void makeGui(){
        List<GuiModuleBase> modules = new ArrayList<>();
        for (int n = 0; n < Config.INSTANCE.researchList.size(); n++) {
            Config.Research i = Config.INSTANCE.researchList.get(n);
            guiModuleDefaultButton b = new guiModuleDefaultButton(n,i.name,  guiHandler,0, (int) (n*20),150,16);
            modules.add(b);
        }
        guiHandler.getModules().clear();
        guiModuleScrollContainer c = new guiModuleScrollContainer(modules,0x10000000,guiHandler,10,10,150,160);
        guiHandler.getModules().add(c);
    }



    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);

        if(level.isClientSide) {
            guiHandler.openGui(300, 180);
        }

        return InteractionResultHolder.success(itemstack);
    }


    @Override
    public void readServer(CompoundTag compoundTag, ItemStack itemStack) {

    }

    @Override
    public void readClient(CompoundTag compoundTag) {

    }
}
