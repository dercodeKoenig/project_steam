package ResearchSystem.EngineeringStation;

import ARLib.utils.ItemUtils;
import ResearchSystem.Config.RecipeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ResearchSystem.Registry.MENU_ENGINEERING_STATION;

public class MenuEngineeringStation extends AbstractContainerMenu {

    public EntityEngineeringStation station;
    public BlockPos CLIENT_myBlockPos;

    public MenuEngineeringStation(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (EntityEngineeringStation) null);
        CLIENT_myBlockPos = extraData.readBlockPos();
    }

    public MenuEngineeringStation(int containerId, Inventory playerInv, EntityEngineeringStation station) {
        super(MENU_ENGINEERING_STATION.get(), containerId);
        this.station = station;

        int craftingx = 65;
        int craftingy = 17;
        // 0 - 9, craftingInventory
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(
                    station != null ? station.craftingInventory : new ItemStackHandler(9),
                    i, craftingx + i % 3 * 18, craftingy + i / 3 * 18){
                        @Override
                        public void setChanged() {
                            if (station != null) {
                                station.craftingInventory.setChanged();
                            }
                        }
            });
        }
        //9 bookInventory
        addSlot(new SlotItemHandler(station != null ? station.bookInventory : new ItemStackHandler(1), 0, 10, 35){
            @Override
            public void setChanged(){
                if(station!=null) {
                    station.onBookContentChanged();
                }
            }
        });
        //10 resultSlot
        addSlot(new ResultSlot(
                playerInv.player, station != null ? station.craftingInventory : new CraftingContainerItemStackHandler(3, 3),
                station != null ? station.resultContainer : new ResultContainer(),
                0, 150, 35
        ) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                if (station != null) {

                    // store items to re-stock them from inventory
                    List<ItemStack> savedStacks = new ArrayList<>();
                    for (int i = 0; i < station.craftingInventory.getSlots(); i++) {
                        savedStacks.add(station.craftingInventory.getStackInSlot(i).copy());
                    }

                    CraftingInput craftInput = station.craftingInventory.asCraftInput();
                    Optional<RecipeHolder<CraftingRecipe>> optional = ServerLifecycleHooks.getCurrentServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftInput, station.getLevel());
                    if (optional.isPresent()) {
                        super.onTake(player, stack);
                    } else {
                        // it was a research recipe, consume inputs and produce outputs.
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
                                        } else {
                                            itemstack.shrink(inp.input.amount);
                                            if(inp.onComplete != null) {
                                                ItemStack toProduce = ItemUtils.getItemStackFromIdOrTag(inp.onComplete.id, inp.onComplete.amount, station.getLevel().registryAccess());
                                                if (toProduce != null) {
                                                    moveItemStackTo(toProduce, 11, 11 + 4 * 9 + 18, false);
                                                    if (!toProduce.isEmpty()) {
                                                        Block.popResource(station.getLevel(), station.getBlockPos(), toProduce);
                                                    }
                                                }
                                            }
                                            // so that it recomputes the recipe slot and sets itself changed
                                            station.craftingInventory.setChanged();
                                        }
                                    }
                                }
                                if (matches)
                                    break;
                            }
                        }
                    }

                    // now re-stock from inventory
                    for (int i = 0; i < station.craftingInventory.getSlots(); i++) {
                        ItemStack savedStack = savedStacks.get(i);
                        for (int j = 0; j < station.inputInventory.getSlots(); j++) {
                            ItemStack stackInSlot = station.craftingInventory.getStackInSlot(i);
                            int diff = savedStack.getCount() - stackInSlot.getCount();
                            if (diff == 0) break;
                            ItemStack availableStack = station.inputInventory.getStackInSlot(j);
                            if (ItemStack.isSameItemSameComponents(availableStack, savedStack)) {
                                ItemStack remaining = station.craftingInventory.insertItem(i, availableStack, true);
                                int toInsert = Math.min(diff, availableStack.getCount() - remaining.getCount());
                                if (toInsert > 0) {
                                    ItemStack extracted = station.inputInventory.extractItem(j, toInsert, false);
                                    station.craftingInventory.insertItem(i, extracted, false);
                                }
                            }
                        }
                        //System.out.println(station.craftingInventory.getStackInSlot(i)+":"+i);
                    }
                    //station.craftingInventory.setChanged();
                }
            }
        });
        //10+

        // 11 - 11+4*9 playerInventory
        int yoffset2 = 105;
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInv, i, 10 + i % 9 * 18, 75 + yoffset2));
        }
        for (int i = 9; i < 9 * 4; i++) {
            addSlot(new Slot(playerInv, i, 10 + i % 9 * 18, yoffset2 + i / 9 * 18));
        }

        // 11+4*9 - 11+4*9*18 inputInventory
        int yoffset = 75;
        for (int i = 0; i < 18; i++) {
            addSlot(new SlotItemHandler(station != null ? station.inputInventory : new ItemStackHandler(18), i, 10 + i % 9 * 18, yoffset + i / 9 * 18));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            stack = slot.getItem();
            ItemStack stack1 = stack.copy();
            if (index == 10) {
                if (!moveItemStackTo(stack1, 11, 11 + 4 * 9 + 18, false)) {
                    return ItemStack.EMPTY;
                }
                slots.get(index).onQuickCraft(stack, stack1);

            }
            else if (index == 9) {
                if (!moveItemStackTo(stack, 11, 11 + 4 * 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index < 10) {
                if (!moveItemStackTo(stack, 11, 11 + 4 * 9 + 18, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index > 10 && index < 11 + 4 * 9) {
                // try to insert into bookslot first
                moveItemStackTo(stack, 9, 10, false);
                // if no book, insert into input inventory slots
                if (!moveItemStackTo(stack, 11 + 4 * 9, 11 + 4 * 9 + 18, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (index >= 11 + 4 * 9) {
                if (!moveItemStackTo(stack, 11, 11 + 4 * 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack1.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.setChanged();
            slot.onTake(player, stack1);
        }

        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
