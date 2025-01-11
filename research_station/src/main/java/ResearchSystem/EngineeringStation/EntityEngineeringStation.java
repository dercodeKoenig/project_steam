package ResearchSystem.EngineeringStation;

import ARLib.network.INetworkTagReceiver;
import ARLib.utils.ItemUtils;
import ARLib.utils.RecipePart;
import ResearchSystem.Config.RecipeConfig;
import ResearchSystem.ResearchStation.BlockResearchStation;
import ResearchSystem.ResearchStation.EntityResearchStation;
import ResearchSystem.ResearchStation.ItemResearchBook;
import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

import static ResearchSystem.Registry.ENTITY_ENGINEERING_STATION;

public class EntityEngineeringStation extends BlockEntity implements INetworkTagReceiver {

    public CraftingContainerItemStackHandler craftingInventory = new CraftingContainerItemStackHandler(3, 3) {
        @Override
        public void onContentsChanged(int slot) {
            EntityEngineeringStation.super.setChanged();
            updateCraftingContainerFromCraftingInventory();
        }
    };

    void updateCraftingContainerFromCraftingInventory() {
        if (ServerLifecycleHooks.getCurrentServer() == null || level == null) return;

        CraftingInput craftInput = craftingInventory.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> optional = ServerLifecycleHooks.getCurrentServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftInput, level);
        if (optional.isPresent()) {
            RecipeHolder<CraftingRecipe> icraftingrecipe = optional.get();
            resultContainer.setRecipeUsed(icraftingrecipe);

            ItemStack result = icraftingrecipe.value().assemble(craftInput, level.registryAccess());
            resultContainer.setItem(0, result);
        } else {
            boolean foundMatch = false;
            for (RecipeConfig.Recipe r : RecipeConfig.INSTANCE.recipeList) {
                String[] shrinkedPattern = RecipeConfig.shrink(r.pattern);
                if (craftInput.width() == shrinkedPattern[0].length() && craftInput.height() == shrinkedPattern.length) {
                    boolean matches = true;
                    for (int i = 0; i < craftInput.height(); ++i) {
                        for (int j = 0; j < craftInput.width(); ++j) {
                            RecipeConfig.RecipeInput inp = r.keys.get(String.valueOf(shrinkedPattern[i].charAt(j)));
                            String id = inp.input.id;
                            ItemStack itemstack = craftInput.getItem(j, i);
                            if (!ItemUtils.matches(id, itemstack) || itemstack.getCount() < inp.input.amount) {
                                matches = false;
                            }
                        }
                    }
                    if (matches) {
                        ItemStack bookStack = bookInventory.getStackInSlot(0);
                        if (bookStack.getItem() instanceof ItemResearchBook irb) {
                            if (irb.getCompletedResearches_readOnly(bookStack).contains(r.requiredResearch)) {
                                resultContainer.setItem(0, ItemUtils.getItemStackFromIdOrTag(r.output.id, r.output.amount, level.registryAccess()));
                                foundMatch = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (!foundMatch) {
                resultContainer.setItem(0, ItemStack.EMPTY);
            }
        }
    }

    public ResultContainer resultContainer = new ResultContainer();

    public void onBookContentChanged(){
        EntityEngineeringStation.super.setChanged();
        updateCraftingContainerFromCraftingInventory();

        //update blockstate to show/hide book
        // do not use my own blockstate, it could have changed on remove after pop-inventory
        if (level.getBlockState(getBlockPos()).getBlock() instanceof BlockEngineeringStation) {
            if (bookInventory.getStackInSlot(0).getItem() instanceof ItemResearchBook && bookInventory.getStackInSlot(0).getCount()>0) {
                level.setBlock(getBlockPos(), getBlockState().setValue(BlockEngineeringStation.HAS_BOOK, true), 3);
            } else {
                level.setBlock(getBlockPos(), getBlockState().setValue(BlockEngineeringStation.HAS_BOOK, false), 3);
            }
        }
    }

    public ItemStackHandler bookInventory = new ItemStackHandler(1) {
        @Override
        public void onContentsChanged(int slot) {
onBookContentChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof ItemResearchBook;
        }
    };

    public ItemStackHandler inputInventory = new ItemStackHandler(18) {
        @Override
        public void onContentsChanged(int slot) {
            setChanged();
        }
    };


    public EntityEngineeringStation(BlockPos pos, BlockState blockState) {
        super(ENTITY_ENGINEERING_STATION.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        if (!level.isClientSide) {
            updateCraftingContainerFromCraftingInventory();
        }
    }

    public void popInventory() {
        Block.popResource(level, getBlockPos(), bookInventory.getStackInSlot(0));
        bookInventory.setStackInSlot(0, ItemStack.EMPTY);
        for (int i = 0; i < inputInventory.getSlots(); i++) {
            Block.popResource(level, getBlockPos(), inputInventory.getStackInSlot(i));
            inputInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < craftingInventory.getSlots(); i++) {
            Block.popResource(level, getBlockPos(), craftingInventory.getStackInSlot(i));
            inputInventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        setChanged();
    }

    public void tick() {

    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityEngineeringStation) t).tick();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("craftingInventory", craftingInventory.serializeNBT(registries));
        tag.put("inputInventory", inputInventory.serializeNBT(registries));
        tag.put("bookInventory", bookInventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        craftingInventory.deserializeNBT(registries, tag.getCompound("craftingInventory"));
        inputInventory.deserializeNBT(registries, tag.getCompound("inputInventory"));
        bookInventory.deserializeNBT(registries, tag.getCompound("bookInventory"));
    }


    public void JEItransferResearchRecipe(
            List<List<String>> recipe, // the target recipe, the List<String> holds valid ids for the slot
            ServerPlayer player
    ) {
        if (recipe.size() != 9) return; // it should be 3x3
        Inventory playerInv = player.getInventory();

        for (int n = 0; n < 9; n++) {
            List<String> allowedInputsAtThisPosition = recipe.get(n);

            boolean needsClearSlot = true;
            ItemStack stackInSlot = craftingInventory.getStackInSlot(n);
            for (String s : allowedInputsAtThisPosition) {
                RecipePart allowed = new Gson().fromJson(s, RecipePart.class);
                if (ItemUtils.matches(allowed.id, stackInSlot)) {
                    needsClearSlot = false;
                }
            }

            if (needsClearSlot) {
                for (int i = 0; i < inputInventory.getSlots(); i++) {
                    stackInSlot = craftingInventory.getStackInSlot(n);
                    int numBefore = stackInSlot.getCount();
                    ItemStack remaining = inputInventory.insertItem(i, stackInSlot.copy(), false);
                    int wasInserted = numBefore - remaining.getCount();
                    stackInSlot.shrink(wasInserted);
                }

                stackInSlot = craftingInventory.getStackInSlot(n);
                playerInv.placeItemBackInInventory(stackInSlot);

                if (!stackInSlot.isEmpty()) {
                    continue;
                }
            }

            for (int i = 0; i < inputInventory.getSlots(); i++) {
                stackInSlot = craftingInventory.getStackInSlot(n);
                ItemStack stackAvailable = inputInventory.getStackInSlot(i);

                for (int p = 0; p < allowedInputsAtThisPosition.size(); p++) {
                    RecipePart allowed = new Gson().fromJson(allowedInputsAtThisPosition.get(p), RecipePart.class);
                    if (ItemUtils.matches(allowed.id, stackAvailable)) {
                        int required = Math.max(0, allowed.amount - stackInSlot.getCount());
                        if (required == 0) {
                            break;
                        }
                        ItemStack extracted = inputInventory.extractItem(i, required, true);
                        ItemStack notInserted = craftingInventory.insertItem(n, extracted, true);
                        int canInsert = extracted.getCount() - notInserted.getCount();
                        extracted = inputInventory.extractItem(i, canInsert, false);
                        notInserted = craftingInventory.insertItem(n, extracted, false);
                        if (!notInserted.isEmpty()) {
                            System.out.println("error - could not insert all into craftingInventory," + i + ":" + n + ". Moving it back to input inventory");
                            inputInventory.insertItem(i, notInserted, false);
                        }
                    }
                }
            }

            for (int i = 0; i < playerInv.getContainerSize(); i++) {
                stackInSlot = craftingInventory.getStackInSlot(n);
                ItemStack stackAvailable = playerInv.getItem(i);

                for (String s : allowedInputsAtThisPosition) {
                    RecipePart allowed = new Gson().fromJson(s, RecipePart.class);
                    if (ItemUtils.matches(allowed.id, stackAvailable)) {
                        int required = Math.max(0, allowed.amount - stackInSlot.getCount());
                        if (required == 0) {
                            break;
                        }
                        ItemStack extracted = playerInv.getItem(i).copyWithCount(required);
                        ItemStack notInserted = craftingInventory.insertItem(n, extracted, true);
                        int canInsert = extracted.getCount() - notInserted.getCount();
                        extracted = playerInv.getItem(i).copyWithCount(canInsert);
                        playerInv.getItem(i).shrink(canInsert);
                        notInserted = craftingInventory.insertItem(n, extracted, false);
                        if (!notInserted.isEmpty()) {
                            System.out.println("error - could not insert all into craftingInventory," + i + ":" + n + ". Moving it back to player inventory");
                            playerInv.getItem(i).grow(notInserted.getCount());
                        }
                    }
                }
            }
        }
        craftingInventory.setChanged(); // to re-compute output
    }

    @Override
    public void readServer(CompoundTag compoundTag, ServerPlayer p) {
//System.out.println(compoundTag);
        if (compoundTag.contains("moveItems")) {
            CompoundTag moveItems = compoundTag.getCompound("moveItems");
            String data = moveItems.getString("data");
            Gson gson = new Gson();
            List<List<String>> recipes = gson.fromJson(data, List.class);
            if (recipes != null) {
                JEItransferResearchRecipe(recipes, p);
            }
        }
    }

    @Override
    public void readClient(CompoundTag compoundTag) {

    }
}
