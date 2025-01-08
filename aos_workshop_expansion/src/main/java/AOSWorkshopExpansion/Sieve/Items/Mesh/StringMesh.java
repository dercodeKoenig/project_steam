package AOSWorkshopExpansion.Sieve.Items.Mesh;

import AOSWorkshopExpansion.Main;
import net.minecraft.resources.ResourceLocation;

public class StringMesh extends ItemMesh{
    public static ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/item/string_mesh.png");
    public ResourceLocation getTexture(){
        return texture;
    }
}
