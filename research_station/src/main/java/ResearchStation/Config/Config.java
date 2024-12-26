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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Config {

    public static Config INSTANCE = loadConfig();

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


    public Config() {
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
        Config.INSTANCE = new Gson().fromJson(configString, Config.class);
        System.out.println("load config:" + configString);
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
            Config c = gson.fromJson(jsonContent, Config.class);
            c.makeResearchMap();
            return c;
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse config JSON");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


