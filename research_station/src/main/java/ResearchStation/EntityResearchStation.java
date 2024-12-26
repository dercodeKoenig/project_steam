package ResearchStation;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.modules.*;
import ARLib.network.INetworkTagReceiver;
import ResearchStation.Config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

import static ResearchStation.Registry.ENTITY_RESEARCH_STATION;


public class EntityResearchStation extends BlockEntity implements INetworkTagReceiver {

    public GuiHandlerBlockEntity guiHandlerResearchQueue;
    guiModuleScrollContainer researchQueue;
    guiModuleScrollContainer availableResearch;


    public GuiHandlerBlockEntity guiHandler;
    ItemStackHandler bookInventory;
    ItemStackHandler requiredItemsPreview;
    ItemStackHandler requiredItemsInventory;


    public EntityResearchStation(BlockPos pos, BlockState blockState) {
        super(ENTITY_RESEARCH_STATION.get(), pos, blockState);

        guiHandler = new GuiHandlerBlockEntity(this);
        guiHandlerResearchQueue = new GuiHandlerBlockEntity(this) {
            @Override
            public void onGuiClose() {
                super.onGuiClose();
                EntityResearchStation.this.openGui();
            }
        };

        requiredItemsPreview = new ItemStackHandler(16);
        requiredItemsInventory = new ItemStackHandler(16);

        bookInventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof ItemResearchBook;
            }
        };

        guiModuleItemHandlerSlot bookSlow = new guiModuleItemHandlerSlot(0, bookInventory, 0, 1, 0, guiHandler, 10, 10);
        guiModuleDefaultButton b1 = new guiModuleDefaultButton(1, "open", guiHandler, 30, 10, 30, 16) {
            @Override
            public void onButtonClicked() {
                // the stack in the item handler slot is synced to client during guiHandler.servertick()
                if (bookSlow.client_getItemStackToRender().getItem() instanceof ItemResearchBook irb) {
                    if (level.isClientSide) {
                        irb.openGui();
                    }
                }
            }
        };
        guiModuleDefaultButton b2 = new guiModuleDefaultButton(5, "research queue", guiHandler, 70, 10, 100, 16) {
            @Override
            public void onButtonClicked() {
                if (level.isClientSide) {
                    guiHandlerResearchQueue.openGui(300, 180, false);
                }
            }
        };
        guiHandler.getModules().add(bookSlow);
        guiHandler.getModules().add(b1);
        guiHandler.getModules().add(b2);

        guiModuleText target = new guiModuleText(2, "- current Research -", guiHandler, 10, 30, 0xff000000, false);
        guiHandler.getModules().add(target);

        guiModuleText inventory = new guiModuleText(4, "inventory", guiHandler, 10, 45, 0xff000000, false);
        guiHandler.getModules().add(inventory);

        guiModuleText reqItemsText = new guiModuleText(3, "required items", guiHandler, 100, 45, 0xff000000, false);
        guiHandler.getModules().add(reqItemsText);

        int itemViewW = 4;
        int itemViewOffsetY = 60;

        for (int i = 0; i < requiredItemsPreview.getSlots(); i++) {
            int x = 100 + (i % itemViewW) * 18;
            int y = itemViewOffsetY + (i / itemViewW) * 18;
            guiModuleItemHandlerSlot slot = new guiModuleItemHandlerSlot(i + 100, requiredItemsPreview, i, 1, 0, guiHandler, x, y);
            guiHandler.getModules().add(slot);
        }

        for (int i = 0; i < requiredItemsInventory.getSlots(); i++) {
            int x = 10 + (i % itemViewW) * 18;
            int y = itemViewOffsetY + (i / itemViewW) * 18;
            guiModuleItemHandlerSlot slot = new guiModuleItemHandlerSlot(i + 200, requiredItemsInventory, i, 1, 0, guiHandler, x, y);
            guiHandler.getModules().add(slot);
        }


        int playerInventoryOffsetY = 150;
        for (GuiModuleBase i : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, playerInventoryOffsetY + 70, 500, 0, 1, guiHandler)) {
            guiHandler.getModules().add(i);
        }
        for (GuiModuleBase i : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, playerInventoryOffsetY, 600, 0, 1, guiHandler)) {
            guiHandler.getModules().add(i);
        }


        guiModuleImage i1 = new guiModuleImage(guiHandlerResearchQueue, 0, 0, 150, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/book.png"), 148, 180);
        guiHandlerResearchQueue.getModules().add(i1);
        guiModuleImage i2 = new guiModuleImage(guiHandlerResearchQueue, 150, 0, 150, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/book.png"), 148, 180);
        guiHandlerResearchQueue.getModules().add(i2);
        researchQueue = new guiModuleScrollContainer(new ArrayList<>(), 0x00000000, guiHandler, 0, 7, 150, 180);
        guiHandler.getModules().add(researchQueue);
        availableResearch = new guiModuleScrollContainer(new ArrayList<>(), 0x00000000, guiHandler, 150, 7, 150, 180);
        guiHandler.getModules().add(availableResearch);
    }



    public void popInventory() {
        Block.popResource(level, getBlockPos(), bookInventory.getStackInSlot(0));
        bookInventory.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
    }

    public void openGui() {
        if (level.isClientSide) {
            guiHandler.openGui(180, 250, true);
        }
    }


    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void tick() {
        if (!level.isClientSide) {
            guiHandler.serverTick();
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityResearchStation) t).tick();
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        guiHandler.readServer(compoundTag);
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        guiHandler.readClient(compoundTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        bookInventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", bookInventory.serializeNBT(registries));
    }

}