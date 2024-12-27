package ResearchSystem.EngineeringStation;

import ARLib.utils.ItemUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;

import static ResearchSystem.Registry.MENU_ENGINEERING_STATION;

public class MenuEngineeringStation extends AbstractContainerMenu {

    public MenuEngineeringStation(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null);
    }

    public MenuEngineeringStation(int containerId, Inventory playerInv, EntityEngineeringStation station) {
        super(MENU_ENGINEERING_STATION.get(), containerId);

        int craftingx = 65;
        int craftingy = 17;
        // 0 - 9
        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(
                    station != null ? station.craftingInventory : new ItemStackHandler(9),
                    i, craftingx + i % 3 * 18, craftingy + i / 3 * 18));
        }
        //9
        addSlot(new SlotItemHandler(station != null ? station.bookInventory : new ItemStackHandler(1), 0, 10, 35));
        //10
        addSlot(new ResultSlot(
                playerInv.player, station != null ? station.craftingInventory : new CraftingContainerItemStackHandler(3, 3),
                station != null ? station.resultContainer : new ResultContainer(),
                0, 150, 35
        ){
            @Override
            public void onTake(Player player, ItemStack stack) {
                if (station != null) {
                    CraftingInput craftInput = station.craftingInventory.asCraftInput();
                    Optional<RecipeHolder<CraftingRecipe>> optional = ServerLifecycleHooks.getCurrentServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftInput, station.getLevel());
                    if (optional.isPresent()) {
                        super.onTake(player,stack);
                    }else{
                        // it was a research recipe, consume inputs and produce outputs.
                        for (EngineeringConfig.Recipe r : EngineeringConfig.INSTANCE.recipeList) {
                            String[] shrinkedPattern = EngineeringConfig.shrink(r.pattern);
                            if (craftInput.width() == shrinkedPattern[0].length() && craftInput.height() == shrinkedPattern.length) {
                                boolean matches = true;
                                for (int i = 0; i < craftInput.height(); ++i) {
                                    for (int j = 0; j < craftInput.width(); ++j) {
                                        EngineeringConfig.RecipeInput inp = r.keys.get(String.valueOf(shrinkedPattern[i].charAt(j)));
                                        String id = inp.input.id;
                                        ItemStack itemstack = craftInput.getItem(j, i);
                                        if (!ItemUtils.matches(id, itemstack) || itemstack.getCount() < inp.input.amount) {
                                            matches = false;
                                        }else{
                                            itemstack.shrink(inp.input.amount);
                                            ItemStack toProduce = ItemUtils.getItemStackFromId(inp.onComplete.id,inp.onComplete.amount,station.getLevel().registryAccess());
                                            if(toProduce!=null){
                                                moveItemStackTo(toProduce,11,11+4*9,false);
                                                if(!toProduce.isEmpty()){
                                                    Block.popResource(station.getLevel(),station.getBlockPos(),toProduce);
                                                }
                                            }
                                        }
                                    }
                                }
                                if(matches)
                                    break;
                            }
                        }
                    }
                }
            }
        });
        //10+
        int yoffset = 65;
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInv, i, 10 + i % 9 * 18, 75 + yoffset));
        }
        for (int i = 9; i < 9 * 4; i++) {
            addSlot(new Slot(playerInv, i, 10 + i % 9 * 18, yoffset + i / 9 * 18));
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
                if(!moveItemStackTo(stack1, 11, 11 + 4 * 9, false)){
                 return ItemStack.EMPTY;
                }
                slots.get(index).onQuickCraft(stack, stack1);

            }
            if(stack1.getCount() == stack.getCount()){
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
