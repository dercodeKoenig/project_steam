package ResearchSystem.ResearchStation;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.ModularScreen;
import ARLib.gui.modules.*;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ARLib.utils.InventoryUtils;
import ARLib.utils.RecipePart;
import ResearchSystem.Config.ResearchConfig;
import ResearchSystem.ItemResearchBook;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static ResearchSystem.Registry.ENTITY_RESEARCH_STATION;


public class EntityResearchStation extends BlockEntity implements INetworkTagReceiver {

    public GuiHandlerBlockEntity guiHandlerResearchQueue;
    guiModuleScrollContainer researchQueue;
    guiModuleScrollContainer availableResearch;


    public GuiHandlerBlockEntity guiHandler;
    guiModuleItemHandlerSlot bookSlot; // this can be used to read book data on client because it will be synced while gui is open
    guiModuleText targetResearchText;
    ItemStackHandler bookInventory;
    ItemStackHandler requiredItemsPreview;
    ItemStackHandler requiredItemsInventory;
    guiModuleProgressBarHorizontal6px progressBar;


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

        requiredItemsPreview = new ItemStackHandler(16) {
            // block input/output on this one
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        };
        requiredItemsInventory = new ItemStackHandler(16) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };

        bookInventory = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();

                //update blockstate to show/hide book
                if (level.getBlockState(getBlockPos()).getBlock() instanceof BlockResearchStation) {
                    if (getStackInSlot(0).getItem() instanceof ItemResearchBook) {
                        level.setBlock(getBlockPos(), getBlockState().setValue(BlockResearchStation.HAS_BOOK, true), 3);
                    } else {
                        level.setBlock(getBlockPos(), getBlockState().setValue(BlockResearchStation.HAS_BOOK, false), 3);
                    }
                }
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() instanceof ItemResearchBook;
            }
        };

        bookSlot = new guiModuleItemHandlerSlot(0, bookInventory, 0, 1, 0, guiHandler, 10, 10){
            @Override
            public void client_handleDataSyncedToClient(CompoundTag tag) {
                super.client_handleDataSyncedToClient(tag);

                // re-build the gui when something changes. The book-itemStack will be synced while gui is open
                updateResearchQueueGuiFromBookStack(this.client_getItemStackToRender());
                if(client_getItemStackToRender().getItem() instanceof  ItemResearchBook irb){
                    irb.makeGui(client_getItemStackToRender());
                }
            }
            };

        guiModuleDefaultButton b1 = new guiModuleDefaultButton(1, "open", guiHandler, 30, 10, 30, 16) {
            @Override
            public void onButtonClicked() {
                // the stack in the item handler slot is synced to client during guiHandler.servertick()
                if (bookSlot.client_getItemStackToRender().getItem() instanceof ItemResearchBook irb) {
                    if (level.isClientSide) {
                        irb.openGui(bookSlot.client_getItemStackToRender());
                    }
                }
            }
        };

        guiModuleDefaultButton b2 = new guiModuleDefaultButton(5, "research queue", guiHandler, 70, 10, 100, 16) {
            @Override
            public void onButtonClicked() {
                if (level.isClientSide) {
                    guiHandlerResearchQueue.openGui(380, 180, false);
                }
            }
        };
        guiHandler.getModules().add(bookSlot);
        guiHandler.getModules().add(b1);
        guiHandler.getModules().add(b2);


        targetResearchText = new guiModuleText(2, "- current Research -", guiHandler, 10, 30, 0xff000000, false);
        guiHandler.getModules().add(targetResearchText);

        guiModuleText inventory = new guiModuleText(4, "inventory", guiHandler, 10, 55, 0xff000000, false);
        guiHandler.getModules().add(inventory);

        guiModuleText reqItemsText = new guiModuleText(3, "required items", guiHandler, 100, 55, 0xff000000, false);
        guiHandler.getModules().add(reqItemsText);

        int itemViewW = 4;
        int itemViewOffsetY = 70;
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


        int playerInventoryOffsetY = 160;
        for (GuiModuleBase i : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, playerInventoryOffsetY + 62, 5000, 0, 1, guiHandler)) {
            guiHandler.getModules().add(i);
        }
        for (GuiModuleBase i : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, playerInventoryOffsetY, 6000, 0, 1, guiHandler)) {
            guiHandler.getModules().add(i);
        }

        progressBar = new guiModuleProgressBarHorizontal6px(9009,0xff00ff00,guiHandler,10,40);
guiHandler.getModules().add(progressBar);



        guiModuleImage i1 = new guiModuleImage(guiHandlerResearchQueue, 0, 0, 190, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/research_queue.png"), 148, 180);
        guiHandlerResearchQueue.getModules().add(i1);
        guiModuleImage i2 = new guiModuleImage(guiHandlerResearchQueue, 190, 0, 190, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/research_queue.png"), 148, 180);
        guiHandlerResearchQueue.getModules().add(i2);

        guiModuleText rqt = new guiModuleText(100000, "Research in queue", guiHandlerResearchQueue, 30, 20, 0xff000000, false);
        guiHandlerResearchQueue.getModules().add(rqt);
        guiModuleText rat = new guiModuleText(100010, "Research available", guiHandlerResearchQueue, 220, 20, 0xff000000, false);
        guiHandlerResearchQueue.getModules().add(rat);
        researchQueue = new guiModuleScrollContainer(new ArrayList<>(), 0x00000000, guiHandler, 24, 39, 143, 135);
        guiHandlerResearchQueue.getModules().add(researchQueue);
        availableResearch = new guiModuleScrollContainer(new ArrayList<>(), 0x00000000, guiHandler, 190 + 24, 39, 143, 135);
        guiHandlerResearchQueue.getModules().add(availableResearch);
    }

    void updateResearchQueueGuiFromBookStack(ItemStack bookStack) {
        researchQueue.modules.clear();
        availableResearch.modules.clear();

        if (bookStack.getItem() instanceof ItemResearchBook irb) {
            List<String> queued = irb.getQueuedResearches_readOnly(bookStack);
            for (int i = 0; i < queued.size(); i++) {
                String name = queued.get(i);
                int y = 14 * i + 2;
                guiModuleText t = new guiModuleText(10000 + i, name, guiHandlerResearchQueue, 2, y + 2, 0xFF000000, false);
                researchQueue.modules.add(t);

                guiModuleButton db = new guiModuleButton(20000 + i, "-", guiHandlerResearchQueue, 130, y, 10, 10, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/btn.png"), 10, 10) {
                    @Override
                    public void onButtonClicked() {
                        CompoundTag requestTag = new CompoundTag();
                        requestTag.putString("removeFromQueue", name);
                        PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityResearchStation.this, requestTag));
                    }
                };
                db.color = 0xFFFFFFFF;
                researchQueue.modules.add(db);
            }

            List<ResearchSystem.Config.ResearchConfig.Research> available = irb.getAvailableResearches(bookStack);
            for (int i = 0; i < available.size(); i++) {
                String name = available.get(i).id;
                int y = 14 * i + 2;
                guiModuleText t = new guiModuleText(30000 + i, name, guiHandlerResearchQueue, 2, y + 2, 0xFF000000, false);
                availableResearch.modules.add(t);

                guiModuleButton db = new guiModuleButton(40000 + i, "+", guiHandlerResearchQueue, 130, y, 10, 10, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/btn.png"), 10, 10) {
                    @Override
                    public void onButtonClicked() {
                        CompoundTag requestTag = new CompoundTag();
                        requestTag.putString("addToQueue", name);
                        PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityResearchStation.this, requestTag));
                    }
                };
                db.color = 0xFFFFFFFF;
                availableResearch.modules.add(db);
            }
        }
        if(guiHandlerResearchQueue.screen != null)
            ((ModularScreen)guiHandlerResearchQueue.screen).calculateGuiOffsetAndNotifyModules();
    }

    public void popInventory() {
        Block.popResource(level, getBlockPos(), bookInventory.getStackInSlot(0));
        bookInventory.setStackInSlot(0, ItemStack.EMPTY);

        for (int i = 0; i < requiredItemsInventory.getSlots(); i++) {
            Block.popResource(level, getBlockPos(), requiredItemsInventory.getStackInSlot(i));
            requiredItemsInventory.setStackInSlot(i, ItemStack.EMPTY);
        }


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

    void setRequiredItemsPreview() {
        for (int i = 0; i < requiredItemsPreview.getSlots(); i++) {
            requiredItemsPreview.setStackInSlot(i, ItemStack.EMPTY);
        }
        ItemStack book = bookInventory.getStackInSlot(0);
        if (book.getItem() instanceof ItemResearchBook irb) {
            ItemStackHandler tmp = new ItemStackHandler(requiredItemsPreview.getSlots());
            for (RecipePart i : irb.getRequiredItemsForResearch(book)) {
                InventoryUtils.createElements(List.of(), List.of(tmp), i.id, i.amount,level.registryAccess());
            }

            for (int i = 0; i < requiredItemsPreview.getSlots(); i++) {
                requiredItemsPreview.setStackInSlot(i, tmp.getStackInSlot(i));
            }
        }
    }

    public void tick() {
        if (!level.isClientSide) {
            guiHandler.serverTick();
            setRequiredItemsPreview();
            ItemStack book = bookInventory.getStackInSlot(0);
            if (book.getItem() instanceof ItemResearchBook irb) {
                irb.startResearchIfPossibleAndConsumeElements(book, requiredItemsInventory);
                irb.tickResearch(book, 1);
                double progress = 0;
                String currentResearch = irb.getCurrentResearch(book);
                if(!currentResearch.isEmpty()) {
                    progress = (double) irb.getCurrentProgress(book) / ResearchConfig.INSTANCE.getResearchMap().get(currentResearch).ticksRequired;
                    targetResearchText.setTextAndSync(currentResearch);
                }else {
                    List<String> queue = irb.getQueuedResearches_readOnly(book);
                    if (!queue.isEmpty()) {
                        targetResearchText.setTextAndSync(queue.getFirst());
                    } else {
                        targetResearchText.setTextAndSync("no research selected");
                    }
                }
                progressBar.setProgressAndSync(progress);

            }else{
                targetResearchText.setTextAndSync("provide research book");
                progressBar.setProgressAndSync(0);
            }
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityResearchStation) t).tick();
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        guiHandler.readServer(compoundTag);
        //System.out.println(compoundTag);

        if (compoundTag.contains("addToQueue")) {
            String name = compoundTag.getString("addToQueue");
            ItemStack bookStack = bookInventory.getStackInSlot(0);
            if (bookStack.getItem() instanceof ItemResearchBook irb) {
                List<String> queued = irb.getQueuedResearches_readOnly(bookStack);
                if (!queued.contains(name)) {
                    queued.add(name);
                    irb.setQueuedResearches(bookStack, queued);
                }
            }
        }
        if(compoundTag.contains("removeFromQueue")){
            String name = compoundTag.getString("removeFromQueue");
            ItemStack bookStack = bookInventory.getStackInSlot(0);
            if (bookStack.getItem() instanceof ItemResearchBook irb) {
                List<String> queued = irb.getQueuedResearches_readOnly(bookStack);
                if (queued.contains(name)) {
                    queued.remove(name);
                    irb.setQueuedResearches(bookStack, queued);
                }
            }
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        guiHandler.readClient(compoundTag);
        //System.out.println(compoundTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        bookInventory.deserializeNBT(registries, tag.getCompound("bookInventory"));
        requiredItemsInventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("bookInventory", bookInventory.serializeNBT(registries));
        tag.put("inventory", requiredItemsInventory.serializeNBT(registries));
    }
}