package Farms.CropFarm;

import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleItemHandlerSlot;
import ARLib.gui.modules.guiModulePlayerInventorySlot;
import ARLib.gui.modules.guiModuleText;
import Farms.EntityFarmBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import static Farms.Registry.ENTITY_CROP_FARM;

public class EntityCropFarm extends EntityFarmBase {

    ItemStackHandler mainInventory = new ItemStackHandler(18) {
        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    ItemStackHandler inputsInventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.getItem() instanceof BlockItem bi) {
                if (bi.getBlock() instanceof CropBlock || bi.getBlock() instanceof StemBlock) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    ItemStackHandler specialResourcesInventory = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.getItem().equals(Items.BONE_MEAL))
                return true;
            else return false;
        }

        @Override
        public void onContentsChanged(int i) {
            setChanged();
        }
    };

    public EntityCropFarm(BlockPos pos, BlockState blockState) {
        super(ENTITY_CROP_FARM.get(), pos, blockState);

        for (GuiModuleBase m : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, 210, 500, 0, 1, guiHandlerMain)) {
            guiHandlerMain.getModules().add(m);
        }
        for (GuiModuleBase m : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, 150, 600, 0, 1, guiHandlerMain)) {
            guiHandlerMain.getModules().add(m);
        }


        guiModuleText t1 = new guiModuleText(11001,"Output", guiHandlerMain, 10,30,0xff000000,false);
        guiHandlerMain.getModules().add(t1);
        guiModuleText t2 = new guiModuleText(11002,"Resources", guiHandlerMain, 10,78,0xff000000,false);
        guiHandlerMain.getModules().add(t2);
        guiModuleText t3 = new guiModuleText(11003,"Special Resources", guiHandlerMain, 10,110,0xff000000,false);
        guiHandlerMain.getModules().add(t3);


        for (int i = 0; i < inputsInventory.getSlots(); i++) {
            int x = i * 18 + 10;
            int y = 89;
            guiModuleItemHandlerSlot s = new guiModuleItemHandlerSlot(900 + i, inputsInventory, i, 1, 0, guiHandlerMain, x, y);
            guiHandlerMain.getModules().add(s);
        }

        for (int i = 0; i < specialResourcesInventory.getSlots(); i++) {
            int x = i * 18 + 10;
            int y = 120;
            guiModuleItemHandlerSlot s = new guiModuleItemHandlerSlot(1000 + i, specialResourcesInventory, i, 1, 0, guiHandlerMain, x, y);
            guiHandlerMain.getModules().add(s);
        }

        int w = 9;
        for (int i = 0; i < mainInventory.getSlots(); i++) {
            int x = i % w * 18 + 10;
            int y = i / w * 18 + 40;
            guiModuleItemHandlerSlot s = new guiModuleItemHandlerSlot(800 + i, mainInventory, i, 1, 0, guiHandlerMain, x, y);
            guiHandlerMain.getModules().add(s);
        }
    }

    @Override
    public void openMainGui() {
        if (level.isClientSide) {
            guiHandlerMain.openGui(180, 240, true);
        }
    }
}
