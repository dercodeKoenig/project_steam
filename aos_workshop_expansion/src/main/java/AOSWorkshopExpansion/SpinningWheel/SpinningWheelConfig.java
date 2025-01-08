package AOSWorkshopExpansion.SpinningWheel;

import AOSWorkshopExpansion.Main;
import ARLib.utils.RecipePartWithProbability;
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

public class SpinningWheelConfig {

    public static SpinningWheelConfig INSTANCE = loadConfig();

    public float baseResistance;
    public float k;
    public float clickForce;
    public List<SpinningWheelRecipe> recipes = new ArrayList<>();

    public void addRecipe(SpinningWheelRecipe r) {
        if(r.inputItem.id.isEmpty())return;
        for (SpinningWheelRecipe i : recipes) {
            if (Objects.equals(i.inputItem.id, r.inputItem.id)) {
                i.outputItems.addAll(r.outputItems);
                System.out.println("Added " + r.outputItems.size() + " outputs to Spinning Wheel recipe for input: " + r.inputItem.id);
                return;
            }
        }
        recipes.add(r);
        System.out.println("Created Spinning Wheel recipe for input: " + r.inputItem.id + " with " + r.outputItems.size() + " output items");
    }

    public static class SpinningWheelRecipe {
        public RecipePartWithProbability inputItem =new RecipePartWithProbability("");
        public List<RecipePartWithProbability> outputItems = new ArrayList<>();
        public float timeRequired = 3f;
        public float additionalResistance = 5f;
    }
    public void SyncConfig(ServerPlayer p) {
        if (p != null) {
            PacketDistributor.sendToPlayer(p, new SpinningWheelConfig.PacketConfigSync(new Gson().toJson(this)));
        }
    }
    public static SpinningWheelConfig loadConfig() {

        if(ServerLifecycleHooks.getCurrentServer() == null)return new SpinningWheelConfig();

        String filename = "spinning_wheel.json";
        String recipesDirName = "spinning_wheel_recipes";
        Class<SpinningWheelRecipe> recipeClass = SpinningWheelRecipe.class;
        Class<SpinningWheelConfig> configClass = SpinningWheelConfig.class;
        SpinningWheelRecipe recipe;
        SpinningWheelConfig config;

        System.out.println("load spinning wheel config");
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
                for(RecipePartWithProbability i : recipe.outputItems){
                    // if no p set, i set it to 1
                    if(i.p==0){
                        i.p=1;
                        System.out.println(recipeFile+" - output with id "+i.id+" has no probability set or it is set to 0. It will default to one.");
                    }
                }
                if(recipe.inputItem.p==0){
                    recipe.inputItem.p=1;
                    System.out.println(recipeFile+" - input with id "+recipe.inputItem.id+" has no probability set or it is set to 0. It will default to one.");
                }
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
                new Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "packet_spinning_wheel_config_sync"));


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
            SpinningWheelConfig.INSTANCE = new Gson().fromJson(config, SpinningWheelConfig.class);
            System.out.println("client loaded spinning wheel config:" + config);
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
