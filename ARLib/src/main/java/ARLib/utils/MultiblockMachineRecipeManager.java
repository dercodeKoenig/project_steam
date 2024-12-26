package ARLib.utils;

import ARLib.multiblockCore.EntityMultiblockMachineMaster;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static ARLib.utils.ItemUtils.getFluidStackFromId;
import static ARLib.utils.ItemUtils.getItemStackFromId;

public class MultiblockMachineRecipeManager<T extends EntityMultiblockMachineMaster> {


    public int progress;
    public MachineRecipe currentRecipe;
    public List<MachineRecipe> recipes = new ArrayList<>();
    public T master;

    public MultiblockMachineRecipeManager(T masterTile) {
        this.master = masterTile;
    }

    public void reset() {
        currentRecipe = null;
        progress = 0;
    }

    public ItemFluidStacks getNextProducedItems(){
        ItemFluidStacks r = new ItemFluidStacks();
        if(currentRecipe != null){
            for (RecipePartWithProbability i:currentRecipe.outputs){
                ItemStack istack = getItemStackFromId(i.id, i.getRandomAmount(),master.getLevel().registryAccess());
                FluidStack fstack = getFluidStackFromId(i.id, i.getRandomAmount());
                if(istack!=null)
                    r.itemStacks.add(istack);
                if(fstack!=null)
                    r.fluidStacks.add(fstack);
            }
        }
        return r;
    }

    public void scanFornewRecipe() {
        for (MachineRecipe r : recipes) {
            if (master.hasinputs(new ArrayList<>(r.inputs)) && master.canFitOutputs(new ArrayList<>(r.outputs))) {
                currentRecipe = r.copy(); // make a copy because they can have different actual_num values for every new recipe
                currentRecipe.compute_actual_output_nums(); // roll the dice to compute input / output to consume for given probability
                break;
            }
        }
    }

    // returns true if it was a processing tick, false if not. can be used to check if the machine is running
    public boolean  update() {
        if (currentRecipe == null) {
            scanFornewRecipe();
            return false;
        }
        if (master.hasinputs(new ArrayList<>(currentRecipe.inputs)) && master.canFitOutputs(new ArrayList<>(currentRecipe.outputs))) {
            if (master.getTotalEnergyStored() >= currentRecipe.energyPerTick) {
                progress += 1;
                master.consumeEnergy(currentRecipe.energyPerTick);
                if (progress == currentRecipe.ticksRequired) {
                    master.consumeInput(currentRecipe.inputs, false);
                    master.produceOutput(currentRecipe.outputs);
                    reset();
                }
                return true;
            }
        } else {
            reset();
        }
        return false;
    }
}
