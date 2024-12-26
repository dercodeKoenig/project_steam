package ResearchStation;

import ARLib.gui.GuiHandlerBlockEntity;
import ARLib.gui.modules.*;
import ARLib.network.INetworkTagReceiver;
import ARLib.network.PacketBlockEntity;
import ResearchStation.Config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                    CompoundTag requestDataTag = new CompoundTag();
                    requestDataTag.putUUID("request_book_tag", Minecraft.getInstance().player.getUUID());
                    PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityResearchStation.this, requestDataTag));
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
        for (GuiModuleBase i : guiModulePlayerInventorySlot.makePlayerHotbarModules(10, playerInventoryOffsetY + 70, 5000, 0, 1, guiHandler)) {
            guiHandler.getModules().add(i);
        }
        for (GuiModuleBase i : guiModulePlayerInventorySlot.makePlayerInventoryModules(10, playerInventoryOffsetY, 6000, 0, 1, guiHandler)) {
            guiHandler.getModules().add(i);
        }


        guiModuleImage i1 = new guiModuleImage(guiHandlerResearchQueue, 0, 0, 150, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/research_queue.png"), 148, 180);
        guiHandlerResearchQueue.getModules().add(i1);
        guiModuleImage i2 = new guiModuleImage(guiHandlerResearchQueue, 150, 0, 150, 200, ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/research_queue.png"), 148, 180);
        guiHandlerResearchQueue.getModules().add(i2);

        guiModuleText rqt = new guiModuleText(10000,"Research in queue",guiHandlerResearchQueue,30,20,0xff000000,false);
        guiHandlerResearchQueue.getModules().add(rqt);
        guiModuleText rat = new guiModuleText(10001,"Research available",guiHandlerResearchQueue,180,20,0xff000000,false);
        guiHandlerResearchQueue.getModules().add(rat);
        researchQueue = new guiModuleScrollContainer(new ArrayList<>(), 0x00000000, guiHandler, 19, 39, 113, 135);
        guiHandlerResearchQueue.getModules().add(researchQueue);
        availableResearch = new guiModuleScrollContainer(new ArrayList<>(), 0x00000000, guiHandler, 150+19, 39, 113, 135);
        guiHandlerResearchQueue.getModules().add(availableResearch);
    }

    void updateResearchQueueGuiFromBookStack(ItemStack bookStack) {
        researchQueue.modules.clear();
        availableResearch.modules.clear();
        if (bookStack.getItem() instanceof ItemResearchBook irb) {
            List<String> queued = irb.getQueuedResearches_readOnly(bookStack);
            for (int i = 0; i < queued.size(); i++) {
                String name = queued.get(i);
                int y = 14 * i+2;
                guiModuleText t = new guiModuleText(1000+i,name,guiHandlerResearchQueue,2,y+2,0xFF000000,false);
                researchQueue.modules.add(t);

                guiModuleButton db = new guiModuleButton(1000 + i, "-", guiHandlerResearchQueue, 100, y, 10, 10,ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/btn.png"),10,10) {
                    @Override
                    public void onButtonClicked() {
                        CompoundTag requestTag = new CompoundTag();
                        requestTag.putString("removeFromQueue", name);
                        PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityResearchStation.this,requestTag));
                    }
                };
                db.color = 0xFFFFFFFF;
                researchQueue.modules.add(db);
            }

            List<Config.Research> available = irb.getAvailableResearches(bookStack);
            for (int i = 0; i < available.size(); i++) {
                String name = available.get(i).name;
                int y = 14 * i+2;
                guiModuleText t = new guiModuleText(2000+i,name,guiHandlerResearchQueue,2,y+2,0xFF000000,false);
                availableResearch.modules.add(t);

                guiModuleButton db = new guiModuleButton(2000 + i, "+", guiHandlerResearchQueue, 100, y, 10, 10,ResourceLocation.fromNamespaceAndPath("research_station", "textures/gui/btn.png"),10,10) {
                    @Override
                    public void onButtonClicked() {
                        CompoundTag requestTag = new CompoundTag();
                        requestTag.putString("addToQueue", name);
                        PacketDistributor.sendToServer(PacketBlockEntity.getBlockEntityPacket(EntityResearchStation.this,requestTag));
                    }
                };
                db.color = 0xFFFFFFFF;
                availableResearch.modules.add(db);
            }
        }
        guiHandlerResearchQueue.screen.calculateGuiOffsetAndNotifyModules();
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

    CompoundTag getUpdatTag() {
        CompoundTag t = new CompoundTag();
        boolean hasBook = !bookInventory.getStackInSlot(0).isEmpty();
        t.putBoolean("hasBook", hasBook);
        if (hasBook) {
            ItemStack bookStack = bookInventory.getStackInSlot(0);
            t.put("bookStack", bookStack.save(level.registryAccess()));
        }
        return t;
    }

    void sendUpdateToGuiTrackingPlayers() {
        CompoundTag t = getUpdatTag();
        for (UUID player : guiHandler.playersTrackingGui.keySet()) {
            ServerPlayer p = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player);
            if (p != null) {
                PacketDistributor.sendToPlayer(p, PacketBlockEntity.getBlockEntityPacket(this, t));
            }
        }
    }

    @Override
    public void readServer(CompoundTag compoundTag) {
        guiHandler.readServer(compoundTag);

        if (compoundTag.contains("addToQueue")) {
            String name = compoundTag.getString("addToQueue");
            ItemStack bookStack = bookInventory.getStackInSlot(0);
            if (bookStack.getItem() instanceof ItemResearchBook irb) {
                List<String> queued = irb.getQueuedResearches_readOnly(bookStack);
                if (!queued.contains(name)) {
                    queued.add(name);
                    irb.setQueuedResearches(bookStack, queued);
                    sendUpdateToGuiTrackingPlayers();
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
                    sendUpdateToGuiTrackingPlayers();
                }
            }
        }

        if (compoundTag.contains("request_book_tag")) {
            UUID from = compoundTag.getUUID("request_book_tag");
            ServerPlayer pfrom = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(from);
            if (pfrom != null) {
                PacketDistributor.sendToPlayer(pfrom, PacketBlockEntity.getBlockEntityPacket(this, getUpdatTag()));
            }
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {
        guiHandler.readClient(compoundTag);

        if (compoundTag.contains("hasBook")) {
            if (!compoundTag.getBoolean("hasBook")) {
                guiHandlerResearchQueue.screen.onClose();
            } else {
                if (compoundTag.contains("bookStack")) {
                    ItemStack bookStack = ItemStack.parse(level.registryAccess(), compoundTag.getCompound("bookStack")).get();
                    updateResearchQueueGuiFromBookStack(bookStack);
                }
            }
        }
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