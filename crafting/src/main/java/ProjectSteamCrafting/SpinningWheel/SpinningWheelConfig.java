package ProjectSteamCrafting.SpinningWheel;

import ARLib.utils.RecipePartWithProbability;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpinningWheelConfig {

    public float baseResistance;
    public float k;
    public float clickForce;
    public List<MachineRecipe> recipes = new ArrayList<>();

    public void addRecipe(MachineRecipe r) {
        if(r.inputItem.id.isEmpty())return;
        for (MachineRecipe i : recipes) {
            if (Objects.equals(i.inputItem.id, r.inputItem.id)) {
                i.outputItems.addAll(r.outputItems);
                System.out.println("Added " + r.outputItems.size() + " outputs to Spinning Wheel recipe for input: " + r.inputItem.id);
                return;
            }
        }
        recipes.add(r);
        System.out.println("Created Spinning Wheel recipe for input: " + r.inputItem.id + " with " + r.outputItems.size() + " output items");
    }

    public static class MachineRecipe {
        public RecipePartWithProbability inputItem =new RecipePartWithProbability("");
        public List<RecipePartWithProbability> outputItems = new ArrayList<>();
        public float timeRequired = 3f;
        public float additionalResistance = 5f;
    }
}
