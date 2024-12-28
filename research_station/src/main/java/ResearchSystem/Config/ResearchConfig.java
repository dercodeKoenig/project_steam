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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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


    public ResearchConfig() {
        Research t1 = new Research();
        t1.id = "example Research 1";
        t1.ticksRequired = 100;
        t1.requiredItems.add(new RecipePart("c:ingots/iron",4));
        researchList.add(t1);

        t1 = new Research();
        t1.id = "example Research 2";
        t1.ticksRequired = 300;
        t1.requiredResearches.add("example Research 1");
        t1.requiredItems.add(new RecipePart("minecraft:string",128));
        researchList.add(t1);
    }

    public void SyncConfig(ServerPlayer p) {
        if (p != null) {
            PacketDistributor.sendToPlayer(p, new PacketConfigSync(new Gson().toJson(this)));
        }
    }

    public void loadConfig(String configString) {
        ResearchConfig.INSTANCE = new Gson().fromJson(configString, ResearchConfig.class);
        System.out.println("load config:" + configString);
    }

    public static ResearchConfig loadConfig() {
        String filename = "research_list.json";
        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString());
        Path filePath = configDir.resolve(filename);
        try {
            // Create the config directory if it doesn't exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                Files.write(filePath, new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.PRIVATE).create().toJson(new ResearchConfig()).getBytes(StandardCharsets.UTF_8));
            }
            // Load JSON from the file
            String jsonContent = Files.readString(filePath);
            Gson gson = new Gson();
            ResearchConfig c = gson.fromJson(jsonContent, ResearchConfig.class);
            c.makeResearchMap();
            return c;
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse config JSON");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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


