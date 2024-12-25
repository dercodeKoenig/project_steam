package ResearchStation.Config;

import ARLib.utils.RecipePart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Config {

    public static Config INSTANCE = loadConfig();

    public static class Research{
        public String name = "";
        public List<String> requiredResearches = new ArrayList<>();
        public List<RecipePart> requiredItems = new ArrayList<>();
    }

    public List<Research> researchList = new ArrayList<>();

    public Config(){
        Research testResearch = new Research();
        testResearch.name = "testResearch";
        testResearch.requiredItems.add(new RecipePart("minecraft:coal",4));
        researchList.add(testResearch);

        Research testResearch2 = new Research();
        testResearch2.name = "testResearch2";
        testResearch2.requiredItems.add(new RecipePart("minecraft:iron",4));
        testResearch2.requiredItems.add(new RecipePart("minecraft:stone",2));
        testResearch2.requiredResearches.add("testResearch");
        researchList.add(testResearch2);
    }

    public void SyncConfig(ServerPlayer p) {
        if (p != null) {
            PacketDistributor.sendToPlayer(p, new PacketConfigSync(new Gson().toJson(this)));
        }
    }

    public void loadConfig(String configString) {
        Config.INSTANCE = new Gson().fromJson(configString, Config.class);
        System.out.println("load config:"+configString);
    }

    public static Config loadConfig() {
        String filename = "research_station.json";
        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString());
        Path filePath = configDir.resolve(filename);
        try {
            // Create the config directory if it doesn't exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                Files.write(filePath, new GsonBuilder().setPrettyPrinting().create().toJson(new Config()).getBytes(StandardCharsets.UTF_8));
            }
            // Load JSON from the file
            String jsonContent = Files.readString(filePath);
            Gson gson = new Gson();
            return gson.fromJson(jsonContent, Config.class);
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse config JSON");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


