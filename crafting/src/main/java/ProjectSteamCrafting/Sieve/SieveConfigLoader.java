package ProjectSteamCrafting.Sieve;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SieveConfigLoader {

    public static SieveConfig loadConfig() {

        String filename = "sieve.json";
        String recipesDirName = "sieve_recipes";
        Class<SieveConfig.MachineRecipe> recipeClass = SieveConfig.MachineRecipe.class;
        Class<SieveConfig> configClass = SieveConfig.class;
        SieveConfig.MachineRecipe recipe;
        SieveConfig config;

        System.out.println("load sieve config");
        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString(), "projectsteam_crafting");
        Path filePath = configDir.resolve(filename);
        Path configRecipesDir = configDir.resolve(recipesDirName);

        try {
            // Create the config directory if it doesn't exist
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            // Load recipes from the recipesDirName directory
            if (!Files.exists(configRecipesDir)) {
                Files.createDirectories(configRecipesDir);
                System.out.println("Recipes directory created: " + configRecipesDir);
            }

            // Create a default config file if it doesn't exist
            if (!Files.exists(filePath)) {
                Resource r = ServerLifecycleHooks.getCurrentServer().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("projectsteam_crafting", "config/" + filename)).get();
                Files.copy(r.open(), filePath);
                System.out.println("Default config file copied: " + filePath);
            }

            // Load JSON from the file
            String jsonContent = Files.readString(filePath);
            Gson gson = new Gson();
            config = gson.fromJson(jsonContent, configClass);
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse config JSON");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // copy the recipes from the data packs to config
        Map<ResourceLocation, Resource> recipeFiles = ServerLifecycleHooks.getCurrentServer().getResourceManager().listResources("config/" + recipesDirName, path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : recipeFiles.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            Resource resource = entry.getValue();
            String fileName = resourceLocation.getPath().substring(resourceLocation.getPath().lastIndexOf('/') + 1);
            Path configRecipePath = configRecipesDir.resolve(fileName);
            // If the file doesn't exist in the config directory, copy it
            if (!Files.exists(configRecipePath)) {
                InputStream inputStream = null;
                try {
                    inputStream = resource.open();
                    Files.copy(inputStream, configRecipePath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("copied recipe file: " + fileName);
                } catch (IOException e) {
                    System.err.println("failed to copy recipe file to config:" + fileName);
                }
            }
        }

        // load recipes from config
        DirectoryStream<Path> stream = null;
        try {
            stream = Files.newDirectoryStream(configRecipesDir, "*.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Path recipeFile : stream) {
            try {
                String recipeContent = Files.readString(recipeFile);
                Gson gson = new Gson();
                recipe = gson.fromJson(recipeContent, recipeClass);
                config.addRecipe(recipe);
                System.out.println("Loaded recipe: " + recipeFile.getFileName());
            } catch (JsonSyntaxException e) {
                System.err.println("Failed to parse JSON, skipping recipe file: " + recipeFile.getFileName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return config;
    }
}
