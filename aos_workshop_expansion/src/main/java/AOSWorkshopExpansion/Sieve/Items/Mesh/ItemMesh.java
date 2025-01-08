package AOSWorkshopExpansion.Sieve.Items.Mesh;

import AOSWorkshopExpansion.Sieve.IMesh;
import net.minecraft.world.item.Item;

public abstract class ItemMesh extends Item implements IMesh {
    public ItemMesh() {
        super(new Properties().stacksTo(64));
    }
}
