package AOSWorkshopExpansion.MillStone;

import AOSWorkshopExpansion.Main;
import ARLib.utils.RecipePart;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MillStoneConfig {

    public static MillStoneConfig INSTANCE = loadConfig();

    public float resistance = 100;
    public List<MillStoneRecipe> recipes = new ArrayList<>();

    public void addRecipe(MillStoneRecipe r) {
        if(r.inputItem.id.isEmpty())return;
        for (MillStoneRecipe i : recipes) {
            if (Objects.equals(i.inputItem.id, r.inputItem.id)) {
                    System.out.println("recipe for input "+r.inputItem.id+" already exists and produces "+i.outputItem.id+". This recipe will be skipped");
                return;
            }
        }
        recipes.add(r);
        System.out.println("Created Millstone recipe for input: " + r.inputItem.id + " with output " + r.outputItem.id+" "+r.outputItem.amount);
    }

    public static class MillStoneRecipe {
        public RecipePart inputItem =new RecipePart("");
        public RecipePart outputItem = new RecipePart("");
        public float timeRequired = 3f;
    }
    public void SyncConfig(ServerPlayer p) {
        if (p != null) {
            PacketDistributor.sendToPlayer(p, new MillStoneConfig.PacketConfigSync(new Gson().toJson(this)));
        }
    }
    public static MillStoneConfig loadConfig() {

        if(ServerLifecycleHooks.getCurrentServer() == null)return new MillStoneConfig();

        String filename = "millstone.json";
        String recipesDirName = "millstone_recipes";
        Class<MillStoneRecipe> recipeClass = MillStoneRecipe.class;
        Class<MillStoneConfig> configClass = MillStoneConfig.class;
        MillStoneRecipe recipe;
        MillStoneConfig config;

        System.out.println("load millstone config");
        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString(), Main.MODID);
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
                Resource r = ServerLifecycleHooks.getCurrentServer().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath(Main.MODID, "config/" + filename)).get();
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

    public static class PacketConfigSync implements CustomPacketPayload {

        public static final Type<PacketConfigSync> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "packet_millstone_config_sync"));


        public PacketConfigSync(String config) {
            this.config = config;
        }

        String config;
        public String getConfig() {
            return config;
        }


        public static final StreamCodec<ByteBuf, PacketConfigSync> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                PacketConfigSync::getConfig,
                PacketConfigSync::new
        );

        // this is for jei to load the recipes after sync
         public static Runnable jeiRunnableOnConfigLoad = null;

         public static void readClient(final PacketConfigSync data, final IPayloadContext context) {
            String config = data.getConfig();
            MillStoneConfig.INSTANCE = new Gson().fromJson(config, MillStoneConfig.class);
            System.out.println("client loaded millstone config:" + config);
            if(jeiRunnableOnConfigLoad != null){
                jeiRunnableOnConfigLoad.run();
            }
        }
        public static void readServer(final PacketConfigSync data, final IPayloadContext context) {
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void register(PayloadRegistrar registrar) {
            registrar.playBidirectional(
                    PacketConfigSync.TYPE,
                    PacketConfigSync.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            PacketConfigSync::readClient,
                            PacketConfigSync::readServer
                    )
            );
        }
    }
}