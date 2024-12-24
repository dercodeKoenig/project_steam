package ProjectSteamCrafting.WoodMill;

import ARLib.utils.recipePart;
import ProjectSteamCrafting.Sieve.SieveConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WoodMillConfig {

        public float baseResistance;
        public List<MachineRecipe> recipes = new ArrayList<>();
        public float speedMultiplier;


    public void addRecipe(MachineRecipe r) {
        if(r.inputItem.id.isEmpty())return;
        for (MachineRecipe i : recipes) {
            if (Objects.equals(i.inputItem.id, r.inputItem.id)) {
                i.outputItems.addAll(r.outputItems);
                System.out.println("Added " + r.outputItems.size() + " outputs to woodmill recipe for input: " + r.inputItem.id);
                return;
            }
        }
        recipes.add(r);
        System.out.println("Created WoodMill recipe for input: " + r.inputItem.id + " with " + r.outputItems.size() + " output items");
    }

        public static class MachineRecipe {
            public recipePart inputItem = new recipePart("");
            public List<recipePart> outputItems = new ArrayList<>();
            public float additionalResistance = 10f;
        }

}
