package ResearchSystem.Config;

import ARLib.utils.RecipePart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class ResearchConfig {

    public static ResearchConfig INSTANCE = loadConfig();

    public static class Research {
        public String id = "";
        public int ticksRequired = 100;
        public List<String> requiredResearches = new ArrayList<>();
        public List<RecipePart> requiredItems = new ArrayList<>();
    }

    public List<Research> researchList = new ArrayList<>();

    private Map<String, Research> researchMap = new HashMap<>();
    private void makeResearchMap() {
        researchMap = new HashMap<>();
        for (Research i : researchList) {
            researchMap.put(i.id, i);
        }
    }
    public Map<String, Research> getResearchMap() {
        return researchMap;
    }


    public ResearchConfig() {}

    public void SyncConfig(ServerPlayer p) {
        if (p != null) {
            PacketDistributor.sendToPlayer(p, new PacketConfigSync(new Gson().toJson(this)));
        }
    }

    public void loadConfig(String configString) {
        ResearchConfig.INSTANCE = new Gson().fromJson(configString, ResearchConfig.class);
        ResearchConfig.INSTANCE.makeResearchMap();
        System.out.println("client loaded config:" + configString);
    }

    public static ResearchConfig loadConfig() {
        String folderName = "researches";
        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString());
        Path folderPath = configDir.resolve(folderName);

        ResearchConfig researchConfig = new ResearchConfig();

        try {
            // Create the folder if it doesn't exist
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
                System.out.println("Created recipes folder: " + folderPath);
            }

            // Scan for recipe files in the folder
            DirectoryStream<Path> researchFiles = Files.newDirectoryStream(folderPath, "*.json");
            List<Path> sortedFiles = StreamSupport.stream(researchFiles.spliterator(), false)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .excludeFieldsWithModifiers(Modifier.PRIVATE)
                    .create();

            for (Path filePath : sortedFiles) {
                try {
                    // Read each recipe file
                    String jsonContent = Files.readString(filePath);
                    ResearchConfig.Research research = gson.fromJson(jsonContent, ResearchConfig.Research.class); // Assuming individual recipe files correspond to a Recipe class

                    if (research != null) {
                        researchConfig.researchList.add(research); // Add the parsed recipe to RecipeConfig
                        System.out.println("added research:"+filePath.getFileName());
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Failed to parse recipe file: " + filePath);
                    e.printStackTrace();
                } catch (IOException e) {
                    System.err.println("Failed to read recipe file: " + filePath);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error accessing recipes folder", e);
        }
researchConfig.makeResearchMap();
        return researchConfig;
    }


    public static class PacketConfigSync implements CustomPacketPayload {

        public static final Type<PacketConfigSync> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("research_station", "packet_research_config_sync"));


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

        public static void readClient(final PacketConfigSync data, final IPayloadContext context) {
            String config = data.getConfig();
            ResearchConfig.INSTANCE.loadConfig(config);
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


