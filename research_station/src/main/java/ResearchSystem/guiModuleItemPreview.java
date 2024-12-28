package ResearchSystem;


import ARLib.gui.IGuiHandler;
import ARLib.gui.ModularScreen;
import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.data.ModelData;

public class guiModuleItemPreview extends GuiModuleBase {
    public Item item;


    public guiModuleItemPreview(IGuiHandler guiHandler, int x, int y, Item item) {
        super(-1, guiHandler, x, y);
        this.item = item;
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if(isEnabled) {
                ModularScreen.renderItemStack(guiGraphics,onGuiX,onGuiY,new ItemStack(item));
                if(client_isMouseOver(mouseX,mouseY,onGuiX,onGuiY,18,18)){
                    guiGraphics.renderTooltip(Minecraft.getInstance().font,new ItemStack(item),mouseX,mouseY);
                }
        }
    }
}
