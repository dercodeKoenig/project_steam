package ResearchStation;

import ARLib.gui.GuiHandlerMainHandItem;
import ARLib.gui.IGuiHandler;
import ARLib.gui.IguiOnClientTick;
import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleDefaultButton;
import ARLib.gui.modules.guiModuleScrollContainer;
import ARLib.network.INetworkTagReceiver;
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

public class ItemResearchBook extends Item implements INetworkTagReceiver, IguiOnClientTick {

    GuiHandlerMainHandItem guiHandler = new GuiHandlerMainHandItem(this);

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
        guiModuleScrollContainer c = new guiModuleScrollContainer(modules,0x00000000,guiHandler,10,20,220,150);
        guiHandler.registerModule(c);
    }



    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);

        if(level.isClientSide) {
            makeGui();

            guiHandler.openGui(300, 180);
        }

        return InteractionResultHolder.success(itemstack);
    }



    @Override
    public void readServer(CompoundTag compoundTag) {

    }

    @Override
    public void readClient(CompoundTag compoundTag) {

    }

    @Override
    public void onGuiClientTick() {

    }
}
