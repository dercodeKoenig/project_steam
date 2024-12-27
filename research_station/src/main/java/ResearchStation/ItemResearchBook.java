package ResearchStation;

import ARLib.gui.GuiHandlerMainHandItem;
import ARLib.gui.modules.GuiModuleBase;
import ARLib.gui.modules.guiModuleDefaultButton;
import ARLib.gui.modules.guiModuleImage;
import ARLib.gui.modules.guiModuleScrollContainer;
import ARLib.utils.InventoryUtils;
import ARLib.utils.RecipePart;
import ResearchStation.Config.ResearchConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

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
        for (int n = 0; n < ResearchConfig.INSTANCE.researchList.size(); n++) {
            ResearchConfig.Research i = ResearchConfig.INSTANCE.researchList.get(n);
            guiModuleDefaultButton b = new guiModuleDefaultButton(n, i.id, guiHandler, 10, (int) (n * 20), 130, 16) {
                @Override
                public void onButtonClicked() {
                    researchButtonCLicked(i.id);
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
            CompoundTag itemTag = new CompoundTag();
            ListTag completed = new ListTag();
            itemTag.put("completed", completed);
            ListTag queued = new ListTag();
            itemTag.put("queued", queued);

            StringTag currentResearch = StringTag.valueOf("");
            itemTag.put("currentResearch", currentResearch);
            IntTag currentProgress = IntTag.valueOf(0);
            itemTag.put("currentProgress", currentProgress);

            return itemTag;
        }
    }

    void setStackTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    String getCurrentResearch(CompoundTag itemTag) {
        return itemTag.getString("currentResearch");
    }

    String getCurrentResearch(ItemStack stack) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        return getCurrentResearch(itemTag);
    }

    void setCurrentResearch(ItemStack stack, String researchId) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        itemTag.putString("currentResearch", researchId);
        setStackTag(stack,itemTag);
    }

    int getCurrentProgress(CompoundTag itemTag) {
        return itemTag.getInt("currentProgress");
    }

    int getCurrentProgress(ItemStack stack) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        return getCurrentProgress(itemTag);
    }

    void setCurrentProgress(ItemStack stack, int progress) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        itemTag.putInt("currentProgress", progress);
        setStackTag(stack,itemTag);
    }

    List<String> getCompletedResearches_readOnly(CompoundTag itemTag) {
        List<String> completedStringList = new ArrayList<>();
        ListTag completedResearchesT = itemTag.getList("completed", Tag.TAG_STRING);
        for (int i = 0; i < completedResearchesT.size(); i++) {
            completedStringList.add(completedResearchesT.getString(i));
        }
        return completedStringList;
    }

    List<String> getCompletedResearches_readOnly(ItemStack stack) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        return getCompletedResearches_readOnly(itemTag);
    }

    void setCompletedResearches(ItemStack stack, List<String> completedResearches) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        ListTag t = new ListTag();
        for (String i : completedResearches) {
            t.add(StringTag.valueOf(i));
        }
        itemTag.put("completed", t);
        setStackTag(stack, itemTag);
    }

    List<String> getQueuedResearches_readOnly(CompoundTag itemTag) {
        List<String> queuedStringList = new ArrayList<>();
        ListTag queuedResearchesT = itemTag.getList("queued", Tag.TAG_STRING);
        for (int i = 0; i < queuedResearchesT.size(); i++) {
            queuedStringList.add(queuedResearchesT.getString(i));
        }
        return queuedStringList;
    }

    List<String> getQueuedResearches_readOnly(ItemStack stack) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        return getQueuedResearches_readOnly(itemTag);
    }

    void setQueuedResearches(ItemStack stack, List<String> queuedResearches) {
        CompoundTag itemTag = getStackTagOrEmpty(stack);
        ListTag t = new ListTag();
        for (String i : queuedResearches) {
            t.add(StringTag.valueOf(i));
        }
        itemTag.put("queued", t);
        setStackTag(stack, itemTag);

        removeInvalidQueuedResearches(stack);
    }

    void removeInvalidQueuedResearches(ItemStack stack) {
        List<String> queuedResearches = getQueuedResearches_readOnly(stack);
        List<String> completedResearches = getCompletedResearches_readOnly(stack);
        if(!getCurrentResearch(stack).isEmpty())
            // if a research is in progress, assume it is completed to step to the next one
            completedResearches.add(getCurrentResearch(stack));

        for (int i = 0; i < queuedResearches.size(); i++) {
            String name = queuedResearches.get(i);
            ResearchConfig.Research r = ResearchConfig.INSTANCE.getResearchMap().get(name);

            if (!completedResearches.containsAll(r.requiredResearches)) {
                queuedResearches.remove(name);
                setQueuedResearches(stack, queuedResearches);
                removeInvalidQueuedResearches(stack);
                return;
            } else {
                // if this queued research is valid, assume it is completed to step to the next one
                completedResearches.add(name);
            }
        }
    }

    boolean tryCompleteResearch(ItemStack stack) {
        String researchId = getCurrentResearch(stack);
        List<String> queuedResearches = getQueuedResearches_readOnly(stack);
        queuedResearches.remove(researchId);
        setQueuedResearches(stack, queuedResearches);

        List<String> completedResearches = getCompletedResearches_readOnly(stack);


        ResearchConfig.Research r = ResearchConfig.INSTANCE.getResearchMap().get(researchId);
        boolean hasAllRequired = completedResearches.containsAll(r.requiredResearches);
        setCurrentResearch(stack, "");
        setCurrentProgress(stack, 0);
        if (hasAllRequired) {
            completedResearches.add(researchId);
            setCompletedResearches(stack, completedResearches);
            return true;
        }
        return false;
    }

    public void startResearchIfPossibleAndConsumeElements(ItemStack stack, IItemHandler inventory) {
        if (getCurrentResearch(stack).isEmpty()) {
            List<String> queued = getQueuedResearches_readOnly(stack);
            if (!queued.isEmpty()) {
                String first = queued.removeFirst();
                ResearchConfig.Research i = ResearchConfig.INSTANCE.getResearchMap().get(first);
                if(i==null){
                    setCurrentResearch(stack,"");
                    setQueuedResearches(stack, queued);
                    return; // cam happen if config was changed
                }
                if (InventoryUtils.hasInputs(List.of(inventory), new ArrayList<>(), i.requiredItems)) {
                    for (RecipePart p : i.requiredItems) {
                        InventoryUtils.consumeElements(new ArrayList<>(), List.of(inventory), p.id, p.amount, false);
                    }
                    setCurrentResearch(stack, first);
                    setCurrentProgress(stack, 0);
                    setQueuedResearches(stack, queued);
                }
            }
        }
    }

    public List<RecipePart> getRequiredItemsForResearch(ItemStack stack) {
        if (getCurrentResearch(stack).isEmpty()) {
            List<String> queued = getQueuedResearches_readOnly(stack);
            if (!queued.isEmpty()) {
                String first = queued.getFirst();
                ResearchConfig.Research c = ResearchConfig.INSTANCE.getResearchMap().get(first);
                if(c!=null) // can happen if config was changes. dont want it to crash the game
                    return c.requiredItems;
                else return List.of();
            }
        }
        return List.of();
    }

    public void tickResearch(ItemStack stack, int increment) {
        String currentResearch = getCurrentResearch(stack);
        if (!currentResearch.isEmpty()) {
            int progress = getCurrentProgress(stack);
            progress += increment;
            setCurrentProgress(stack, progress);
            ResearchConfig.Research i = ResearchConfig.INSTANCE.getResearchMap().get(currentResearch);
            if (i == null)
                // can happen if config was changed
                setCurrentResearch(stack, "");
            else if (progress >= i.ticksRequired) {
                tryCompleteResearch(stack);
            }
        }
    }

    List<ResearchConfig.Research> getAvailableResearches(ItemStack stack) {
        List<ResearchConfig.Research> availableResearch = new ArrayList<>();
        List<String> completedAndQueuedResearches = new ArrayList<>();
        completedAndQueuedResearches.addAll(getCompletedResearches_readOnly(stack));
        completedAndQueuedResearches.addAll(getQueuedResearches_readOnly(stack));
        if(!getCurrentResearch(stack).isEmpty())
            // assume it will be completed next
            completedAndQueuedResearches.add(getCurrentResearch(stack));

        for (ResearchConfig.Research r : ResearchConfig.INSTANCE.researchList) {
            if (completedAndQueuedResearches.containsAll(r.requiredResearches) && !completedAndQueuedResearches.contains(r.id) && !r.id.equals(getCurrentResearch(stack))) {
                // return all except already completed or already in queue or currently researched
                availableResearch.add(r);
            }
        }

        return availableResearch;
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
