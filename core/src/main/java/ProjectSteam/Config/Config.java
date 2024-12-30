package ProjectSteam.Config;

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

public class Config {

    public static Config INSTANCE = loadConfig();

    public double WOODEN_AXLE_FRICTION = 0.1;
    public double WOODEN_AXLE_INERTIA = 0.1;
    public double WOODEN_AXLE_MAX_STRESS = 600;


    public double WOODEN_FLYWHEEL_FRICTION = 0.1;
    public double WOODEN_FLYWHEEL_INERTIA = 20;
    public double WOODEN_FLYWHEEL_MAX_STRESS = 600;


    public double WOODEN_FLYWHEEL_LARGE_FRICTION = 0.1;
    public double WOODEN_FLYWHEEL_LARGE_INERTIA = 500;
    public double WOODEN_FLYWHEEL_LARGE_MAX_STRESS = 600;


    public double WOODEN_CLUTCH_FRICTION_PER_SIDE = 0.1;
    public double WOODEN_CLUTCH_INERTIA_PER_SIDE = 3;
    public double WOODEN_CLUTCH_MAX_STRESS = 900;


    public double WOODEN_CRANKSHAFT_BIG_FRICTION = 0.2;
    public double WOODEN_CRANKSHAFT_BIG_INERTIA = 2;
    public double WOODEN_CRANKSHAFT_BIG_MAX_STRESS = 600;


    public double WOODEN_CRANKSHAFT_SMALL_FRICTION = 0.1;
    public double WOODEN_CRANKSHAFT_SMALL_INERTIA = 1;
    public double WOODEN_CRANKSHAFT_SMALL_MAX_STRESS = 300;


    public double WOODEN_DISTRIBUTOR_GEARBOX_FRICTION = 2;
    public double WOODEN_DISTRIBUTOR_GEARBOX_INERTIA = 1;
    public double WOODEN_DISTRIBUTOR_GEARBOX_MAX_STRESS = 600;


    public double WOODEN_GEARBOX_FRICTION = 2;
    public double WOODEN_GEARBOX_INERTIA = 1;
    public double WOODEN_GEARBOX_MAX_STRESS = 900;


    public double WOODEN_T_JUNCTION_FRICTION = 2;
    public double WOODEN_T_JUNCTION_INERTIA = 1;
    public double WOODEN_T_JUNCTION_MAX_STRESS = 600;


    public double HAND_GENERATOR_FRICTION = 0.1;
    public double HAND_GENERATOR_INERTIA = 5;
    public double HAND_GENERATOR_MAX_STRESS = 600;
    public double HAND_GENERATOR_MAX_FORCE = 50;
    public double HAND_GENERATOR_MAX_SPEED = 10;



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
        String filename = "projectsteam_main_config.json";
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

