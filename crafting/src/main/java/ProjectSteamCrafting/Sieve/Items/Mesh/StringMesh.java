package ProjectSteamCrafting.Sieve.Items.Mesh;

import net.minecraft.resources.ResourceLocation;

public class StringMesh extends ItemMesh{
    public static ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("projectsteam_crafting", "textures/item/string_mesh.png");
    public ResourceLocation getTexture(){
        return texture;
    }
}
