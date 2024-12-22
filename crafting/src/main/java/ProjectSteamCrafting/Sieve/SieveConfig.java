package ProjectSteamCrafting.Sieve;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SieveConfig {

    public float baseResistance;
    public float k;
    public float clickForce;
    public List<MachineRecipe> recipes = new ArrayList<>();
    public int inventorySize;
    public int inventorySizeHopper;

    public void addRecipe(MachineRecipe r) {
        if(r.inputItem.id.isEmpty())return;
        if(r.requiredMesh.isEmpty())return;
        for (MachineRecipe i : recipes) {
            if (Objects.equals(i.inputItem.id, r.inputItem.id) && Objects.equals(r.requiredMesh, i.requiredMesh)) {
                i.outputItems.addAll(r.outputItems);
                System.out.println("Added " + r.outputItems.size() + " outputs to sieve recipe for input: " + r.inputItem.id + ", " + r.requiredMesh);
                return;
            }
        }
        recipes.add(r);
        System.out.println("Created Sieve recipe for input: " + r.inputItem.id + ", " + r.requiredMesh + " with " + r.outputItems.size() + " output items");
    }

    public static class MachineRecipe {
        public Item inputItem = new Item();
        public List<Item> outputItems = new ArrayList<>();
        public float timeRequired = 3f;
        public float additionalResistance = 10f;
        public String requiredMesh = "";

        public static class Item {
            public String id = "";
            public int amount = 1;
            public float p = 1;
        }
    }
}
